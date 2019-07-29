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
import android.webkit.JsPromptResult;
import android.webkit.WebView;

import com.vechain.wallet.dapp.bean.JSRequest;
import com.vechain.wallet.dapp.bean.WalletResponse;
import com.vechain.wallet.dapp.bean.response.ResponseConnexStatus;
import com.vechain.wallet.dapp.bean.response.ResponseConnexStatusHead;
import com.vechain.wallet.dapp.plugin.BasePlugin;
import com.vechain.wallet.dapp.plugin.PluginEntry;
import com.vechain.wallet.network.Api;
import com.vechain.wallet.network.bean.core.Block;
import com.vechain.wallet.network.bean.core.Peer;
import com.vechain.wallet.network.engine.NetCallBack;
import com.vechain.wallet.network.utils.GsonUtils;
import com.vechain.wallet.network.websocket.WebSocketManager;
import com.vechain.wallet.network.websocket.SocketListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.ByteString;


public class GetAccountStatus extends BasePlugin {

    private static String METHOD = "getStatus";

    public static String getMETHOD() {
        return METHOD;
    }

    public static PluginEntry get() {
        return PluginEntry.get(METHOD, GetAccountStatus.class.getName());
    }

    private WebSocketManager webSocketManager;

    private long maxBlockNumber;

    private ResponseConnexStatus status;
    private JSRequest request;
    private JsPromptResult jsPromptResult;

    @Override
    public void initialize(WebView webView) {
        super.initialize(webView);
        if(status!=null){
            //If you find that there is a boot ahead, it is closed by other pages and restarted.
            start();
        }
    }

    @Override
    public void closeWebView() {
        super.closeWebView();
        //3.When it's not recycled, close it
        close();

    }


    public void response(JsPromptResult result, WalletResponse response) {

        //Recovering WebView to Prevent Memory Leakage
        // initialize(null);

        if (result != null) {
            String json = GsonUtils.toJson(response);
            result.confirm(json);

            //2.restart
            start();

        }else{
            if(jsPromptResult!=null) {
                String json = GsonUtils.toJson(response);
                jsPromptResult.confirm(json);
            }
        }
    }

    public void execute(final JSRequest request, final String rawJson, final JsPromptResult jsPromptResult) {

        this.request = request;
        this.jsPromptResult = jsPromptResult;

        if (status == null) {
            //1.first
            maxBlockNumber = 0;
            getPeers(request, jsPromptResult);
        } else {
            //Get back
            responseSuccess(jsPromptResult, request, status);
        }

    }


    private void getBestBlock(final JSRequest request, final JsPromptResult jsPromptResult) {
        String blockId = "best";

        Api.getBlock(blockId, new NetCallBack() {

            @Override
            public void onSuccess(Object result) {

                if (result != null && result instanceof Block) {

                    Block block = (Block) result;

                    if (status == null)
                        status = new ResponseConnexStatus();

                    long bestBlockNumber = block.getNumber();
                    double progress = 1;
                    if (maxBlockNumber <= bestBlockNumber) {
                        progress = 1;
                    } else {
                        progress = bestBlockNumber * 1.0f / maxBlockNumber;
                    }

                    status.setProgress(progress);

                    ResponseConnexStatusHead head = new ResponseConnexStatusHead();
                    head.setId(block.getId());
                    head.setNumber(block.getNumber());
                    head.setParentID(block.getParentID());
                    head.setTimestamp(block.getTimestamp());

                    status.setHead(head);

                    responseSuccess(jsPromptResult, request, status);
                } else {
                    responseDataError(jsPromptResult, request);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                responseThrowable(throwable, jsPromptResult, request);
            }
        }, null);
    }


    private void getPeers(final JSRequest request, final JsPromptResult jsPromptResult) {

        Api.getPeers(new NetCallBack() {

            @Override
            public void onSuccess(Object result) {

                if (result != null && result instanceof List) {

                    List<Peer> peers = (List<Peer>) result;
                    getMaxBlockNumber(peers);
                    getBestBlock(request, jsPromptResult);

                } else {
                    responseDataError(jsPromptResult, request);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                responseNetworkError(jsPromptResult, request);
            }
        }, null);
    }


    private void getMaxBlockNumber(List<Peer> peers) {

        if (peers == null || peers.isEmpty()) return;

        for (Peer item : peers) {
            long number = item.getBlockNumber();
            if (number > maxBlockNumber) maxBlockNumber = number;
        }

    }


    private void start() {

        if (webSocketManager != null) return;

        String host = Api.getBlockChainHost();
        String wsHost = null;
        if (host.startsWith("https://"))
            wsHost = host.replace("https://", "wss://");
        else if (host.startsWith("http://"))
            wsHost = host.replace("http://", "ws://");


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
                getPeers(null, null);
            }

            @Override
            public void onMessage(ByteString bytes) {
                super.onMessage(bytes);
                //Message processing
                getPeers(null, null);
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
    }

    private void close() {
        if (webSocketManager != null) {
            webSocketManager.stopConnect();
            webSocketManager = null;
        }
        status = null;
    }

}

