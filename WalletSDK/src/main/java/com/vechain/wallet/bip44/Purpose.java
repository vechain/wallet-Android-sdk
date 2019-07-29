package com.vechain.wallet.bip44;

import com.vechain.wallet.bip32.Index;


public final class Purpose {
    private final M m;
    private final int purpose;
    private final String toString;

    Purpose(final M m, final int purpose) {
        this.m = m;
        if (purpose == 0 || Index.isHardened(purpose))
            throw new IllegalArgumentException();
        this.purpose = purpose;
        toString = String.format("%s/%d'", m, purpose);
    }

    public int getValue() {
        return purpose;
    }

    @Override
    public String toString() {
        return toString;
    }

    /**
     * Create a {@link Coin} for this purpose.
     *
     * @param coinType The coin type
     * @return A coin type instance for this purpose
     */
    public Coin coin(final CoinType coinType) {
        return new Coin(this, coinType);
    }
}
