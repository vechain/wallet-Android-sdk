package com.vechain.wallet.bip39;

import java.security.SecureRandom;
import java.util.Random;


public class SecureRandomSeed {
    public static byte[] random(MnemonicWordNumber mnemonicWordNumber) {
        //https://developer.android.com/reference/java/security/SecureRandom
        //https://docs.oracle.com/javase/8/docs%2Ftechnotes%2Fguides%2Fsecurity%2FStandardNames.html#SecureRandom
        //SecureRandom.algorithm = NativePRNG
        //http://www.vuln.cn/6040
        //https://blog.csdn.net/fishmen26/article/details/42581917
        return random(mnemonicWordNumber, new SecureRandom());
    }

    public static byte[] random(MnemonicWordNumber mnemonicWordNumber, Random random) {
        byte[] randomSeed = new byte[mnemonicWordNumber.byteLength()];
        random.nextBytes(randomSeed);
        return randomSeed;
    }
}
