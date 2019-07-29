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

import android.util.Log;

import com.vechain.wallet.network.NodeUrl;
import com.vechain.wallet.network.bean.common.Token;
import com.vechain.wallet.thor.tx.Clause;
import com.vechain.wallet.thor.tx.ParameterException;
import com.vechain.wallet.thor.tx.TransferParameter;
import com.vechain.wallet.utils.HexUtils;
import com.vechain.wallet.utils.StringUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class WalletUtilsTest {

    private final static String TAG = "VeChain";

    @Test
    public void createWallet() {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        WalletUtils.OnCreateWalletCallback callback = new WalletUtils.OnCreateWalletCallback() {
            @Override
            public void onCreate(WalletUtils.Wallet wallet) {

                Log.v(TAG, wallet.getAddress());
                assertEquals(true, wallet != null);

                countDownLatch.countDown();
            }
        };

        WalletUtils.createWallet("12345678", callback);

        try {
            countDownLatch.await();
        } catch (Exception e) {

        }
    }

    @Test
    public void createWallet1() {


        final CountDownLatch countDownLatch = new CountDownLatch(1);

        String words = "enact again rate alone congress scheme solid theory flush length twenty head";
        List<String> mnemonicWords = new ArrayList<>();
        String[] strings = words.split(" ");

        for (String item : strings) {
            mnemonicWords.add(item);
        }

        WalletUtils.OnCreateWalletCallback callback = new WalletUtils.OnCreateWalletCallback() {
            @Override
            public void onCreate(WalletUtils.Wallet wallet) {

                assertEquals(true, wallet != null);
                countDownLatch.countDown();

            }
        };

        WalletUtils.createWallet(mnemonicWords, "12345678", callback);

        try {
            countDownLatch.await();
        } catch (Exception e) {

        }
    }

    @Test
    public void sign() {

        String keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";

        byte[] signature = null;

        try {
            signature = WalletUtils.sign(keystoreJson, "123456", "dkfjalsdjfk".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.v(TAG, HexUtils.toHexString(signature));

        assertEquals(65, signature.length);
    }

    @Test
    public void recoverAddress() {

        String signature = "0x4eb1ae9254217b356b2958ab0b7a02e72f6fa86858240ca4998f74ef8a0fd68155e71ba8bd15625dc3d5e0c89c021f3852070d290688a65ba7e1d608a03d6e8400";
        byte[] bytes = HexUtils.hexStringToByteArray(signature);


        String address = WalletUtils.recoverAddress("dkfjalsdjfk".getBytes(), bytes);

        address = address.toLowerCase();

        assertEquals("0x36d7189625587d7c4c806e0856b6926af8d36fea", address);

    }

    @Test
    public void getAddress() {

        String keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";

        String address = WalletUtils.getAddress(keystoreJson);

        address = address.toLowerCase();

        assertEquals("0x36d7189625587d7c4c806e0856b6926af8d36fea", address);
    }

    @Test
    public void isValidKeystore() {

        String keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";

        boolean isValid = WalletUtils.isValidKeystore(keystoreJson);


        assertEquals(true, isValid);
    }

    @Test
    public void isValidMnemonicWords() {

        String words = "enact again rate alone congress scheme solid theory flush length twenty head";
        List<String> mnemonicWords = new ArrayList<>();
        String[] strings = words.split(" ");

        for (String item : strings) {
            mnemonicWords.add(item);
        }
        boolean isValid = WalletUtils.isValidMnemonicWords(mnemonicWords);

        assertEquals(true, isValid);
    }

    @Test
    public void verifyKeystorePassword() {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        String keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";

        WalletUtils.verifyKeystorePassword(keystoreJson, "123456", new WalletUtils.OnVerifyPasswordCallback() {
            @Override
            public void onCheckPassword(boolean success) {

                assertEquals(true, success);
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (Exception e) {

        }

    }

    @Test
    public void decryptKeystore() {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        String keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";

        WalletUtils.decryptKeystore(keystoreJson, "123456", new WalletUtils.DecryptKeystoreCallback() {
            @Override
            public void decryptResult(String privateKey) {

                Log.v(TAG, privateKey);
                assertEquals(true, privateKey != null);

                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (Exception e) {

        }
    }

    @Test
    public void encryptPrivateKey() {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        String privateKey = "0xbc9fe2428a8faec37674412c113f4a9a66b2e40076014547bfe7bbdc2c5a85ee";

        WalletUtils.encryptPrivateKey(privateKey, "123", new WalletUtils.EncryptPrivateKeyCallback() {
            @Override
            public void encryptPrivateKeyResult(String keystoreJson) {

                String address = WalletUtils.getAddress(keystoreJson);
                address = address.toLowerCase();

                assertEquals("0x36d7189625587d7c4c806e0856b6926af8d36fea", address);
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (Exception e) {

        }

    }

    @Test
    public void modifyKeystorePassword() {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        String keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";

        WalletUtils.modifyKeystorePassword(keystoreJson, "123456", "123", new WalletUtils.OnModifyKeystorePasswordCallback() {
            @Override
            public void onModifyResult(String newKeystore) {

                String address = WalletUtils.getAddress(newKeystore);
                address = address.toLowerCase();

                assertEquals("0x36d7189625587d7c4c806e0856b6926af8d36fea", address);
                countDownLatch.countDown();
            }
        });


        try {
            countDownLatch.await();
        } catch (Exception e) {

        }
    }

    @Test
    public void getChecksumAddress() {

        String address = "0x36d7189625587d7c4c806e0856b6926af8d36fea";

        String checkSumAddress = WalletUtils.getChecksumAddress(address);


        assertEquals("0x36D7189625587D7C4c806E0856b6926Af8d36FEa", checkSumAddress);

    }

    private String testChainTag;
    private String testBlockRef;
    @Test
    public void signAndSendTransfer() {

        testChainTag = null;
        testBlockRef = null;

        final CountDownLatch countDownLatch = new CountDownLatch(2);
        WalletUtils.setNodeUrl(NodeUrl.TEST_NODE);

        WalletUtils.getChainTag(new WalletUtils.OnGetChainTagCallback() {
            @Override
            public void onGetChainTagResult(String chainTag) {
                testChainTag = chainTag;
                countDownLatch.countDown();
            }
        });



        WalletUtils.getBlockReference(new WalletUtils.OnGetBlockReferenceCallback() {
            @Override
            public void onGetBlockReferenceResult(String blockRef) {

                testBlockRef = blockRef;
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (Exception e) {

        }


        Clause clause = getTokenParams();
        long gas = 80000;

        List<Clause> clauseList = new ArrayList<>();
        clauseList.add(clause);

        //WalletUtils.signAndSendTransfer();

        String hexNonce = null;
        byte[] nonceBytes = new byte[8];
        Random random = new Random();
        random.nextBytes(nonceBytes);
        hexNonce = HexUtils.toHexString(nonceBytes);

        TransferParameter.Builder builder = new TransferParameter.Builder();
        TransferParameter transferParameter = null;
        try {
            transferParameter = builder.setClauseList(clauseList)
                    .setGas(gas)
                    .setChainTag(testChainTag)
                    .setBlockRef(testBlockRef)
                    .setNonce(hexNonce)
                    .build();
        } catch (ParameterException e) {
            e.printStackTrace();

            assertEquals("",null);
        }

        String keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";

        final CountDownLatch transferDownLatch = new CountDownLatch(1);

        WalletUtils.signAndSendTransfer(keystoreJson, "123456", transferParameter, new WalletUtils.OnSignCallback() {
            @Override
            public void onSignResult(String result) {

                String txId = result;

                assertEquals(true,txId!=null);
                transferDownLatch.countDown();
            }
        });

        try {
            transferDownLatch.await();
        } catch (Exception e) {

        }
    }

    @Test
    public void signTransfer() {

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        WalletUtils.setNodeUrl(NodeUrl.TEST_NODE);
        WalletUtils.getChainTag(new WalletUtils.OnGetChainTagCallback() {
            @Override
            public void onGetChainTagResult(String chainTag) {
                testChainTag = chainTag;
                countDownLatch.countDown();
            }
        });



        WalletUtils.getBlockReference(new WalletUtils.OnGetBlockReferenceCallback() {
            @Override
            public void onGetBlockReferenceResult(String blockRef) {

                testBlockRef = blockRef;
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (Exception e) {

        }


        Clause clause = getTokenParams();
        long gas = 80000;

        List<Clause> clauseList = new ArrayList<>();
        clauseList.add(clause);

        //WalletUtils.signAndSendTransfer();

        String hexNonce = null;
        byte[] nonceBytes = new byte[8];
        Random random = new Random();
        random.nextBytes(nonceBytes);
        hexNonce = HexUtils.toHexString(nonceBytes);

        TransferParameter.Builder builder = new TransferParameter.Builder();
        TransferParameter transferParameter = null;
        try {
            transferParameter = builder.setClauseList(clauseList)
                    .setGas(gas)
                    .setChainTag(testChainTag)
                    .setBlockRef(testBlockRef)
                    .setNonce(hexNonce)
                    .build();
        } catch (ParameterException e) {
            e.printStackTrace();

            assertEquals("",null);
        }

        String keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";

        final CountDownLatch transferDownLatch = new CountDownLatch(1);

        WalletUtils.signTransfer(keystoreJson, "123456", transferParameter, new WalletUtils.OnSignCallback() {
            @Override
            public void onSignResult(String result) {

                String raw = result;

                assertEquals(true,raw!=null);
                transferDownLatch.countDown();

            }
        });

        try {
            transferDownLatch.await();
        } catch (Exception e) {

        }
    }

    @Test
    public void getChainTag() {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        WalletUtils.setNodeUrl(NodeUrl.TEST_NODE);
        WalletUtils.getChainTag(new WalletUtils.OnGetChainTagCallback() {
            @Override
            public void onGetChainTagResult(String chainTag) {

                assertEquals("0x27", chainTag);
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (Exception e) {

        }
    }

    @Test
    public void getBlockReference() {

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        WalletUtils.setNodeUrl(NodeUrl.TEST_NODE);

        WalletUtils.getBlockReference(new WalletUtils.OnGetBlockReferenceCallback() {
            @Override
            public void onGetBlockReferenceResult(String blockRef) {

                assertEquals(18, blockRef.length());
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (Exception e) {

        }

    }


    private Clause getTokenParams() {

        Clause clause = new Clause();

        //token contract address
        clause.setTo("0x0000000000000000000000000000456e65726779");
        clause.setValue("0");


        String toAddress = "0xe002107ad60e83f7762105504fac50730805f3ab";
        toAddress = toAddress.toLowerCase();

        String amount = "1";
        //Convert to Wei value（amount*10^18）(hex values),The value of VTHO decimal is 18,
        // and other tokens need to be queried by the decimals () method of the token address.
        // blockChain node Url+/accounts/{token address}  send post data: {"value":"0x0","data":"0x313ce567"}
        //@see Api.getTokenDecimals(String tokenAddress, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider)
        String hexAmountWei = StringUtils.string2LargeHex(amount, Token.DECIMALS);


        String[] args = {toAddress, hexAmountWei};

        String data = getTransferABIEncode(args);
        clause.setData(data);

        return clause;
    }

    private String getTransferABIEncode(String[] args) {

        //token transfer: transfer(address,uint256)
        //Keccak-256("transfer(address,uint256)")="0xa9059cbb2ab09eb219583f4a59a5d0623ade346d962bcd4e46b11da047c9049b"
        //Take the first four bytes：0xa9059cbb
        //Reference resources：
        //1.https://www.4byte.directory/signatures/
        //2.https://github.com/vechain/VIPs/blob/master/VIP-180.md
        String tokenMethodId = "0xa9059cbb";
        StringBuffer buffer = new StringBuffer();
        buffer.append(tokenMethodId);

        for (String item : args) {
            buffer.append(getParamValue(item));
        }

        return buffer.toString();
    }

    private String getParamValue(String item) {
        int size = 0;
        if (item == null || item.isEmpty())
            size = 0;
        else
            size = item.length();
        String hexValue = "";

        if (size > 0) {
            //去掉0x开头
            hexValue = HexUtils.cleanHexPrefix(item);
            size = hexValue.length();
        }

        int count = 64;
        StringBuffer buffer = new StringBuffer();
        int n = count - size;
        if (n > 0) {
            for (int i = 0; i < n; i++)
                buffer.append("0");
        }
        buffer.append(hexValue);
        return buffer.toString();
    }

}