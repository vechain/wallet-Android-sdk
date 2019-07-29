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

package com.vechain.wallet.thor.tx;

import com.vechain.wallet.utils.HexUtils;

import java.math.BigInteger;

public class Clause {

    //Hexadecimal address
    private String to;

    //Decimal value
    private String value;

    //Hexadecimal value
    private String data;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    //Value is 10-digit
    public static Clause getTokenClause(String toAddress, String value, String tokenAddress) {

        Clause clause = new Clause();
        clause.setValue("0");

        String tokenMethodId = "0xa9059cbb";
        StringBuffer buffer = new StringBuffer();
        buffer.append(tokenMethodId);
        buffer.append("000000000000000000000000");
        buffer.append(toAddress);

        BigInteger bigInteger = new BigInteger(value);
        String hexValue = HexUtils.toHexStringNoPrefix(bigInteger);
        int size = 64 - hexValue.length();
        if (size > 0) {
            for (int i = 0; i < size; i++)
                buffer.append("0");
        }
        buffer.append(hexValue);

        clause.setData(buffer.toString());
        clause.setTo(tokenAddress);

        return clause;
    }

    /**
     * @param value           Contract Transaction Amount
     * @param methodId        Contract Method
     * @param contractAddress Contract address
     * @param params          The parameters of the contract method, each of which is 32 bytes, 64-bit 16-digit string
     * @return Clauses
     */
    public static Clause getContractMethodClause(String value, String methodId, String contractAddress, String... params) {
        //params数据都是16进制字符串
        Clause clause = new Clause();
        clause.setValue(value);

        StringBuffer buffer = new StringBuffer();
        buffer.append(methodId);

        if(contractAddress!=null && !contractAddress.isEmpty()) {
            for (String item : params)
                buffer.append(getParamValue(item));
        }else{
            //contractAddress==null 认为是合约部署
            for (String item : params) {
                String itemHex = HexUtils.cleanHexPrefix(item);
                buffer.append(itemHex);
            }
        }
        clause.setData(buffer.toString());

        clause.setTo(contractAddress);

        return clause;
    }

    public static Clause getClause(String to, String value, String data) {
        Clause clause = new Clause();
        clause.setTo(to);
        clause.setValue(value);
        clause.setData(data);

        return clause;
    }


    /**
     * Each parameter bit of the contract method is 32 bytes, and a 64-bit hexadecimal string
     *
     * @param item Hexadecimal parameter
     * @return 64-bit hexadecimal string
     */
    private static String getParamValue(String item) {
        int size = 0;
        if (item == null || item.isEmpty())
            size = 0;
        else
            size = item.length();
        String hexValue = "";

        if (size > 0) {
            //Remove the 0x start
            hexValue = HexUtils.cleanHexPrefix(item);
            size = hexValue.length();
        }

        int count = 64;
        StringBuffer buffer = new StringBuffer();
        int n = count - size;
        if (n > 0) {
            for (int i = 0; i < n; i++)
                buffer.append("0");
        }
        buffer.append(hexValue);
        return buffer.toString();
    }

}
