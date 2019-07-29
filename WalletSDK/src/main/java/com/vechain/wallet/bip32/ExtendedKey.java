/*
 * Copyright 2013 bits of proof zrt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vechain.wallet.bip32;

import com.vechain.wallet.key.KeyPair;
import com.vechain.wallet.key.PublicKey;
import com.vechain.wallet.key.Key;

import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.Arrays;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Key Generator following BIP32 https://en.bitcoin.it/wiki/BIP_0032
 */
public class ExtendedKey {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final X9ECParameters curve = SECNamedCurves.getByName("secp256k1");

    private final Key master;
    private final byte[] chainCode;
    private final int depth;
    private final int parent;
    private final int sequence;

    private static final byte[] BITCOIN_SEED = "Bitcoin seed".getBytes();


    public static ExtendedKey create(byte[] seed) throws ValidationException {
        try {
            //https://android-developers.googleblog.com/2018/03/cryptography-changes-in-android-p.html
            //Mac mac = Mac.getInstance("HmacSHA512","BC");
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKey seedkey = new SecretKeySpec(BITCOIN_SEED, "HmacSHA512");
            mac.init(seedkey);
            byte[] lr = mac.doFinal(seed);
            byte[] l = Arrays.copyOfRange(lr, 0, 32);
            byte[] r = Arrays.copyOfRange(lr, 32, 64);
            BigInteger m = new BigInteger(1, l);
            if (m.compareTo(curve.getN()) >= 0) {
                throw new ValidationException("This is rather unlikely, but it did just happen");
            }
            KeyPair keyPair = new KeyPair(l, true);
            return new ExtendedKey(keyPair, r, 0, 0, 0);
        } catch (NoSuchAlgorithmException e) {
            throw new ValidationException(e);
        } catch (InvalidKeyException e) {
            throw new ValidationException(e);
        }
    }

    public static ExtendedKey createNew() {
        Key key = KeyPair.createNew(true);
        byte[] chainCode = new byte[32];
        RANDOM.nextBytes(chainCode);
        return new ExtendedKey(key, chainCode, 0, 0, 0);
    }

    public static ExtendedKey parsePrivateKey(byte[] bytes) throws ValidationException {
        Key key = new KeyPair(bytes, true);
        byte[] chainCode = new byte[32];
        RANDOM.nextBytes(chainCode);
        return new ExtendedKey(key, chainCode, 0, 0, 0);
    }

    public ExtendedKey(Key key, byte[] chainCode, int depth, int parent, int sequence) {
        this.master = key;
        this.chainCode = chainCode;
        this.parent = parent;
        this.depth = depth;
        this.sequence = sequence;
    }

    public Key getMaster() {
        return master;
    }

    public byte[] getChainCode() {
        return Arrays.clone(chainCode);
    }

    public int getDepth() {
        return depth;
    }

    public int getParent() {
        return parent;
    }

    public int getSequence() {
        return sequence;
    }

    public int getFingerPrint() {
        int fingerprint = 0;
        byte[] address = master.getRawAddress();
        for (int i = 0; i < 4; ++i) {
            fingerprint <<= 8;
            fingerprint |= address[i] & 0xff;
        }
        return fingerprint;
    }

    public Key getKey(int sequence) throws ValidationException {
        return generateKey(sequence).getMaster();
    }

    public ExtendedKey getChild(int sequence) throws ValidationException {
        ExtendedKey sub = generateKey(sequence);
        return new ExtendedKey(sub.getMaster(), sub.getChainCode(), sub.getDepth() + 1,
                getFingerPrint(), sequence);
    }

    public ExtendedKey getReadOnly() {
        return new ExtendedKey(new PublicKey(master.getRawPublicKey(), true), chainCode, depth,
                parent, sequence);
    }

    public boolean isReadOnly() {
        return master.getRawPrivateKey() == null;
    }

    private ExtendedKey generateKey(int sequence) throws ValidationException {
        try {
            if ((sequence & 0x80000000) != 0 && master.getRawPrivateKey() == null) {
                throw new ValidationException("need private key for private generation");
            }
            //https://android-developers.googleblog.com/2018/03/cryptography-changes-in-android-p.html
            //Mac mac = Mac.getInstance("HmacSHA512","BC");
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKey key = new SecretKeySpec(chainCode, "HmacSHA512");
            mac.init(key);

            byte[] extended;
            byte[] pub = master.getRawPublicKey();
            if ((sequence & 0x80000000) == 0) {
                extended = new byte[pub.length + 4];
                System.arraycopy(pub, 0, extended, 0, pub.length);
                extended[pub.length] = (byte) ((sequence >>> 24) & 0xff);
                extended[pub.length + 1] = (byte) ((sequence >>> 16) & 0xff);
                extended[pub.length + 2] = (byte) ((sequence >>> 8) & 0xff);
                extended[pub.length + 3] = (byte) (sequence & 0xff);
            } else {
                byte[] priv = master.getRawPrivateKey();
                extended = new byte[priv.length + 5];
                System.arraycopy(priv, 0, extended, 1, priv.length);
                extended[priv.length + 1] = (byte) ((sequence >>> 24) & 0xff);
                extended[priv.length + 2] = (byte) ((sequence >>> 16) & 0xff);
                extended[priv.length + 3] = (byte) ((sequence >>> 8) & 0xff);
                extended[priv.length + 4] = (byte) (sequence & 0xff);
            }
            byte[] lr = mac.doFinal(extended);
            byte[] l = Arrays.copyOfRange(lr, 0, 32);
            byte[] r = Arrays.copyOfRange(lr, 32, 64);

            BigInteger m = new BigInteger(1, l);
            if (m.compareTo(curve.getN()) >= 0) {
                throw new ValidationException("This is rather unlikely, but it did just happen");
            }
            if (master.getRawPrivateKey() != null) {
                BigInteger k = m.add(new BigInteger(1, master.getRawPrivateKey())).mod(curve.getN
                        ());
                if (k.equals(BigInteger.ZERO)) {
                    throw new ValidationException("This is rather unlikely, but it did just " +
                            "happen");
                }
                return new ExtendedKey(new KeyPair(k, true), r, depth, parent, sequence);
            } else {
                ECPoint q = curve.getG().multiply(m).add(curve.getCurve().decodePoint(pub));
                if (q.isInfinity()) {
                    throw new ValidationException("This is rather unlikely, but it did just " +
                            "happen");
                }
                pub = new ECPoint.Fp(curve.getCurve(), q.getX(), q.getY(), true).getEncoded();
                return new ExtendedKey(new PublicKey(pub, true), r, depth, parent, sequence);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new ValidationException(e);
        } catch (InvalidKeyException e) {
            throw new ValidationException(e);
        }
    }


}
