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

package com.vechain.wallet.dapp.web;

import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.google.gson.reflect.TypeToken;
import com.vechain.wallet.dapp.bean.JSRequest;
import com.vechain.wallet.dapp.plugin.PluginManager;
import com.vechain.wallet.dapp.utils.WebUtils;
import com.vechain.wallet.network.utils.GsonUtils;

import java.lang.reflect.Type;

/**
 * initialize WebChromeClient in WebView
 */
public class DAppWebChromeClient extends WebChromeClient {

    private final static String SCHEME = "wallet://";
    private PluginManager pluginManager;


    public DAppWebChromeClient(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        WebUtils.injectWalletJavaScript(view);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {

        boolean isIntercept = shouldInterceptJsPrompt(this, view, url, message, defaultValue, result);
        if (isIntercept) return true;

        return super.onJsPrompt(view, url, message, defaultValue, result);
    }


    private boolean shouldInterceptJsPrompt(WebChromeClient client, WebView webView, String url, String message, String defaultValue, JsPromptResult result) {

        int schemeSize = SCHEME.length();
        if (defaultValue != null && defaultValue.length() > schemeSize && defaultValue.startsWith(SCHEME)) {
            if (WebUtils.isMustUpdate()) {
                result.confirm("");
                WebUtils.logVersion();
                return true;
            }
            String json;
            try {
                json = defaultValue.substring(schemeSize);

                Type type = new TypeToken<JSRequest>() {
                }.getType();

                JSRequest request = GsonUtils.fromJson(json, type);
                if (request != null && pluginManager != null) {
                    //Later, WebView needs to be reclaimed to prevent memory leaks
                    pluginManager.setWebView(webView);
                    pluginManager.exec(request, json, result);
                } else {
                    result.confirm("");
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                result.confirm("");
                return true;
            }
        }


        return false;
    }

}
