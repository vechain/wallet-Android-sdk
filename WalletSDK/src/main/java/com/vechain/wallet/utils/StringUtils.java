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

package com.vechain.wallet.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;

public class StringUtils {


    private StringUtils() {
    }


    public static String zeros(int n) {
        return repeat('0', n);
    }

    public static String repeat(char value, int n) {
        return new String(new char[n]).replace("\0", String.valueOf(value));
    }


    public static String toHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] fromHex(String s) {
        if (s != null) {
            try {
                StringBuilder sb = new StringBuilder(s.length());
                for (int i = 0; i < s.length(); i++) {
                    char ch = s.charAt(i);
                    if (!Character.isWhitespace(ch)) {
                        sb.append(ch);
                    }
                }
                s = sb.toString();
                int len = s.length();
                byte[] data = new byte[len / 2];
                for (int i = 0; i < len; i += 2) {
                    int hi = (Character.digit(s.charAt(i), 16) << 4);
                    int low = Character.digit(s.charAt(i + 1), 16);
                    if (hi >= 256 || low < 0 || low >= 16) {
                        return null;
                    }
                    data[i / 2] = (byte) (hi | low);
                }
                return data;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static boolean isNullString(@Nullable String str) {
        return str == null || str.length() == 0 || "".equals(str) || "null".equals(str);
    }

    public static boolean string2Boolean(String str, boolean def) {
        boolean value = def;
        try {
            value = Boolean.valueOf(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    public static int string2Int(String str) {
        return string2Int(str, 0);
    }

    public static int string2Int(String str, int def) {
        try {
            return Integer.valueOf(str);
        } catch (Exception e) {
        }
        return def;
    }


    public static long string2Long(String str) {
        if (isNullString(str)) {
            return 0L;
        } else {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException var2) {
                return 0L;
            }
        }
    }

    public static double string2Double(String str) {
        if (isNullString(str)) {
            return 0.0D;
        } else {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException var2) {
                return 0.0D;
            }
        }
    }

    public static float string2Float(String str) {
        if (isNullString(str)) {
            return 0.0F;
        } else {
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException var2) {
                return 0.0F;
            }
        }
    }


    public static boolean isHexString(String hex) {
        if (hex == null) return false;
        if (!hex.startsWith("0x")) return false;
        String value = hex.substring(2);

        return checkHEXString(value);
    }

    private static boolean checkHEXString(String hex) {
        char[] chars = hex.toCharArray();
        boolean checked = false;
        for (char item : chars) {
            checked = ((item >= '0' && item <= '9') || (item >= 'A' && item <= 'F') || (item >= 'a' && item <= 'f'));
            if (!checked) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkDecimalString(String value) {
        if (TextUtils.isEmpty(value)) return false;
        char[] chars = value.toCharArray();
        boolean checked;
        for (char item : chars) {
            checked = (item >= '0' && item <= '9');
            if (!checked) {
                return false;
            }
        }
        return true;
    }


    //

    /**
     * Decimals, divided by 10 ^ decimals, retain decimals (retain less than or equal to decimals)
     *
     * @param value
     * @param decimals exponent
     * @param retain   retain decimals
     * @return formatString
     */
    public static String largeDecimal2FormatString(String value, int decimals, int retain) {
        BigDecimal bigDecimal = largeDecimal2BigDecimal(value, decimals, retain);

        return getCommaFormat(bigDecimal);
    }

    public static String largeDecimal2String(String value, int decimals, int retain) {

        BigDecimal bigDecimal = largeDecimal2BigDecimal(value, decimals, retain);

        return bigDecimal.toString();

    }

    public static BigDecimal largeDecimal2BigDecimal(String value, int decimals, int retain) {
        if (value == null || value.isEmpty()) return new BigDecimal(0);

        if (decimals <= 0 || retain <= 0) return new BigDecimal(value);

        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.movePointLeft(decimals);
        bigDecimal = bigDecimal.setScale(retain, BigDecimal.ROUND_HALF_UP);

        return bigDecimal;

    }


    /**
     * Hexadecimal number divided by 10 ^ decimals, retain decimals (retain less than or equal to decimals)
     *
     * @param hex      Hexadecimal number
     * @param decimals exponent
     * @param retain   retain decimals
     * @return formatString
     */
    public static String hexString2DecimalFormatString(String hex, int decimals, int retain) {
        BigDecimal bigDecimal = hexString2DecimalValue(hex, decimals, retain);

        return getCommaFormat(bigDecimal);

    }

    public static String hexString2DecimalString(String hex, int decimals, int retain) {
        BigDecimal bigDecimal = hexString2DecimalValue(hex, decimals, retain);

        return bigDecimal.toString();

    }

    public static BigDecimal hexString2DecimalValue(String hex, int decimals, int retain) {

        if (hex == null || hex.isEmpty())
            return new BigDecimal("0");

        BigInteger bigInteger = HexUtils.toBigInt(hex);
        BigDecimal bigDecimal = new BigDecimal(bigInteger.toString());

        if (decimals > 0)
            bigDecimal = bigDecimal.movePointLeft(decimals);
        if (retain > 0)
            bigDecimal = bigDecimal.setScale(retain, BigDecimal.ROUND_DOWN);

        return bigDecimal;
    }

    public static String hexString2DecimalString(String hex) {

        if (hex == null || hex.isEmpty())
            return "0";

        BigInteger bigInteger = HexUtils.toBigInt(hex);
        BigDecimal bigDecimal = new BigDecimal(bigInteger.toString());

        return bigDecimal.toString();
    }

    /**
     * Formatted display with commas added in the middle of each 3 bits
     *
     * @param value
     * @return comma format string
     */
    public static String getCommaFormat(BigDecimal value) {
        return getFormat("###,##0.00", value);
    }

    private static DecimalFormat decimalFormat = new DecimalFormat("###,##0.00", new DecimalFormatSymbols(Locale.ENGLISH));

    /**
     * Custom Digital Format Method
     *
     * @param style
     * @param value
     * @return format string
     */
    public static String getFormat(String style, BigDecimal value) {
        decimalFormat.applyPattern(style);// Applying formats to formatters
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        return decimalFormat.format(value);
    }

    /**
     * String to Number(There is no comma in the string)
     *
     * @param value
     * @return bigDecimal
     */
    public static BigDecimal string2BigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return new BigDecimal(0);
        }
        BigDecimal bigDecimal = null;
        try {
            bigDecimal = new BigDecimal(value);
        } catch (Exception e) {
            bigDecimal = new BigDecimal(0);
        }
        return bigDecimal;
    }

    //10-digit string comma splitting
    public static String decString2Comma(String value) {
        BigDecimal bigDecimal = string2BigDecimal(value);
        return getCommaFormat(bigDecimal);
    }

    public static String intString2Comma(String value) {
        BigDecimal bigDecimal = string2BigDecimal(value);
        decimalFormat.applyPattern("###,##0");
        decimalFormat.setRoundingMode(RoundingMode.UP);
        return decimalFormat.format(bigDecimal);
    }

    public static String decString2CommaRetainFraction(String value) {
        BigDecimal bigDecimal = commaString2BigDecimal(value);
        return getFormat("###,##0.##################", bigDecimal);
    }

    public static String decString2RetainFraction(String value) {
        BigDecimal bigDecimal = commaString2BigDecimal(value);
        return getFormat("#0.##################", bigDecimal);
    }

    /**
     * @param value Include commas in value string
     * @return bigDecimal
     */
    public static BigDecimal commaString2BigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return new BigDecimal(0);
        }
        //去掉逗号
        java.util.StringTokenizer st = new StringTokenizer(value, ",");
        StringBuffer sb = new StringBuffer();
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
        }
        BigDecimal bigDecimal = new BigDecimal(sb.toString());

        return bigDecimal;
    }

    /**
     * Enlarging 10 ^ n times to the beginning of the hexadecimal string 0x
     *
     * @param value  numerical value string
     * @param series exponent
     * @return hex string
     */
    public static String string2LargeHex(String value, int series) {
        BigDecimal bigDecimal = commaString2BigDecimal(value);

        if (series > 0)
            bigDecimal = bigDecimal.movePointRight(series);

        BigInteger bigInteger = bigDecimal.toBigInteger();

        return HexUtils.toHexStringWithPrefix(bigInteger);

    }

    /**
     * Enlargement of a 10-decimal string by 10 ^ n times after a string is converted to a numeric value
     *
     * @param value  numerical value string
     * @param series exponent
     * @return large Dec string
     */
    public static String string2LargeDec(String value, int series) {
        BigDecimal bigDecimal = commaString2BigDecimal(value);

        if (series > 0)
            bigDecimal = bigDecimal.movePointRight(series);

        BigInteger bigInteger = bigDecimal.toBigInteger();

        return bigInteger.toString();

    }

    public static String getRandomString(int length) {

        if (length <= 0) return "";
        Random random = new Random();

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int value = Math.abs(random.nextInt() % 10);
            buffer.append(value);
        }
        return buffer.toString();
    }


    /**
     * Each parameter bit of the contract method is 32 bytes, and a 64 hexadecimal string
     *
     * @param item Hexadecimal parameter
     * @return 64 hexadecimal strings
     */
    public static String getParamTo32BitsLength(String item) {
        int size = 0;
        if (item == null || item.isEmpty())
            size = 0;
        else
            size = item.length();
        String hexValue = "";
        if (size > 0) {
            //clean 0x
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
        buffer.append(item);
        return buffer.toString();
    }

}