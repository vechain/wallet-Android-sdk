package com.vechain.wallet.bip44;

import com.vechain.wallet.bip32.Index;


public class Account {
    private final Coin coin;
    private final int account;
    private final String string;

    Account(final Coin coin, final int account) {
        if (Index.isHardened(account))
            throw new IllegalArgumentException();
        this.coin = coin;
        this.account = account;
        string = String.format("%s/%d'", coin, account);
    }

    public int getValue() {
        return account;
    }

    public Coin getParent() {
        return coin;
    }

    @Override
    public String toString() {
        return string;
    }

    /**
     * Create a {@link Change} for this purpose, coin type and account.
     * <p>
     * Constant 0 is used for external chain.
     * External chain is used for addresses that are meant to be visible outside of the wallet (e.g. for receiving
     * payments).
     *
     * @return A {@link Change} = 0 instance for this purpose, coin type and account
     */
    public Change external() {
        return new Change(this, 0);
    }

    /**
     * Create a {@link Change} for this purpose, coin type and account.
     * <p>
     * Constant 1 is used for internal chain (also known as change addresses).
     * Internal chain is used for addresses which are not meant to be visible outside of the wallet and is used for
     * return transaction change.
     *
     * @return A {@link Change} = 1 instance for this purpose, coin type and account
     */
    public Change internal() {
        return new Change(this, 1);
    }

}
