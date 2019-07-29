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

import com.vechain.wallet.thor.tx.ParameterException;
import com.vechain.wallet.network.bean.common.Token;
import com.vechain.wallet.utils.StringUtils;
import com.vechain.wallet.thor.tx.Clause;
import com.vechain.wallet.utils.HexUtils;

import java.math.BigDecimal;
import java.util.List;

public class ClauseUtil {


    public static boolean checkClauseList(List<Clause> clauseList) throws ParameterException {

        if (clauseList == null || clauseList.isEmpty()) return false;
        for (Clause item : clauseList) {
            if (!ClauseUtil.checkClause(item)) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkClause(Clause clause) throws ParameterException {

        if (clause == null)
            throw new ParameterException("clause is null");

        String to = clause.getTo();
        String value = clause.getValue();
        String data = clause.getData();

        if (to != null && !AddressCheckUtil.checkAddress(to)) {
            throw new ParameterException("The to parameter of Clause is incorrect");
        }


        if (data != null && !StringUtils.isHexString(data)) {
            //parameter error
            throw new ParameterException("The data parameter of Clause is not hex string");
        }

        if (value != null && !StringUtils.checkDecimalString(value) && !StringUtils.isHexString(value)) {
            //parameter error
            throw new ParameterException("The value parameter of Clause is not hex or decimal string");
        }

        if (!isRightClause(to, data, value)) {
            //parameter error
            throw new ParameterException("to, data, value parameter of Clause are mismatch");
        }

        //0x Equivalent to null
        if (to != null && data != null && data.equals(HexUtils.HEX_PREFIX))
            clause.setData(null);

        if(value != null && StringUtils.isHexString(value)){
            String decValue = StringUtils.hexString2DecimalString(value);
            clause.setValue(decValue);
        }

        return true;
    }

    private static boolean isRightClause(String to, String data, String value) throws ParameterException {

        if (TextUtils.isEmpty(to)) {

            if (TextUtils.isEmpty(data)) {
                //parameter error
                return false;
            } else {
                //Data is not a value of 0x0000
                String dataValue = HexUtils.cleanHexPrefix(data);
                boolean isZero = isAllZero(dataValue);
                if (isZero) {
                    //parameter error
                    return false;
                }
            }

        } else {
            if (data != null) {

                if(hasValue(value))return true;

                if (!TextUtils.isEmpty(data) && data.length() >= 10) {
                    int length = data.length();
                    int m = (length - 10) % 64;
                    if (m != 0) {
                        //parameter error
                        return false;
                    }
                } else {
                    if (data.equals(HexUtils.HEX_PREFIX)) {//0x to null
                        return true;
                    }
                    return false;
                }
            } else {
                //to!=null data=null value!=null
                if (TextUtils.isEmpty(value))
                    return false;
            }
        }

        String thorAddress = Token.getVTHOToken().getAddress();
        if (thorAddress.equalsIgnoreCase(to) && value != null) {
            //When transferring VTHO, the value must be 0
            BigDecimal bigDecimal = null;
            if (StringUtils.isHexString(value)) {
                bigDecimal = StringUtils.hexString2DecimalValue(value, 0, 0);
            } else {
                bigDecimal = StringUtils.string2BigDecimal(value);
            }
            if (bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                throw new ParameterException("When transferring VTHO（to = " + thorAddress + "）, the value parameter of this Clause must be 0");
            }
        }

        return true;
    }

    private static boolean isAllZero(String data) {
        if (TextUtils.isEmpty(data)) return false;
        char[] array = data.toCharArray();
        for (char s : array) {
            if (s != '0') return false;
        }
        return true;
    }

    private static boolean hasValue(String value){

        if(value==null || value.isEmpty())return false;
        String decValue = value;
        if(StringUtils.isHexString(value))
            decValue = StringUtils.hexString2DecimalString(value);
        BigDecimal bigDecimal = new BigDecimal(decValue);
        BigDecimal zero = new BigDecimal("0");
        return bigDecimal.compareTo(zero) > 0;
    }

}
