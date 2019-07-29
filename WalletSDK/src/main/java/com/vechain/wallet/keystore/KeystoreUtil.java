package com.vechain.wallet.keystore;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.lambdaworks.crypto.SCrypt;
import com.vechain.wallet.key.KeyPair;
import com.vechain.wallet.network.utils.GsonUtils;
import com.vechain.wallet.key.ThorKeyPair;
import com.vechain.wallet.utils.Hash;
import com.vechain.wallet.utils.HexUtils;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KeystoreUtil {

    private static SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int N_LIGHT = 1 << 12;
    private static final int P_LIGHT = 6;

    private static final int N_STANDARD = 1 << 18;
    private static final int P_STANDARD = 1;

    private static final int R = 8;
    private static final int DKLEN = 32;

    private static final int CURRENT_VERSION = 3;

    private static final String CIPHER = "aes-128-ctr";
    static final String AES_128_CTR = "pbkdf2";
    static final String SCRYPT = "scrypt";

    public static Keystore create(String password, KeyPair keyPair, int n, int p)
            throws CipherException {

        byte[] salt = generateRandomBytes(32);

        byte[] derivedKey = generateDerivedScryptKey(
                password.getBytes(Charset.forName("UTF-8")), salt, n, R, p, DKLEN);

        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);
        byte[] iv = generateRandomBytes(16);

        byte[] privateKeyBytes =
                HexUtils.toBytesPadded(keyPair.getBigIntegerPrivateKey(), KeyTool.PRIVATE_KEY_SIZE);

        byte[] cipherText = performCipherOperation(
                Cipher.ENCRYPT_MODE, iv, encryptKey, privateKeyBytes);

        byte[] mac = generateMac(derivedKey, cipherText);

        return createWalletFile(keyPair, cipherText, iv, salt, mac, n, p);
    }

    public static Keystore createStandard(String password, KeyPair keyPair)
            throws CipherException {
        return create(password, keyPair, N_STANDARD, P_STANDARD);
    }

//    public static Keystore createLight(String password, ECKeyPair ecKeyPair)
//            throws CipherException {
//        return create(password, ecKeyPair, N_LIGHT, P_LIGHT);
//    }

    private static Keystore createWalletFile(
            KeyPair keyPair, byte[] cipherText, byte[] iv, byte[] salt, byte[] mac,
            int n, int p) {

        Keystore walletFile = new Keystore();
        walletFile.setAddress(KeyTool.getAddress(keyPair.getBigIntegerPublicKey()));

        Keystore.Crypto crypto = new Keystore.Crypto();
        crypto.setCipher(CIPHER);
        crypto.setCiphertext(HexUtils.toHexStringNoPrefix(cipherText));
        walletFile.setCrypto(crypto);

        Keystore.CipherParams cipherParams = new Keystore.CipherParams();
        cipherParams.setIv(HexUtils.toHexStringNoPrefix(iv));
        crypto.setCipherparams(cipherParams);

        crypto.setKdf(SCRYPT);
        Keystore.ScryptKdfParams kdfParams = new Keystore.ScryptKdfParams();
        kdfParams.setDklen(DKLEN);
        kdfParams.setN(n);
        kdfParams.setP(p);
        kdfParams.setR(R);
        kdfParams.setSalt(HexUtils.toHexStringNoPrefix(salt));
        crypto.setKdfparams(kdfParams);

        crypto.setMac(HexUtils.toHexStringNoPrefix(mac));
        walletFile.setCrypto(crypto);
        walletFile.setId(UUID.randomUUID().toString());
        walletFile.setVersion(CURRENT_VERSION);

        return walletFile;
    }

    private static byte[] generateDerivedScryptKey(
            byte[] password, byte[] salt, int n, int r, int p, int dkLen) throws CipherException {
        try {
            return SCrypt.scrypt(password, salt, n, r, p, dkLen);
        } catch (GeneralSecurityException e) {
            throw new CipherException(e);
        }
    }

    private static byte[] generateAes128CtrDerivedKey(
            byte[] password, byte[] salt, int c, String prf) throws CipherException {

        if (!prf.equals("hmac-sha256")) {
            throw new CipherException("Unsupported prf:" + prf);
        }

        // Java 8 supports this, but you have to convert the password to a character array, see
        // http://stackoverflow.com/a/27928435/3211687

        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(password, salt, c);
        return ((KeyParameter) gen.generateDerivedParameters(256)).getKey();
    }

    private static byte[] performCipherOperation(
            int mode, byte[] iv, byte[] encryptKey, byte[] text) throws CipherException {

        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

            SecretKeySpec secretKeySpec = new SecretKeySpec(encryptKey, "AES");
            cipher.init(mode, secretKeySpec, ivParameterSpec);
            return cipher.doFinal(text);
        } catch (NoSuchPaddingException e) {
            return throwCipherException(e);
        } catch (NoSuchAlgorithmException e) {
            return throwCipherException(e);
        } catch (InvalidAlgorithmParameterException e) {
            return throwCipherException(e);
        } catch (InvalidKeyException e) {
            return throwCipherException(e);
        } catch (BadPaddingException e) {
            return throwCipherException(e);
        } catch (IllegalBlockSizeException e) {
            return throwCipherException(e);
        }
    }

    private static byte[] throwCipherException(Exception e) throws CipherException {
        throw new CipherException("Error performing cipher operation", e);
    }

    private static byte[] generateMac(byte[] derivedKey, byte[] cipherText) {
        byte[] result = new byte[16 + cipherText.length];

        System.arraycopy(derivedKey, 16, result, 0, 16);
        System.arraycopy(cipherText, 0, result, 16, cipherText.length);

        return Hash.keccak256(result);
    }

    public static KeyPair decrypt(String password, Keystore walletFile)
            throws CipherException {

        validate(walletFile);

        Keystore.Crypto crypto = walletFile.getCrypto();

        byte[] mac = HexUtils.hexStringToByteArray(crypto.getMac());
        byte[] iv = HexUtils.hexStringToByteArray(crypto.getCipherparams().getIv());
        byte[] cipherText = HexUtils.hexStringToByteArray(crypto.getCiphertext());

        byte[] derivedKey;

        Keystore.KdfParams kdfParams = crypto.getKdfparams();
        if (kdfParams instanceof Keystore.ScryptKdfParams) {
            Keystore.ScryptKdfParams scryptKdfParams =
                    (Keystore.ScryptKdfParams) crypto.getKdfparams();
            int dklen = scryptKdfParams.getDklen();
            int n = scryptKdfParams.getN();
            int p = scryptKdfParams.getP();
            int r = scryptKdfParams.getR();
            byte[] salt = HexUtils.hexStringToByteArray(scryptKdfParams.getSalt());
            derivedKey = generateDerivedScryptKey(
                    password.getBytes(Charset.forName("UTF-8")), salt, n, r, p, dklen);
        } else if (kdfParams instanceof Keystore.Aes128CtrKdfParams) {
            Keystore.Aes128CtrKdfParams aes128CtrKdfParams =
                    (Keystore.Aes128CtrKdfParams) crypto.getKdfparams();
            int c = aes128CtrKdfParams.getC();
            String prf = aes128CtrKdfParams.getPrf();
            byte[] salt = HexUtils.hexStringToByteArray(aes128CtrKdfParams.getSalt());

            derivedKey = generateAes128CtrDerivedKey(
                    password.getBytes(Charset.forName("UTF-8")), salt, c, prf);
        } else {
            throw new CipherException("Unable to deserialize params: " + crypto.getKdf());
        }

        byte[] derivedMac = generateMac(derivedKey, cipherText);

        if (!Arrays.equals(derivedMac, mac)) {
            throw new CipherException("Invalid password provided");
        }

        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);
        byte[] privateKey = performCipherOperation(Cipher.DECRYPT_MODE, iv, encryptKey, cipherText);

        return new ThorKeyPair(HexUtils.toBigInt(privateKey));//ECKeyPair.create(privateKey);
    }

    public static void validate(Keystore walletFile) throws CipherException {
        Keystore.Crypto crypto = walletFile.getCrypto();

        if (walletFile.getVersion() != CURRENT_VERSION) {
            throw new CipherException("KeystoreUtil version is not supported");
        }

        if (!crypto.getCipher().equals(CIPHER)) {
            throw new CipherException("KeystoreUtil cipher is not supported");
        }

        if (!crypto.getKdf().equals(AES_128_CTR) && !crypto.getKdf().equals(SCRYPT)) {
            throw new CipherException("KDF type is not supported");
        }
    }

    static byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }


    public static String getChecksumAddress(String address){

        address = address.toLowerCase();
        //clean 0x
        address = HexUtils.cleanHexPrefix(address);
        address = address.toLowerCase();

        //sha3
        byte[] bytes = Hash.keccak256(address.getBytes());

        StringBuffer buffer = new StringBuffer();
        //buffer.append("0x");
        String hex = HexUtils.toHexStringNoPrefix(bytes);

        char[] chars = hex.toCharArray();
        int size = address.length();

        char[] raws = address.toCharArray();

        for (int i = 0; i < size; i++) {
            if (parseInt(chars[i]) >= 8) {
                buffer.append((""+raws[i]).toUpperCase());

            } else {
                buffer.append(raws[i]);
            }
        }

        return buffer.toString();
    }

    private static int parseInt(char value){
        if(value>='a' && value<='f'){
            return 9 + (value - 'a'+1);
        }else {
            return value - '0';
        }
    }

    public static boolean toChecksumAddress(String address){
        String ruleAddress = getChecksumAddress(address);
        return address.equals(ruleAddress);
    }


    public static boolean isKeystorePassword(String password, Keystore walletFile) throws CipherException{

        //1. The user enters the password `password `+ `salt `+ `n `+ `R `+ `p `+ `dklen ` to encrypt `scrypt', and gets `derivedkey'.`
        //2. ` derivedkey `+ `iv `+ `private key `encrypted by `aes128 ` yields `ciphertext', with a length of 64 bits
        //3. ` Derived key `+ `ciphertext'(encrypted private key) is encrypted by `sha3-256', resulting in `Mac', 64 bits in length.
        // Use step 1 + step 3 to determine whether the user's password is entered correctly.

        validate(walletFile);

        Keystore.Crypto crypto = walletFile.getCrypto();

        byte[] mac = HexUtils.hexStringToByteArray(crypto.getMac());
        byte[] iv = HexUtils.hexStringToByteArray(crypto.getCipherparams().getIv());
        byte[] cipherText = HexUtils.hexStringToByteArray(crypto.getCiphertext());

        byte[] derivedKey;

        Keystore.KdfParams kdfParams = crypto.getKdfparams();
        if (kdfParams instanceof Keystore.ScryptKdfParams) {
            Keystore.ScryptKdfParams scryptKdfParams =
                    (Keystore.ScryptKdfParams) crypto.getKdfparams();
            int dklen = scryptKdfParams.getDklen();
            int n = scryptKdfParams.getN();
            int p = scryptKdfParams.getP();
            int r = scryptKdfParams.getR();
            byte[] salt = HexUtils.hexStringToByteArray(scryptKdfParams.getSalt());
            derivedKey = generateDerivedScryptKey(
                    password.getBytes(Charset.forName("UTF-8")), salt, n, r, p, dklen);
        } else if (kdfParams instanceof Keystore.Aes128CtrKdfParams) {
            Keystore.Aes128CtrKdfParams aes128CtrKdfParams =
                    (Keystore.Aes128CtrKdfParams) crypto.getKdfparams();
            int c = aes128CtrKdfParams.getC();
            String prf = aes128CtrKdfParams.getPrf();
            byte[] salt = HexUtils.hexStringToByteArray(aes128CtrKdfParams.getSalt());

            derivedKey = generateAes128CtrDerivedKey(
                    password.getBytes(Charset.forName("UTF-8")), salt, c, prf);
        } else {
            throw new CipherException("Unable to deserialize params: " + crypto.getKdf());
        }

        byte[] derivedMac = generateMac(derivedKey, cipherText);

        if (!Arrays.equals(derivedMac, mac)) {
            return false;
        }
        return true;
    }


    /**
     * Wallet JSON encryption format converted to memory object
     *
     * @param keystoreJson json data
     * @return object
     */
    public static Keystore json2Keystore(String keystoreJson) {
        if (TextUtils.isEmpty(keystoreJson)) return null;
        Keystore keystore = null;
        keystoreJson = keystoreJson.toLowerCase();
        if (keystoreJson.contains("\"n\":")) {
            Type type = new TypeToken<Keystore<Keystore.ScryptKdfParams>>() {
            }.getType();
            keystore = GsonUtils.fromJson(keystoreJson, type);
        } else {
            Type type = new TypeToken<Keystore<Keystore.Aes128CtrKdfParams>>() {
            }.getType();
            keystore = GsonUtils.fromJson(keystoreJson, type);
        }

        return keystore;
    }

}
