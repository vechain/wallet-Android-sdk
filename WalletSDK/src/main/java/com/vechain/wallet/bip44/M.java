package com.vechain.wallet.bip44;


public final class M {
    private final Purpose PURPOSE_44 = new Purpose(this, 44);
    private final Purpose PURPOSE_49 = new Purpose(this, 49);

    M() {
    }

    /**
     * Create a {@link Purpose}.
     * For 44 and 49 this function is guaranteed to return the same instance.
     *
     * @param purpose The purpose number.
     * @return A purpose object.
     */
    public Purpose purpose(final int purpose) {
        switch (purpose) {
            case 44:
                return PURPOSE_44;
            case 49:
                return PURPOSE_49;
            default:
                return new Purpose(this, purpose);
        }
    }

    public Purpose purpose44() {
        return PURPOSE_44;
    }

    public Purpose purpose49() {
        return PURPOSE_49;
    }

    @Override
    public String toString() {
        return "m";
    }
}
