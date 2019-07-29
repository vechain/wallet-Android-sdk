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

import android.text.TextUtils;
import android.util.ArrayMap;
import android.webkit.JsPromptResult;
import android.webkit.WebView;

import com.vechain.wallet.dapp.bean.JSRequest;
import com.vechain.wallet.dapp.bean.WalletResponse;
import com.vechain.wallet.dapp.plugin.connex.GetAccountBalance;
import com.vechain.wallet.dapp.plugin.connex.GetAccountCode;
import com.vechain.wallet.dapp.plugin.connex.GetAccountMethod;
import com.vechain.wallet.dapp.plugin.connex.GetAccountStatus;
import com.vechain.wallet.dapp.plugin.connex.GetAccountStorage;
import com.vechain.wallet.dapp.plugin.connex.GetBlock;
import com.vechain.wallet.dapp.plugin.connex.GetGenesisBlock;
import com.vechain.wallet.dapp.plugin.connex.GetMethodAsClause;
import com.vechain.wallet.dapp.plugin.connex.GetTransaction;
import com.vechain.wallet.dapp.plugin.connex.GetTransactionReceipt;
import com.vechain.wallet.dapp.plugin.connex.Owned;
import com.vechain.wallet.dapp.plugin.connex.Sign;
import com.vechain.wallet.dapp.plugin.connex.ThorExplain;
import com.vechain.wallet.dapp.plugin.connex.ThorFilter;
import com.vechain.wallet.dapp.plugin.connex.TickerNext;
import com.vechain.wallet.dapp.plugin.inject.AddDAppAction;
import com.vechain.wallet.dapp.plugin.inject.DAppAction;
import com.vechain.wallet.dapp.plugin.web3.GetAccountAddress;
import com.vechain.wallet.dapp.plugin.web3.GetBalance;
import com.vechain.wallet.dapp.plugin.web3.GetNodeHost;
import com.vechain.wallet.dapp.plugin.web3.SendTransaction;
import com.vechain.wallet.dapp.utils.WebUtils;
import com.vechain.wallet.network.utils.GsonUtils;


import java.util.Iterator;

public class PluginManager {

    // List of service entries
    private final ArrayMap<String, PluginEntry> entries = new ArrayMap<String, PluginEntry>();

    private WebView webView;

    private DAppAction dAppAction;

    // Flag to track first time through
    private boolean firstRun;

    public PluginManager() {
        this.firstRun = true;
        init();
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    public void setdAppAction(DAppAction dAppAction) {
        this.dAppAction = dAppAction;
    }

    private void init() {
        // If first time, then load plugins from config.xml file
        if (this.firstRun) {

            //functions provided by Web3
            addService(GetAccountAddress.get());
            addService(GetBalance.get());
            addService(GetNodeHost.get());
            addService(SendTransaction.get());

            //functions provided by connex
            addService(GetAccountBalance.get());
            addService(GetAccountCode.get());
            addService(GetAccountStorage.get());
            addService(GetAccountStatus.get());
            addService(GetGenesisBlock.get());
            addService(GetBlock.get());
            addService(GetTransaction.get());
            addService(GetTransactionReceipt.get());
            addService(GetMethodAsClause.get());
            addService(Sign.get());
            addService(TickerNext.get());
            addService(GetAccountMethod.get());
//            addService(GetAccountEvent.get());
            addService(ThorExplain.get());
            addService(ThorFilter.get());
            addService(Owned.get());

            this.firstRun = false;
        }
    }


    public void addService(PluginEntry entry) {
        this.entries.put(entry.service, entry);
    }

    public void exec(final JSRequest request, String rawJson, final JsPromptResult result) {

        execHelper(request, rawJson, result);
    }

    private void execHelper(final JSRequest request, String rawJson, final JsPromptResult result) {
        BasePlugin plugin = getPlugin(request.getMethod());
        if (plugin == null) {
            //No corresponding service was found.
            String response = GsonUtils.toJson(WalletResponse.notFoundMethod(request));
            result.confirm(response);

            String callbackId = request.getCallbackId();
            String requestId = request.getRequestId();
            if (!TextUtils.isEmpty(callbackId) && !TextUtils.isEmpty(requestId)) {
                NativeCallJS callJSUtils = new NativeCallJS();
                callJSUtils.setWebView(webView);
                String callbackMsg = GsonUtils.toJson(response);
                callJSUtils.callJavaScript(callbackId + requestId, callbackMsg);
            }
            return;
        }

        plugin.execute(request, rawJson, result);
    }

    /**
     * Get the plugin object that implements the service. If the plugin object
     * does not already exist, then create it. If the service doesn't exist,
     * then return null.
     *
     * @param service The name of the service.
     * @return BasePlugin or null
     */
    public BasePlugin getPlugin(String service) {
        PluginEntry entry = this.entries.get(service);
        if (entry == null) {
            return null;
        }
        BasePlugin plugin = entry.createPlugin(this.webView);

        //Inject JS functionality implemented by developers
        if (plugin instanceof AddDAppAction) {
            AddDAppAction action = (AddDAppAction) plugin;
            action.setdAppAction(dAppAction);
        }

        return plugin;
    }

    public void clearWebView() {
        webView = null;
        Iterator<String> iterator = entries.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            PluginEntry entry = this.entries.get(key);

            if (entry == null) continue;

            BasePlugin plugin = entry.plugin;

            if (plugin != null) {
                plugin.closeWebView();

                //Inject JS functions implemented by developers, recycle and empty
                if (plugin instanceof AddDAppAction) {
                    AddDAppAction action = (AddDAppAction) plugin;
                    action.setdAppAction(null);
                }
            }
        }
    }
}
