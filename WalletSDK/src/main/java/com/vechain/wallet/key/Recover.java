
/*
 * Vechain Wallet SDK is licensed under the MIT LICENSE, also included in LICENSE file in the repository.
 *
 * Copyright (c) 2019 VeChain support@vechain.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.vechain.wallet.key;

import com.vechain.wallet.utils.Hash;

public class Recover {

    private Recover(){

    }

    public static byte[] recoverAddress(byte[] messageHash, byte[] signature) {

        //Signature Object
        ECDSASignature ecdsaSignature = getSignature(signature);
        if (ecdsaSignature == null) return null;
        byte[] pub = null;
        try {
            //Get the public key based on signature information and messageHash
            pub = ThorKeyPair.recoverPubBytesFromSignature(ecdsaSignature.v, ecdsaSignature, messageHash);
        } catch (Exception e) {
            pub = null;
        }
        if (pub == null) return null;

        //Return the address based on the public key
        byte[] publicKey = new byte[pub.length - 1];
        System.arraycopy(pub, 1, publicKey, 0, publicKey.length);

        byte[] byteAddress = Hash.keccak256(publicKey);
        byte[] address = new byte[20];
        System.arraycopy(byteAddress, 12, address, 0, address.length);

        return address;
    }


    private static ECDSASignature getSignature(byte[] signature) {
        //Signature information must be 65 bytes
        if (signature == null || signature.length != 65) return null;
        byte[] r = new byte[32];//The first 32 are r.
        byte[] s = new byte[32];//Then 32 are s.

        System.arraycopy(signature, 0, r, 0, 32);
        System.arraycopy(signature, 32, s, 0, 32);

        byte v = signature[64];

        ECDSASignature ecdsaSignature = ECDSASignature.fromComponents(r, s, v);

        return ecdsaSignature;
    }



}
