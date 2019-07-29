package com.vechain.wallet.bip44;


public class Change {
    private final Account account;
    private final int change;
    private final String string;

    Change(final Account account, final int change) {
        this.account = account;
        this.change = change;
        string = String.format("%s/%d", account, change);
    }

    public int getValue() {
        return change;
    }

    public Account getParent() {
        return account;
    }

    @Override
    public String toString() {
        return string;
    }

    /**
     * Create a {@link AddressIndex} for this purpose, coin type, account and change.
     *
     * @param addressIndex The index of the child
     * @return A coin type instance for this purpose, coin type, account and change.
     */
    public AddressIndex address(final int addressIndex) {
        return address(addressIndex, false);
    }

    public AddressIndex address(final int addressIndex, final boolean hard) {
        return new AddressIndex(this, addressIndex, hard);
    }
}
