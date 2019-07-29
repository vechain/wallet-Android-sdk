package com.vechain.wallet.key;


import com.vechain.wallet.bip32.ValidationException;

import java.math.BigInteger;

public interface Key extends Cloneable {

    byte[] getRawPrivateKey();

    byte[] getRawPublicKey(boolean isCompressed);

    byte[] getRawPublicKey();

    byte[] getRawAddress();

    String getPrivateKey();

    String getPublicKey();

    BigInteger getBigIntegerPrivateKey();

    BigInteger getBigIntegerPublicKey();

    String getAddress();

    boolean isCompressed();

    Key clone() throws CloneNotSupportedException;

    <T extends Object> T sign(byte[] messageHash) throws ValidationException;

}