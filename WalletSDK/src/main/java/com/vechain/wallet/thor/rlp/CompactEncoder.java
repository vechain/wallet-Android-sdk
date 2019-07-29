package com.vechain.wallet.thor.rlp;

import org.spongycastle.util.encoders.Hex;

import java.util.HashMap;
import java.util.Map;

public class CompactEncoder {

    private final static Map<Character, Byte> hexMap = new HashMap<>();

    static {
        hexMap.put('0', (byte) 0x0);
        hexMap.put('1', (byte) 0x1);
        hexMap.put('2', (byte) 0x2);
        hexMap.put('3', (byte) 0x3);
        hexMap.put('4', (byte) 0x4);
        hexMap.put('5', (byte) 0x5);
        hexMap.put('6', (byte) 0x6);
        hexMap.put('7', (byte) 0x7);
        hexMap.put('8', (byte) 0x8);
        hexMap.put('9', (byte) 0x9);
        hexMap.put('a', (byte) 0xa);
        hexMap.put('b', (byte) 0xb);
        hexMap.put('c', (byte) 0xc);
        hexMap.put('d', (byte) 0xd);
        hexMap.put('e', (byte) 0xe);
        hexMap.put('f', (byte) 0xf);
    }



    public static byte[] binToNibblesNoTerminator(byte[] str) {

        byte[] hexEncoded = Hex.encode(str);

        for (int i = 0; i < hexEncoded.length; ++i) {
            byte b = hexEncoded[i];
            hexEncoded[i] = hexMap.get((char) b);
        }

        return hexEncoded;
    }
}
