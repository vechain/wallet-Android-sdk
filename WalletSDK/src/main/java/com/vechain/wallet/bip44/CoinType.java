package com.vechain.wallet.bip44;


public enum CoinType {

    VET(818, "VET");


    private int type;
    private String name;

    CoinType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static CoinType parseCoinType(int type) throws NotFindCoinException {
        for (CoinType item : CoinType.values()) {
            if (item.type == type) {
                return item;
            }
        }
        throw new NotFindCoinException("Not supporting the currency");
    }
}
