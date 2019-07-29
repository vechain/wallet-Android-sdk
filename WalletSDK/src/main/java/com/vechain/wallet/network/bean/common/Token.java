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

package com.vechain.wallet.network.bean.common;


import android.text.TextUtils;

public class Token {

    public final static String VET = "VET";
    public final static String VTHO = "VTHO";

    public final static int RETAIN = 2;//keep decimal digits
    public final static int DECIMALS = 18;//data magnification exponential multiple


    private String address;


    private String name;


    private String symbol;


    private int decimals;


    private long transferGas;



    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public long getTransferGas() {
        return transferGas;
    }

    public void setTransferGas(long transferGas) {
        this.transferGas = transferGas;
    }



    public static Token getVETToken(String address){
        if(TextUtils.isEmpty(address))return null;
        if(!address.startsWith("0x"))return null;
        if(address.length()!=42)return null;
        Token vetToken = new Token();
        vetToken.setAddress(address);
        vetToken.setDecimals(DECIMALS);
        vetToken.setName(VET);
        vetToken.setSymbol(VET);
        vetToken.setTransferGas(21000);

        return vetToken;
    }

    public static Token getVTHOToken(){

        Token vthoToken = new Token();
        vthoToken.setAddress("0x0000000000000000000000000000456e65726779");
        vthoToken.setDecimals(DECIMALS);
        vthoToken.setName(VTHO);
        vthoToken.setSymbol(VTHO);
        vthoToken.setTransferGas(60000);

        return vthoToken;
    }

}
