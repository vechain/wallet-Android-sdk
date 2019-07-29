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

package com.vechain.wallet.dapp.plugin.connex;

import android.text.TextUtils;
import android.webkit.JsPromptResult;

import com.vechain.wallet.dapp.bean.JSRequest;
import com.vechain.wallet.dapp.bean.WalletResponse;
import com.vechain.wallet.dapp.bean.request.RequestConnexReceipt;
import com.vechain.wallet.dapp.plugin.BasePlugin;
import com.vechain.wallet.dapp.plugin.NativeCallJS;
import com.vechain.wallet.dapp.plugin.PluginEntry;
import com.vechain.wallet.network.Api;
import com.vechain.wallet.network.bean.core.TransactionReceipt;
import com.vechain.wallet.network.engine.NetCallBack;
import com.vechain.wallet.network.utils.GsonUtils;
import com.vechain.wallet.utils.StringUtils;


public class GetTransactionReceipt extends BasePlugin {

    private static String METHOD = "getTransactionReceipt";

    public static PluginEntry get() {
        return PluginEntry.get(METHOD, GetTransactionReceipt.class.getName());
    }

    public void execute(final JSRequest request, final String rawJson, final JsPromptResult jsPromptResult) {

        if (jsPromptResult != null) jsPromptResult.confirm("");

        RequestConnexReceipt receipt = JSRequest.convertParams(rawJson, RequestConnexReceipt.class);
        if (receipt == null || TextUtils.isEmpty(receipt.getId())) {
            //parameter error
            callbackRequestParamsError(null, request);
            return;
        }

        String txId = receipt.getId();
        if (!StringUtils.isHexString(txId)) {
            //parameter error
            callbackRequestParamsError(null, request);
            return;
        }

        Api.getTransactionReceipt(txId, new NetCallBack() {
            @Override
            public void onSuccess(Object result) {

                if (result != null && result instanceof TransactionReceipt) {
                    callbackSuccess(null, request, result);
                } else {
                    //response data error
                    callbackSuccess(null, request, null);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                //network error
                callbackSuccess(null, request, null);
            }
        }, null);
    }

    public  void callback(String callbackId, WalletResponse response, JsPromptResult jsPromptResult) {
        if (jsPromptResult != null) jsPromptResult.confirm("");
        if (callJSUtils == null)
            callJSUtils = new NativeCallJS();
        callJSUtils.setWebView(webView);

        String callbackMsg = GsonUtils.toSerializeNullString(response);

        callJSUtils.callJavaScript(callbackId, callbackMsg);
    }

}
