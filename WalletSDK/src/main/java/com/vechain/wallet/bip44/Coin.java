package com.vechain.wallet.bip44;


public class Coin {
    private final Purpose purpose;
    private final CoinType coinType;
    private final String string;

    Coin(final Purpose purpose, final CoinType coinType) {
        this.purpose = purpose;
        this.coinType = coinType;
        string = String.format("%s/%d'", purpose, coinType.getType());
    }

    public CoinType getValue() {
        return coinType;
    }

    public Purpose getParent() {
        return purpose;
    }

    @Override
    public String toString() {
        return string;
    }

    /**
     * Create a {@link Account} for this purpose and coin type.
     *
     * @param account The account number
     * @return An {@link Account} instance for this purpose and coin type
     */
    public Account account(final int account) {
        return new Account(this, account);
    }
}
