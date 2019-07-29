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

package com.vechain.wallet.dapp.plugin;

import android.webkit.JsPromptResult;
import android.webkit.WebView;

import com.vechain.wallet.dapp.bean.JSRequest;
import com.vechain.wallet.dapp.bean.WalletResponse;
import com.vechain.wallet.network.utils.GsonUtils;


public class BasePlugin {

    protected WebView webView; // WebView object

    protected NativeCallJS callJSUtils;

    public void initialize(WebView webView) {
        this.webView = webView;
        if (callJSUtils == null)
            callJSUtils = new NativeCallJS();
        callJSUtils.setWebView(webView);
    }

    public void closeWebView() {
        this.webView = null;
        if (callJSUtils == null)
            callJSUtils = new NativeCallJS();
        callJSUtils.setWebView(null);
    }

    public WebView getWebView() {
        return webView;
    }

    public void execute(JSRequest request, String rawJson, JsPromptResult result) {

        responseRequestParamsError(result, request);
    }

    public void callback(String callbackId, WalletResponse response, JsPromptResult jsPromptResult) {
        if (jsPromptResult != null) jsPromptResult.confirm("");
        if (callJSUtils == null)
            callJSUtils = new NativeCallJS();
        callJSUtils.setWebView(webView);
        String callbackMsg = GsonUtils.toJson(response);
        callJSUtils.callJavaScript(callbackId, callbackMsg);
    }


    public final void responseSuccess(JsPromptResult jsPromptResult, JSRequest request, Object data) {

        //successs
        WalletResponse response = WalletResponse.success(request, data);
        response(jsPromptResult, response);
    }

    public final void responseRequestParamsError(JsPromptResult jsPromptResult, JSRequest request) {

        //request parameter error
        WalletResponse response = WalletResponse.requestParamsError(request);
        response(jsPromptResult, response);
    }

    public final void responseDataError(JsPromptResult jsPromptResult, JSRequest request) {
        //response data error
        WalletResponse response = WalletResponse.dataError(request);
        response(jsPromptResult, response);
    }

    public final void responseNetworkError(JsPromptResult jsPromptResult, JSRequest request) {
        //network error
        WalletResponse response = WalletResponse.networkError(request);
        response(jsPromptResult, response);
    }

    public final void responseThrowable(Throwable throwable, JsPromptResult jsPromptResult, JSRequest request) {
        if (throwable != null && throwable instanceof NullPointerException) {
            responseDataError(jsPromptResult, request);
            return;
        }
        responseNetworkError(jsPromptResult, request);
    }


    public void response(JsPromptResult result, WalletResponse response) {

        //Recovering WebView to Prevent Memory Leakage
        closeWebView();

        if (result == null) return;
        String json = GsonUtils.toJson(response);
        result.confirm(json);

    }


    public void callbackSuccess(JsPromptResult jsPromptResult, JSRequest request, Object data) {

        //success
        WalletResponse response = WalletResponse.success(request, data);
        callback(getCallbackId(request), response, jsPromptResult);
    }

    public final void callbackDAppInitError(JsPromptResult jsPromptResult, JSRequest request) {

        //dApp initialization error
        WalletResponse response = WalletResponse.dAppInitError(request);
        callback(getCallbackId(request), response, jsPromptResult);
    }

    public final void callbackRequestParamsError(JsPromptResult jsPromptResult, JSRequest request) {

        //request parameter error
        WalletResponse response = WalletResponse.requestParamsError(request);
        callback(getCallbackId(request), response, jsPromptResult);
    }

    public final void callbackDataError(JsPromptResult jsPromptResult, JSRequest request) {
        //Response data error
        WalletResponse response = WalletResponse.dataError(request);
        callback(getCallbackId(request), response, jsPromptResult);
    }

    public final void callbackNetworkError(JsPromptResult jsPromptResult, JSRequest request) {
        //network error
        WalletResponse response = WalletResponse.networkError(request);
        callback(getCallbackId(request), response, jsPromptResult);
    }

    public final void callbackThrowable(Throwable throwable, JsPromptResult jsPromptResult, JSRequest request) {

        if (throwable != null && throwable instanceof NullPointerException) {
            callbackDataError(jsPromptResult, request);
            return;
        }
        callbackNetworkError(jsPromptResult, request);
    }

    public final void callback(int status, JsPromptResult jsPromptResult, JSRequest request, Object data) {
        WalletResponse response = null;
        switch (status) {
            case WalletResponse.OK:
                callbackSuccess(jsPromptResult, request, data);
                break;

            case WalletResponse.ERROR_400:
                response = WalletResponse.rejected(request);
                callback(getCallbackId(request), response, jsPromptResult);
                break;

            case WalletResponse.ERROR_500:
                response = WalletResponse.netError(request);
                callback(getCallbackId(request), response, jsPromptResult);
                break;

        }
    }

    private String getCallbackId(JSRequest request){
        String callbackId = request.getCallbackId();
        String requestId = request.getRequestId();

        return  callbackId + requestId;
    }
}
