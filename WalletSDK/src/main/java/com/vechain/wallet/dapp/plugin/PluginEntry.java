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


import android.webkit.WebView;

import com.vechain.wallet.dapp.plugin.connex.GetAccountStatus;

public class PluginEntry {


    public static PluginEntry get(String service, String pluginClass) {
        PluginEntry pluginEntry = new PluginEntry(service, pluginClass);
        return pluginEntry;
    }

    public String service = "";


    public String pluginClass = "";

    public BasePlugin plugin = null;


    public PluginEntry(String service, String pluginClass) {
        this.service = service;
        this.pluginClass = pluginClass;
    }


    public BasePlugin createPlugin(WebView webView) {
        if (this.pluginClass.equals(GetAccountStatus.class.getName())) {
            if (this.plugin != null) {
                this.plugin.initialize(webView);
                return this.plugin;
            }
        }
        try {
            @SuppressWarnings("rawtypes")
            Class c = getClassByName(this.pluginClass);
            if (isPlugin(c)) {
                BasePlugin plugin = (BasePlugin) c.newInstance();
                plugin.initialize(webView);
                if (this.pluginClass.equals(GetAccountStatus.class.getName()))
                    this.plugin = plugin;
                return plugin;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private Class getClassByName(final String clazz) throws ClassNotFoundException {
        Class c = null;
        if (clazz != null) {
            c = Class.forName(clazz);
        }
        return c;
    }

    @SuppressWarnings("rawtypes")
    private boolean isPlugin(Class c) {
        if (c != null) {
            return BasePlugin.class.isAssignableFrom(c);
        }
        return false;
    }

    public String getService() {
        return service;
    }

    public String getPluginClass() {
        return pluginClass;
    }

    public BasePlugin getPlugin() {
        return plugin;
    }
}
