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


import android.content.Context;
import android.text.TextUtils;
import android.webkit.JsPromptResult;

import com.vechain.wallet.dapp.bean.JSRequest;
import com.vechain.wallet.dapp.plugin.BasePlugin;
import com.vechain.wallet.dapp.plugin.PluginEntry;
import com.vechain.wallet.network.Api;
import com.vechain.wallet.network.websocket.WebSocketManager;
import com.vechain.wallet.network.websocket.SocketListener;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.ByteString;

public class TickerNext extends BasePlugin {

    //tickerNext
    private static String METHOD = "tickerNext";

    public static PluginEntry get() {
        return PluginEntry.get(METHOD, TickerNext.class.getName());
    }

    private WebSocketManager webSocketManager;

    public void execute(final JSRequest request, final String rawJson, final JsPromptResult jsPromptResult) {

        String host = Api.getBlockChainHost();
        String wsHost = null;
        if (host.startsWith("https://"))
            wsHost = host.replace("https://", "wss://");
        else if (host.startsWith("http://"))
            wsHost = host.replace("http://", "ws://");

        if (TextUtils.isEmpty(wsHost)) {
            callbackRequestParamsError(jsPromptResult, request);
            return;
        }
        //JsPromptResult has been invoked at this location, and there is no need to transfer it later. It's null.
        jsPromptResult.confirm("");

        String wsUrl = wsHost + "/subscriptions/block";

        Context context = getWebView().getContext();
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .pingInterval(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        webSocketManager = new WebSocketManager.Builder(context)
                .wsUrl(wsUrl)
                .needReconnect(true)
                .client(okHttpClient)
                .build();

        webSocketManager.setSocketListener(new SocketListener() {
            @Override
            public void onOpen(Response response) {
                super.onOpen(response);
                //Protocol initialization, heartbeat, etc.
            }

            @Override
            public void onMessage(String text) {
                super.onMessage(text);
                //Message processing
                callbackSuccess(null, request, "");
                if (webSocketManager != null)
                    webSocketManager.stopConnect();
                webSocketManager = null;
            }

            @Override
            public void onMessage(ByteString bytes) {
                super.onMessage(bytes);
                //Message processing
                callbackSuccess(null, request, "");
                if (webSocketManager != null)
                    webSocketManager.stopConnect();
                webSocketManager = null;
            }

            @Override
            public void onReconnect() {
                super.onReconnect();

            }

            @Override
            public void onClosing(int code, String reason) {
                super.onClosing(code, reason);
            }

            @Override
            public void onClosed(int code, String reason) {
                super.onClosed(code, reason);
            }

            @Override
            public void onFailure(Throwable t, Response response) {
                super.onFailure(t, response);
            }
        });

        webSocketManager.startConnect();
        //String msg or ByteString byteString
        //wsManager.sendMessage();
        //wsManager.stopConnect();
    }

}
