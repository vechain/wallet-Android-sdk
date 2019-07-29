package com.vechain.wallet.bip32;


public final class Index {
    Index() {
    }

    public static int hard(final int index) {
        return index | 0x80000000;
    }

    public static boolean isHardened(final int i) {
        return (i & 0x80000000) != 0;
    }
}
