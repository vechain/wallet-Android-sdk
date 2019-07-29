package com.vechain.wallet.bip44;


public class NotSupportPathException extends Exception {
    public NotSupportPathException(String message) {
        super(message);
    }
}
