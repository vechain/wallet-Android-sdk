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

import android.app.Activity;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebView;

public class NativeCallJS {

    private WebView webView;

    // Web callback method
    private String callbackId;
    // Callback information content
    private String callbackMsg;

    // Whether callbackId exception capture needs to be called back
    private boolean hasCatch;

    private OnCallbackJSListener  finishedListener;

    public void setFinishedListener(OnCallbackJSListener finishedListener) {
        this.finishedListener = finishedListener;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    private String buildJavaScript() {

        StringBuffer jsBuffer = new StringBuffer();
        if (TextUtils.isEmpty(callbackId))
            return jsBuffer.toString();

        jsBuffer.append("(function(){");

        //Abnormal capture or not
        if (hasCatch)
            jsBuffer.append("try {");

        jsBuffer.append("window.");
        jsBuffer.append(callbackId);
        jsBuffer.append("('");
        //Callback parameters do not add parameters for empty
        if (!TextUtils.isEmpty(callbackMsg)) {
            jsBuffer.append(callbackMsg);
        }
        jsBuffer.append("');");

        //Abnormal capture or not
        if (hasCatch) {
            jsBuffer.append("} catch (e) { alert(e);}");
        }

        jsBuffer.append("})()");

        return jsBuffer.toString();
    }


    private final Runnable runnable = new Runnable() {
        public void run() {
            String js = buildJavaScript();
            if (!TextUtils.isEmpty(js)) {
                String function = "javascript:" + js;
                webView.evaluateJavascript(function, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if(finishedListener!=null)
                            finishedListener.finished();
                    }
                });
                //webView.loadUrl("javascript:" + js);
            }

        }
    };

    public void callJavaScript(String callbackId, String callbackMsg) {

        callJavaScript(callbackId, callbackMsg, false);
    }

    public void callJavaScript(String callbackId, String callbackMsg, boolean hasCatch) {

        this.callbackId = callbackId;
        this.callbackMsg = callbackMsg;

        //Abnormal capture
        this.hasCatch = hasCatch;

        if (webView == null) return;
        Activity activity = (Activity) (webView.getContext());
        activity.runOnUiThread(runnable);
    }

    public interface OnCallbackJSListener{
        void finished();
    }
}
