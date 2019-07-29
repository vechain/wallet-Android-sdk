package com.vechain.wallet.key;


import com.vechain.wallet.utils.Hash;

import org.spongycastle.util.Arrays;

import java.math.BigInteger;

public class PublicKey implements Key {
    private byte[] pub;
    private boolean compressed;

    public PublicKey(byte[] pub, boolean compressed) {
        this.pub = pub;
        this.compressed = compressed;
    }

    @Override
    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    @Override
    public byte[] getRawPrivateKey() {
        throw new RuntimeException("Please use private key to sign signature");
    }

    @Override
    public byte[] getRawPublicKey(boolean isCompressed) {
        if (!isCompressed) {
            throw new RuntimeException("No compressed public key");
        }
        return Arrays.clone(pub);
    }

    @Override
    public byte[] getRawPublicKey() {
        return Arrays.clone(pub);
    }

    @Override
    public byte[] getRawAddress() {
        return Hash.hash160(pub);
    }

    @Override
    public String getPrivateKey() {
        throw new RuntimeException("Please use private key to sign signature");
    }

    @Override
    public String getPublicKey() {
        throw new RuntimeException("No formatted public Key");
    }

    public BigInteger getBigIntegerPrivateKey(){
        throw new RuntimeException("Please use private key to sign signature");
    }

    public BigInteger getBigIntegerPublicKey(){

        throw new RuntimeException("No formatted public Key");
    }

    @Override
    public String getAddress() {
        throw new RuntimeException("No formatted address");
    }

    @Override
    public PublicKey clone() throws CloneNotSupportedException {
        PublicKey c = (PublicKey) super.clone();
        c.pub = Arrays.clone(pub);
        return c;
    }

    @Override
    public <T> T sign(byte[] messageHash) {
        throw new RuntimeException("Please use private key to sign signature");
    }

}
