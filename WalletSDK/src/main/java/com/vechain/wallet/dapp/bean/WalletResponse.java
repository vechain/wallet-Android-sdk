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

package com.vechain.wallet.dapp.bean;


public class WalletResponse<T> {

    public final static int OK = 1;
    public final static int ERROR_400 = 400; // Rejected
    public final static int ERROR_500 = 500; // NetError

    private int code;// 1,

    private String message;// 'error message',

    private String requestId;// random string

    private T data;// JSON data returned

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public final static WalletResponse success(JSRequest request, Object data) {
        WalletResponse walletResponse = new WalletResponse();
        walletResponse.code = OK;
        if (request != null)
            walletResponse.requestId = request.getRequestId();
        walletResponse.data = data;

        return walletResponse;
    }

    public final static WalletResponse notFoundMethod(JSRequest request) {

        return rejected(request);
    }


    public final static WalletResponse requestParamsError(JSRequest request) {

        return rejected(request);
    }

    public final static WalletResponse networkError(JSRequest request) {

        return netError(request);
    }

    public final static WalletResponse dataError(JSRequest request) {

        return netError(request);
    }

    public final static WalletResponse cancelError(JSRequest request) {

        return rejected(request);
    }


    public final static WalletResponse dAppInitError(JSRequest request) {

        return rejected(request);
    }


    public final static WalletResponse rejected(JSRequest request) {
        WalletResponse walletResponse = new WalletResponse();
        walletResponse.code = ERROR_400;
        walletResponse.message = "Rejected";
        if (request != null)
            walletResponse.requestId = request.getRequestId();
        return walletResponse;
    }

    public final static WalletResponse netError(JSRequest request) {
        WalletResponse walletResponse = new WalletResponse();
        walletResponse.code = ERROR_500;
        walletResponse.message = "NetError";
        if (request != null)
            walletResponse.requestId = request.getRequestId();
        return walletResponse;
    }

}
