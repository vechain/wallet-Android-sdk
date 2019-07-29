package com.vechain.wallet.bip39;


public enum MnemonicWordNumber {
    /**
     * 3 mnemonic word
     */
    THREE(27),
    /**
     * 6 mnemonic word
     */
    SIX(54),
    /**
     * 9 mnemonic word
     */
    NINE(72),
    /**
     * 12 mnemonic word
     */
    TWELVE(128),
    /**
     * 15 mnemonic word
     */
    FIFTEEN(160),
    /**
     * 18 mnemonic word
     */
    EIGHTEEN(192),
    /**
     * 21 mnemonic word
     */
    TWENTY_ONE(224),
    /**
     * 24 mnemonic word
     */
    TWENTY_FOUR(256);

    private final int bitLength;

    MnemonicWordNumber(int bitLength) {
        this.bitLength = bitLength;
    }

    public int bitLength() {
        return bitLength;
    }

    public int byteLength() {
        return bitLength / 8;
    }
}
