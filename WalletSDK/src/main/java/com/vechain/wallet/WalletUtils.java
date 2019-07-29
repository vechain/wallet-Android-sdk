
/*
 * Vechain Wallet SDK is licensed under the MIT LICENSE, also included in LICENSE file in the repository.
 *
 * Copyright (c) 2019 VeChain support@vechain.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.vechain.wallet;

import android.text.TextUtils;

import com.vechain.wallet.bip32.ExtendedKey;
import com.vechain.wallet.bip32.ValidationException;
import com.vechain.wallet.bip39.MnemonicCode;
import com.vechain.wallet.bip39.MnemonicWordNumber;
import com.vechain.wallet.bip39.SecureRandomSeed;
import com.vechain.wallet.bip44.AddressIndex;
import com.vechain.wallet.bip44.BIP44;
import com.vechain.wallet.bip44.CoinPairDerive;
import com.vechain.wallet.bip44.CoinType;
import com.vechain.wallet.dapp.utils.GasUtil;
import com.vechain.wallet.key.ECDSASignature;
import com.vechain.wallet.key.KeyPair;
import com.vechain.wallet.key.Recover;
import com.vechain.wallet.key.ThorKeyPair;
import com.vechain.wallet.keystore.CipherException;
import com.vechain.wallet.keystore.Keystore;
import com.vechain.wallet.keystore.KeystoreUtil;
import com.vechain.wallet.network.Api;
import com.vechain.wallet.network.NodeUrl;
import com.vechain.wallet.network.bean.core.Block;
import com.vechain.wallet.network.bean.core.SendTransactionResult;
import com.vechain.wallet.network.bean.core.call.BatchCallData;
import com.vechain.wallet.network.bean.core.call.CallResult;
import com.vechain.wallet.network.bean.version.Version;
import com.vechain.wallet.network.engine.NetCallBack;
import com.vechain.wallet.network.utils.GsonUtils;
import com.vechain.wallet.thor.tx.Clause;
import com.vechain.wallet.thor.tx.ThorBuilder;
import com.vechain.wallet.thor.tx.ThorTransaction;
import com.vechain.wallet.thor.tx.TransferParameter;
import com.vechain.wallet.utils.HexUtils;
import com.vechain.wallet.utils.RxUtils;
import com.vechain.wallet.utils.StringUtils;

import org.spongycastle.jcajce.provider.digest.Blake2b;

import java.util.ArrayList;
import java.util.List;

public class WalletUtils {


    /**
     * Create wallet callback interface
     */
    public interface OnCreateWalletCallback {
        /**
         * Create wallet result callback
         *
         * @param wallet wallet object
         */
        void onCreate(Wallet wallet);
    }

    /**
     * set node url
     *
     * @param nodeUrl NodeUrl enum
     */
    public static void setNodeUrl(NodeUrl nodeUrl) {
        if (nodeUrl == null)
            nodeUrl = NodeUrl.MAIN_NODE;
        Api.setNodeUrl(nodeUrl);

    }

    /**
     * Get the node url
     *
     * @return node url
     */
    public static String getNodeUrl() {
        return Api.getBlockChainHost();
    }


    /**
     * Enter password to create Wallet
     *
     * @param password keystore password
     * @param callback Listen for creation status, create successfully returned Wallet object, and create failure returned null
     */
    public static void createWallet(final String password, final OnCreateWalletCallback callback) {

        if (password == null || password.length() <= 0) {
            if (callback != null)
                callback.onCreate(null);
            return;
        }

        List<String> mnemonicWords = null;
        byte[] random = SecureRandomSeed.random(MnemonicWordNumber.TWELVE);
        MnemonicCode mnemonicCode = new MnemonicCode();
        try {
            mnemonicWords = mnemonicCode.toMnemonic(random);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mnemonicWords == null) {
            if (callback != null)
                callback.onCreate(null);
            return;
        }

        createWallet(mnemonicWords, password, callback);
    }


    /**
     * Create wallets with mnemonics
     *
     * @param mnemonicWords Limit 12, 15, 18, 21, 24 words
     * @param password      Generate wallet keystore password
     * @param callback      Listen for creation status, create successfully returned Wallet object, and create failure returned null
     */
    public static void createWallet(final List<String> mnemonicWords, final String password, final OnCreateWalletCallback callback) {


        if (!isValidMnemonicWords(mnemonicWords) || TextUtils.isEmpty(password)) {
            if (callback != null)
                callback.onCreate(null);
            return;
        }

        RxUtils.onFlowable(new RxUtils.BusinessCallBack<String, Wallet>() {

            @Override
            public Wallet map(String s) {

                Wallet wallet = createWalletInner(mnemonicWords, s);

                return wallet;
            }

            @Override
            public void onSuccess(Wallet wallet) {

                if (callback != null)
                    callback.onCreate(wallet);
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (callback != null)
                    callback.onCreate(null);
            }
        }, password);

    }

    private static Wallet createWalletInner(final List<String> mnemonicWords, final String password) {

        Keystore walletFile = null;
        String privateKey = null;
        try {
            // seed
            byte[] seed = MnemonicCode.toSeed(mnemonicWords, "");

            ExtendedKey extendedKey = ExtendedKey.create(seed);

            // Format 1 for generating AddressIndex
            AddressIndex address = BIP44.m().purpose44()
                    .coin(CoinType.VET).account(0).external().address(0);

            // Format 2 for generating AddressIndex (must conform to Bip44 standard)
            // AddressIndex address = BIP44.parsePath("m/44'/818'/0'/0");

            CoinPairDerive coinKeyPair = new CoinPairDerive(extendedKey);
            KeyPair master = coinKeyPair.derive(address);

            // 私钥
            byte[] privateKeyBytes = master.getRawPrivateKey();
            privateKey = HexUtils.toHexString(privateKeyBytes);

            walletFile = KeystoreUtil.createStandard(password, master);
        } catch (CipherException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (walletFile == null) {
            return null;
        }

        String keystore = GsonUtils.toJson(walletFile);
        String address = "0x" + walletFile.getAddress();
        address = getChecksumAddress(address);

        Wallet wallet = new Wallet();
        wallet.setWords(mnemonicWords);
        wallet.setKeystore(keystore);
        wallet.setAddress(address);
        wallet.setPrivateKey(privateKey);

        return wallet;
    }

    /**
     * Wallet signature of information
     *
     * @param keystore Wallet keystore JSON format
     * @param password keystore password
     * @param message  Pending signature information
     * @return Signature information of byte [] of length 65
     * @throws CipherException
     * @throws ValidationException
     */
    public static byte[] sign(String keystore, String password,
                              byte[] message) throws CipherException, ValidationException {

        if (keystore == null || keystore.length() <= 0)
            return null;
        if (password == null || password.length() <= 0)
            return null;
        if (message == null || message.length < 1)
            return null;

        Blake2b.Blake2b256 blake2b256 = new Blake2b.Blake2b256();
        message = blake2b256.digest(message);


        Keystore walletFile = KeystoreUtil.json2Keystore(keystore);
        KeyPair keyPair = KeystoreUtil.decrypt(password, walletFile);

        // 开始签名
        ECDSASignature ecdsaSignature = keyPair.sign(message);
        byte[] signatureBytes = ecdsaSignature.toByteArray();

        if (signatureBytes == null || signatureBytes.length < 65 || signatureBytes[64] >= 2)
            return null;

        return signatureBytes;
    }

    /**
     * Recovering the address of the signature through the original information and the signature information
     *
     * @param message   Original information
     * @param signature byte[] signature information of length 65
     * @return address
     */
    public static String recoverAddress(byte[] message, byte[] signature) {

        if (signature == null || signature.length != 65) return null;
        if (message == null) return null;

        Blake2b.Blake2b256 blake2b256 = new Blake2b.Blake2b256();
        byte[] hashBytes = blake2b256.digest(message);

        byte[] addressBytes = Recover.recoverAddress(hashBytes, signature);
        String address = HexUtils.toHexString(addressBytes);
        address = getChecksumAddress(address);

        return address;
    }

    /**
     * Get the address from the keystore
     *
     * @param keystoreJson keystore json format
     * @return address
     */
    public static String getAddress(String keystoreJson) {

        Keystore keystore = KeystoreUtil.json2Keystore(keystoreJson);
        if (keystore == null) return null;

        String address = "0x" + keystore.getAddress();
        address = getChecksumAddress(address);

        return address;
    }


    /**
     * Is the keystore in JSON format correct
     *
     * @param keystore wallet JSON encryption format
     * @return Is the format correct?
     */
    public static boolean isValidKeystore(String keystore) {

        if (keystore == null || keystore.length() <= 0) return false;

        Keystore walletFile = KeystoreUtil.json2Keystore(keystore);
        if (walletFile == null) return false;

        try {
            KeystoreUtil.validate(walletFile);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * Whether mnemonic words are valid
     *
     * @param words The number of mnemonic words is limited to 12,15,18,21,24
     * @return is valid
     */
    public static boolean isValidMnemonicWords(List<String> words) {
        if (words == null) return false;

        List<String> copyWords = new ArrayList<>();
        for (String item : words) {
            if (item == null)
                return false;
            String word = item.trim();
            if (TextUtils.isEmpty(word))
                return false;
            copyWords.add(word);
        }
        int size = copyWords.size();
        if (size < 12 || size > 24) return false;

        try {
            MnemonicCode mnemonicCode = new MnemonicCode();
            mnemonicCode.check(copyWords);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * Verify that the password callback interface is correct
     */
    public interface OnVerifyPasswordCallback {
        /**
         * Verify keystroe password callback results
         *
         * @param success Is the password correct?
         */
        void onCheckPassword(boolean success);
    }


    /**
     * Verify that the wallet keystore password is correct
     *
     * @param keystore Wallet JSON encryption format
     * @param password Wallet password
     * @param callback Verification Result Callback Interface
     */
    public static void verifyKeystorePassword(String keystore, String password, OnVerifyPasswordCallback callback) {

        Keystore keystoreObject = KeystoreUtil.json2Keystore(keystore);
        if (keystoreObject != null && !TextUtils.isEmpty(password)) {
            verifyKeystorePassword(keystoreObject, password, callback);
        } else {
            if (callback != null)
                callback.onCheckPassword(false);
        }
    }

    private static void verifyKeystorePassword(final Keystore keystore, final String password,
                                               final OnVerifyPasswordCallback callback) {
        RxUtils.onFlowable(new RxUtils.BusinessCallBack<String, Boolean>() {

            @Override
            public Boolean map(String p) {

                return verifyKeystorePassword(keystore, password);
            }

            @Override
            public void onSuccess(Boolean success) {
                if (callback != null)
                    callback.onCheckPassword(success);
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (callback != null)
                    callback.onCheckPassword(false);
            }
        }, password);

    }

    private static boolean verifyKeystorePassword(final Keystore keystore, final String password) {
        if (keystore == null || TextUtils.isEmpty(password)) return false;
        boolean status = false;
        try {

            status = KeystoreUtil.isKeystorePassword(password, keystore);

        } catch (CipherException e) {
            e.printStackTrace();
            status = false;
        } catch (Exception e) {
            e.printStackTrace();
            status = false;
        }

        return status;
    }

    public interface DecryptKeystoreCallback {
        void decryptResult(String privateKey);
    }

    /**
     * keystore decrypt the private key
     *
     * @param keystoreJson
     * @param password
     * @param callback
     */
    public static void decryptKeystore(final String keystoreJson, final String password,
                                       final DecryptKeystoreCallback callback) {

        if (TextUtils.isEmpty(keystoreJson) || TextUtils.isEmpty(password)) {
            if (callback != null) callback.decryptResult(null);
            return;
        }

        RxUtils.onFlowable(new RxUtils.BusinessCallBack<String, String>() {

            @Override
            public String map(String s) {

                Keystore oldKeystore = KeystoreUtil.json2Keystore(keystoreJson);

                boolean isOk = verifyKeystorePassword(oldKeystore, password);
                String privateKey = null;
                if (isOk) {
                    try {
                        KeyPair keyPair = KeystoreUtil.decrypt(password, oldKeystore);
                        privateKey = keyPair.getPrivateKey();
                    } catch (Exception e) {
                        e.printStackTrace();
                        privateKey = null;
                    }
                }
                return privateKey;
            }

            @Override
            public void onSuccess(String privateKey) {
                if (callback != null) callback.decryptResult(privateKey);
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (callback != null) callback.decryptResult(null);
            }
        }, password);

    }

    public interface EncryptPrivateKeyCallback {
        void encryptPrivateKeyResult(String keystoreJson);
    }


    /**
     * Private key encryption generates keystore
     *
     * @param privateKey
     * @param password
     * @param callback
     */
    public static void encryptPrivateKey(final String privateKey, final String password,
                                         final EncryptPrivateKeyCallback callback) {

        if (TextUtils.isEmpty(privateKey) || TextUtils.isEmpty(password)) {
            if (callback != null) callback.encryptPrivateKeyResult(null);
            return;
        }

        RxUtils.onFlowable(new RxUtils.BusinessCallBack<String, String>() {

            @Override
            public String map(String key) {

                if (!StringUtils.isHexString(key)) {
                    return null;
                }
                byte[] bytes = HexUtils.hexStringToByteArray(key);
                Keystore keystoreObject = null;
                try {
                    KeyPair keyPair = new ThorKeyPair(bytes);
                    keystoreObject = KeystoreUtil.createStandard(password, keyPair);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (keystoreObject == null) {
                    return null;
                } else {
                    String keystoreString = GsonUtils.toString(keystoreObject);
                    return keystoreString;
                }
            }

            @Override
            public void onSuccess(String s) {
                if (callback != null) callback.encryptPrivateKeyResult(s);
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (callback != null) callback.encryptPrivateKeyResult(null);
            }
        }, privateKey);


    }

    /**
     * Modify the password callback interface of the wallet keystore
     */
    public interface OnModifyKeystorePasswordCallback {
        /**
         * Modify keystore password results
         *
         * @param newKeystore to generate new keystore by a new password
         */
        void onModifyResult(String newKeystore);
    }


    /**
     * Modify the wallet keytore password
     *
     * @param keystore    old wallet keystore
     * @param oldPassword old password
     * @param newPassword new password
     * @param callback    Modify password result callback interface
     */
    public static void modifyKeystorePassword(final String keystore, final String oldPassword,
                                              final String newPassword,
                                              final OnModifyKeystorePasswordCallback callback) {

        if (TextUtils.isEmpty(keystore) || TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
            if (callback != null)
                callback.onModifyResult(null);
            return;
        }

        RxUtils.onFlowable(new RxUtils.BusinessCallBack<String, String>() {

            @Override
            public String map(String s) {

                Keystore oldKeystore = KeystoreUtil.json2Keystore(keystore);

                boolean isOk = verifyKeystorePassword(oldKeystore, oldPassword);
                Keystore keystoreObject = null;
                if (isOk) {
                    try {
                        KeyPair keyPair = KeystoreUtil.decrypt(oldPassword, oldKeystore);
                        keystoreObject = KeystoreUtil.createStandard(newPassword, keyPair);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (keystoreObject == null) {
                    return null;
                } else {
                    String keystoreString = GsonUtils.toString(keystoreObject);
                    return keystoreString;
                }
            }

            @Override
            public void onSuccess(String keystoreString) {
                if (callback != null)
                    callback.onModifyResult(keystoreString);
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (callback != null)
                    callback.onModifyResult(null);
            }
        }, "");

    }

    /**
     * Generate wallet addresses with checksum formats
     *
     * @param address wallet address
     * @return checksum address
     */
    public static String getChecksumAddress(String address) {
        if (address == null || address.length() != 42) return null;
        if (!StringUtils.isHexString(address)) return null;
        return "0x" + KeystoreUtil.getChecksumAddress(address);
    }


    public interface OnSignCallback {
        void onSignResult(String result);
    }

    public static void signAndSendTransfer(final String keystoreJson, final String password, final TransferParameter transferParameter, final OnSignCallback callback) {

        if (TextUtils.isEmpty(keystoreJson) || TextUtils.isEmpty(password) || transferParameter == null) {
            if (callback != null) callback.onSignResult(null);
            return;
        }

        String signer = getAddress(keystoreJson);
        if (TextUtils.isEmpty(signer)) {
            if (callback != null) callback.onSignResult(null);
            return;
        }

        long gas = transferParameter.getGas();
        if (gas <= 0) {
            signer = signer.toLowerCase();
            //need simulate to call then get the gas
            simulateCallTransfer(signer, transferParameter.getClauses(), new OnMiningCostNotify() {
                @Override
                public void onMiningCost(long gas) {

                    transferParameter.setGas(gas);

                    callSignTransferThenSend(keystoreJson, password, transferParameter, callback);
                }
            });
        } else {

            callSignTransferThenSend(keystoreJson, password, transferParameter, callback);
        }

    }

    private static void callSignTransferThenSend(String keystoreJson, String password, TransferParameter transferParameter, OnSignCallback callback) {
        RxUtils.onFlowable(new RxUtils.BusinessCallBack<String, String[]>() {

            @Override
            public String[] map(String json) {

                String[] result = signTransfer(json, password, transferParameter);

                return result;
            }

            @Override
            public void onSuccess(String[] result) {
                if (result != null && result[0] != null) {
                    sendTransfer(result, callback);
                } else {
                    if (callback != null) callback.onSignResult(null);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (callback != null) callback.onSignResult(null);

            }
        }, keystoreJson);
    }


    public static void signTransfer(final String keystoreJson, final String password, final TransferParameter transferParameter,
                                    final OnSignCallback callback) {

        if (TextUtils.isEmpty(keystoreJson) || TextUtils.isEmpty(password) || transferParameter == null) {
            if (callback != null) callback.onSignResult(null);
            return;
        }

        String signer = getAddress(keystoreJson);
        if (TextUtils.isEmpty(signer)) {
            if (callback != null) callback.onSignResult(null);
            return;
        }

        long gas = transferParameter.getGas();
        if (gas <= 0) {
            signer = signer.toLowerCase();
            //need simulate to call then get the gas
            simulateCallTransfer(signer, transferParameter.getClauses(), new OnMiningCostNotify() {
                @Override
                public void onMiningCost(long gas) {

                    transferParameter.setGas(gas);

                    callSignTransfer(keystoreJson, password, transferParameter, callback);
                }
            });
        } else {

            callSignTransfer(keystoreJson, password, transferParameter, callback);
        }

    }

    private static void callSignTransfer(String keystoreJson, String password, TransferParameter transferParameter, OnSignCallback callback) {
        RxUtils.onFlowable(new RxUtils.BusinessCallBack<String, String>() {

            @Override
            public String map(String json) {

                String[] result = signTransfer(json, password, transferParameter);

                if (result != null && result.length > 1)
                    return result[0];
                else return null;
            }

            @Override
            public void onSuccess(String raw) {
                if (callback != null) callback.onSignResult(raw);
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (callback != null) callback.onSignResult(null);

            }
        }, keystoreJson);
    }

    private static String[] signTransfer(final String keystoreJson, final String password, final TransferParameter transferParameter) {


        Keystore keystore = KeystoreUtil.json2Keystore(keystoreJson);
        if (keystore == null) return null;

        String[] result = new String[2];

        boolean status = false;
        try {

            KeyPair keyPair = KeystoreUtil.decrypt(password, keystore);

            String rawAddress = keystore.getAddress();
            rawAddress = HexUtils.cleanHexPrefix(rawAddress);
            String walletAddress = keyPair.getAddress();
            rawAddress = rawAddress.toLowerCase();
            walletAddress = walletAddress.toLowerCase();
            if (rawAddress != null && walletAddress != null && rawAddress.equals(walletAddress)) {
                status = true;
            } else {
                status = false;
            }

            if (status) {
                ThorBuilder builder = new ThorBuilder();
                builder.setChainTag16(transferParameter.getChainTag())
                        .setBlockRef16(transferParameter.getBlockRef())
                        .setExpiration10(transferParameter.getExpiration())
                        .setClauseList(transferParameter.getClauses())
                        .setGasPriceCoef10(String.valueOf(transferParameter.getGasPriceCoef()))
                        .setGas10(String.valueOf(transferParameter.getGas()))
                        .setDependsOn16(transferParameter.getDependsOn())
                        .setNonce16(transferParameter.getNonce())
                        .setReserveds(transferParameter.getReserveds());

                ThorTransaction thorTransaction = new ThorTransaction(builder);
                byte[] data = thorTransaction.sign(keyPair);

                if (data != null) {
                    //Computing TxId
                    String localTxId = thorTransaction.getTransactionId(rawAddress);

                    String raw = HexUtils.toHexString(data);

                    result[0] = raw;
                    result[1] = localTxId;
                }
            }
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static void sendTransfer(final String[] sources, final OnSignCallback callback) {

        Api.sendTransaction(sources[0], new NetCallBack() {
            @Override
            public void onSuccess(Object result) {
                if (result != null && result instanceof SendTransactionResult) {
                    SendTransactionResult transactionResult = (SendTransactionResult) result;
                    String txId = transactionResult.getId();
                    if (callback != null) callback.onSignResult(txId);
                } else {
                    if (callback != null) callback.onSignResult(sources[1]);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (callback != null) callback.onSignResult(sources[1]);
            }
        }, null);
    }


    public interface OnGetChainTagCallback {

        void onGetChainTagResult(String chainTag);
    }

    /**
     * Get Genesis Block ChainTag
     *
     * @param callback
     */
    public static void getChainTag(final OnGetChainTagCallback callback) {

        String blockId = "0";

        Api.getBlock(blockId, new NetCallBack() {

            @Override
            public void onSuccess(Object result) {

                if (result != null && result instanceof Block) {
                    Block block = (Block) result;
                    String id = block.getId();
                    int size = id.length();
                    //The last byte of the Block ID of the Genesis Block
                    String chainTag = "0x" + id.substring(size - 2);
                    if (callback != null)
                        callback.onGetChainTagResult(chainTag);
                } else {
                    if (callback != null)
                        callback.onGetChainTagResult(null);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (callback != null)
                    callback.onGetChainTagResult(null);
            }
        }, null);
    }

    public interface OnGetBlockReferenceCallback {
        void onGetBlockReferenceResult(String blockRef);
    }

    /**
     * get blockReference
     *
     * @param callback
     */
    public static void getBlockReference(final OnGetBlockReferenceCallback callback) {

        Api.getBlock("best", new NetCallBack() {

            @Override
            public void onSuccess(Object result) {

                if (result != null && result instanceof Block) {
                    Block block = (Block) result;
                    String id = block.getId();
                    id = HexUtils.cleanHexPrefix(id);
                    int size = id.length();
                    String blockReference = null;
                    //The first eight bytes of block ID
                    if (size >= 16) {
                        blockReference = "0x" + id.substring(0, 16);
                    } else {
                        blockReference = "0x" + id;
                    }
                    if (callback != null)
                        callback.onGetBlockReferenceResult(blockReference);
                } else {
                    if (callback != null)
                        callback.onGetBlockReferenceResult(null);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (callback != null)
                    callback.onGetBlockReferenceResult(null);
            }
        }, null);
    }

    private interface OnMiningCostNotify {
        void onMiningCost(long gas);
    }

    private static void simulateCallTransfer(String signer, List<Clause> clauses, OnMiningCostNotify notify) {

        BatchCallData batchCallData = new BatchCallData();
        batchCallData.setClauses(clauses);
        batchCallData.setCaller(signer);

        NetCallBack callBack = new NetCallBack() {
            @Override
            public void onSuccess(Object result) {
                if (result != null && result instanceof List) {

                    List<CallResult> callResults = (List) result;
                    long execGas = 0;
                    if (!callResults.isEmpty()) {
                        for (CallResult item : callResults) {
                            execGas += item.getGasUsed();
                        }
                    }

                    if (execGas > 0) execGas += GasUtil.getOFFSET();
                    long intrinsicGas = GasUtil.intrinsicGas(clauses);

                    long simulateGas = execGas + intrinsicGas;

                    if (notify != null) notify.onMiningCost(simulateGas);

                } else {
                    if (notify != null) notify.onMiningCost(0);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (notify != null) notify.onMiningCost(0);
            }
        };

        Api.simulateBatchContractCall(batchCallData, null, callBack, null);
    }

    /**
     * Add the signature address to the authentication signature data
     *
     * @param message : Authentication signature data
     * @param signer  : wallet address
     */
    public static String addSigner(String message, String signer) {

        if (TextUtils.isEmpty(message) || TextUtils.isEmpty(signer)) return message;
        String result = String.format(message, signer);
        return result;
    }


    public static String getVersion() {
        return Version.VERSION;
    }

    /**
     * wallet object
     */
    public static class Wallet {
        /**
         * Mnemonic word
         */
        private List<String> words;
        /**
         * Wallet private key encryption JSON format
         */
        private String keystore;
        /**
         * wallet address
         */
        private String address;
        /**
         * Wallet private key hex format string
         */
        private String privateKey;

        public List<String> getWords() {
            return words;
        }

        public void setWords(List<String> words) {
            this.words = words;
        }

        public String getKeystore() {
            return keystore;
        }

        public void setKeystore(String keystore) {
            this.keystore = keystore;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }
    }
}
