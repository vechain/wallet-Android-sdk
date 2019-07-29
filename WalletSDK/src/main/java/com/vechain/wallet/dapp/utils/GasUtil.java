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

package com.vechain.wallet.dapp.utils;

import android.text.TextUtils;


import com.vechain.wallet.thor.tx.Clause;

import java.util.List;

public class GasUtil {


    private final static long OFFSET = 15000;


    public static long getOFFSET() {
        return OFFSET;
    }

    public static long intrinsicGas(List<Clause> clauses) {
        long txGas = 5000;
        long clauseGas = 16000;
        long clauseGasContractCreation = 48000;
        if (clauses == null || clauses.isEmpty()) {
            return txGas + clauseGas;
        }

        long sumGas = txGas;
        for (Clause clause : clauses) {

            if (!TextUtils.isEmpty(clause.getTo())) {
                sumGas += clauseGas;
            } else {
                sumGas += clauseGasContractCreation;
            }
            sumGas += getDataGas(clause.getData());
        }


        return sumGas;
    }

    private static long getDataGas(String data) {

        if (data == null || data.isEmpty()) return 0;
        if (data.equalsIgnoreCase("0x")) return 0;

        int zgas = 4;
        int nzgas = 68;
        long sum = 0;
        int size = data.length();
        int n = size / 2;

        for (int i = 1; i < n; i++) {
            String item = data.substring(2 * i, 2 * (i + 1));
            if (item.equalsIgnoreCase("00")) {
                sum += zgas;
            } else {
                sum += nzgas;
            }
        }
        return sum;
    }


}
