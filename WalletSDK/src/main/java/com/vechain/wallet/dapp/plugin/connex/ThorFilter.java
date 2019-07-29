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

import com.google.gson.reflect.TypeToken;
import com.vechain.wallet.dapp.bean.JSRequest;
import com.vechain.wallet.dapp.bean.request.RequestConnexFilter;
import com.vechain.wallet.dapp.plugin.BasePlugin;
import com.vechain.wallet.dapp.plugin.PluginEntry;
import com.vechain.wallet.network.Api;
import com.vechain.wallet.network.bean.logs.EventFilter;
import com.vechain.wallet.network.bean.logs.TransferFilter;
import com.vechain.wallet.network.engine.NetCallBack;
import com.vechain.wallet.network.utils.GsonUtils;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;


public class ThorFilter extends BasePlugin {

    private static String METHOD = "filterApply";

    public static PluginEntry get() {
        return PluginEntry.get(METHOD, ThorFilter.class.getName());
    }

    //'event'|'transfer'
    private final static String EVENT = "event";
    private final static String TRANSFER = "transfer";


    public void execute(final JSRequest request, final String rawJson, final JsPromptResult jsPromptResult) {

        if (jsPromptResult != null)
            jsPromptResult.confirm("");

        String params = JSRequest.getParamsValue(rawJson, "params");
        if (TextUtils.isEmpty(params)) {
            //Parameter error
            callbackRequestParamsError(null, request);
            return;
        }

        String kind = null;
        EventFilter eventFilter = null;
        TransferFilter transferFilter = null;

        try {
            JSONObject jsonObject = new JSONObject(params);
            kind = jsonObject.getString("kind");
            if (TRANSFER.equals(kind)) {

                Type type = new TypeToken<RequestConnexFilter<TransferFilter>>() {
                }.getType();
                RequestConnexFilter<TransferFilter> requestConnexFilter = GsonUtils.fromJson(params, type);
                transferFilter = requestConnexFilter.getFilterBody();

            } else if (EVENT.equals(kind)) {

                Type type = new TypeToken<RequestConnexFilter<EventFilter>>() {
                }.getType();
                RequestConnexFilter<EventFilter> requestConnexFilter = GsonUtils.fromJson(params, type);
                eventFilter = requestConnexFilter.getFilterBody();

            } else {
                //parameter error
                callbackRequestParamsError(null, request);
            }

        } catch (Exception e) {
            e.printStackTrace();
            //parameter error
            callbackRequestParamsError(null, request);
            return;
        }


        if (transferFilter != null) {

            Api.logTransfer(transferFilter, new NetCallBack() {
                @Override
                public void onSuccess(Object result) {
                    if (result != null && result instanceof List) {
                        callbackSuccess(null, request, result);
                    } else {
                        callbackDataError(null, request);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    super.onError(throwable);
                    //parameter error
                    callbackThrowable(throwable, null, request);
                }
            }, null);

        } else if (eventFilter != null) {


            Api.logEvent(eventFilter, new NetCallBack() {
                @Override
                public void onSuccess(Object result) {

                    if (result != null && result instanceof List) {
                        callbackSuccess(null, request, result);
                    } else {
                        callbackDataError(null, request);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    super.onError(throwable);
                    //parameter error
                    callbackThrowable(throwable, null, request);
                }
            }, null);

        } else {
            //parameter error
            callbackRequestParamsError(null, request);
        }

    }

}
