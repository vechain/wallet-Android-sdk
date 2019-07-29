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

package com.vechain.wallet.network.websocket;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;


public class WebSocketManager implements SocketAbility {

    private final static int RECONNECT_INTERVAL = 10 * 1000;    //Reconnection Self-increasing Step
    private final static long RECONNECT_MAX_TIME = 120 * 1000;   //Maximum reconnection interval

    private Context context;
    private OkHttpClient okHttpClient;
    private WebSocket webSocket;
    private Request request;
    private SocketListener socketListener;
    private Lock lock;
    private Handler wsMainHandler = new Handler(Looper.getMainLooper());

    private String wsUrl;
    private int currentStatus = SocketStatus.DISCONNECTED;     //websocket status
    private boolean isNeedReconnect;          //is need automatic reconnection after disconnection
    private boolean isManualClose = false;         //Whether to close the websocket connection manually
    private int reconnectCount = 0;   //reconnection times

    private Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (socketListener != null) {
                socketListener.onReconnect();
            }
            buildConnect();
        }
    };

    private WebSocketListener webSocketListener = new WebSocketListener() {

        @Override
        public void onOpen(WebSocket webSocket, final Response response) {
            WebSocketManager.this.webSocket = webSocket;
            setStatus(SocketStatus.CONNECTED);
            connected();
            if (socketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            socketListener.onOpen(response);
                        }
                    });
                } else {
                    socketListener.onOpen(response);
                }
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, final ByteString bytes) {
            if (socketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            socketListener.onMessage(bytes);
                        }
                    });
                } else {
                    socketListener.onMessage(bytes);
                }
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, final String text) {
            if (socketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            socketListener.onMessage(text);
                        }
                    });
                } else {
                    socketListener.onMessage(text);
                }
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, final int code, final String reason) {
            if (socketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            socketListener.onClosing(code, reason);
                        }
                    });
                } else {
                    socketListener.onClosing(code, reason);
                }
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, final int code, final String reason) {
            if (socketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            socketListener.onClosed(code, reason);
                        }
                    });
                } else {
                    socketListener.onClosed(code, reason);
                }
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, final Throwable t, final Response response) {
            tryReconnect();
            if (socketListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    wsMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            socketListener.onFailure(t, response);
                        }
                    });
                } else {
                    socketListener.onFailure(t, response);
                }
            }
        }
    };

    public WebSocketManager(Builder builder) {
        context = builder.context;
        wsUrl = builder.wsUrl;
        isNeedReconnect = builder.needReconnect;
        okHttpClient = builder.okHttpClient;
        this.lock = new ReentrantLock();
    }

    private void initWebSocket() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build();
        }
        if (request == null) {
            request = new Request.Builder()
                    .url(wsUrl)
                    .build();
        }
        okHttpClient.dispatcher().cancelAll();
        try {
            lock.lockInterruptibly();
            try {
                okHttpClient.newWebSocket(request, webSocketListener);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
        }
    }

    @Override
    public WebSocket getWebSocket() {
        return webSocket;
    }


    public void setSocketListener(SocketListener socketListener) {
        this.socketListener = socketListener;
    }

    @Override
    public synchronized boolean isConnected() {
        return currentStatus == SocketStatus.CONNECTED;
    }

    @Override
    public synchronized int getStatus() {
        return currentStatus;
    }

    @Override
    public synchronized void setStatus(int currentStatus) {
        this.currentStatus = currentStatus;
    }

    @Override
    public void startConnect() {
        isManualClose = false;
        buildConnect();
    }

    @Override
    public void stopConnect() {
        isManualClose = true;
        disconnect();
    }

    private void tryReconnect() {
        if (!isNeedReconnect | isManualClose) {
            return;
        }

        if (!isNetworkConnected(context)) {
            setStatus(SocketStatus.DISCONNECTED);
            return;
        }

        setStatus(SocketStatus.RECONNECT);

        long delay = reconnectCount * RECONNECT_INTERVAL;
        wsMainHandler
                .postDelayed(reconnectRunnable, delay > RECONNECT_MAX_TIME ? RECONNECT_MAX_TIME : delay);
        reconnectCount++;
    }

    private void cancelReconnect() {
        wsMainHandler.removeCallbacks(reconnectRunnable);
        reconnectCount = 0;
    }

    private void connected() {
        cancelReconnect();
    }

    private void disconnect() {
        if (currentStatus == SocketStatus.DISCONNECTED) {
            return;
        }
        cancelReconnect();
        if (okHttpClient != null) {
            okHttpClient.dispatcher().cancelAll();
        }
        if (webSocket != null) {
            boolean isClosed = webSocket.close(SocketStatus.Status.NORMAL_CLOSE, SocketStatus.Reason.NORMAL_CLOSE);
            //Abnormal closure of connection
            if (!isClosed) {
                if (socketListener != null) {
                    socketListener.onClosed(SocketStatus.Status.ABNORMAL_CLOSE, SocketStatus.Reason.ABNORMAL_CLOSE);
                }
            }
        }
        setStatus(SocketStatus.DISCONNECTED);
    }

    private synchronized void buildConnect() {
        if (!isNetworkConnected(context)) {
            setStatus(SocketStatus.DISCONNECTED);
            return;
        }
        switch (getStatus()) {
            case SocketStatus.CONNECTED:
            case SocketStatus.CONNECTING:
                break;
            default:
                setStatus(SocketStatus.CONNECTING);
                initWebSocket();
        }
    }

    //send message
    @Override
    public boolean sendMessage(String msg) {
        return send(msg);
    }

    @Override
    public boolean sendMessage(ByteString byteString) {
        return send(byteString);
    }

    private boolean send(Object msg) {
        boolean isSend = false;
        if (webSocket != null && currentStatus == SocketStatus.CONNECTED) {
            if (msg instanceof String) {
                isSend = webSocket.send((String) msg);
            } else if (msg instanceof ByteString) {
                isSend = webSocket.send((ByteString) msg);
            }
            //Failed to send message, attempt to reconnect
            if (!isSend) {
                tryReconnect();
            }
        }
        return isSend;
    }

    //Check whether the network is connected
    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static final class Builder {

        private Context context;
        private OkHttpClient okHttpClient;
        private String wsUrl;
        private boolean needReconnect = true;

        public Builder(Context ctx) {
            context = ctx;
        }

        public Builder wsUrl(String url) {
            wsUrl = url;
            return this;
        }

        public Builder client(OkHttpClient client) {
            okHttpClient = client;
            return this;
        }

        public Builder needReconnect(boolean isNeedReconnect) {
            needReconnect = isNeedReconnect;
            return this;
        }

        public WebSocketManager build() {
            return new WebSocketManager(this);
        }
    }
}
