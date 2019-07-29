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
import com.vechain.wallet.dapp.bean.request.RequestConnexOwned;
import com.vechain.wallet.dapp.plugin.BasePlugin;
import com.vechain.wallet.dapp.plugin.PluginEntry;
import com.vechain.wallet.dapp.plugin.inject.AddDAppAction;
import com.vechain.wallet.dapp.plugin.inject.DAppAction;
import com.vechain.wallet.dapp.plugin.inject.OwnedAddressCallback;
import com.vechain.wallet.utils.HexUtils;

public class Owned extends BasePlugin implements AddDAppAction, OwnedAddressCallback {

    private static String METHOD = "owned";

    public static PluginEntry get() {
        return PluginEntry.get(METHOD, Owned.class.getName());
    }

    private DAppAction dAppAction;
    private JSRequest request;
    private JsPromptResult jsPromptResult;

    @Override
    public void setdAppAction(DAppAction dAppAction) {
        this.dAppAction = dAppAction;
    }

    public void execute(final JSRequest request, final String rawJson, final JsPromptResult jsPromptResult) {

        if (dAppAction == null) {
            responseRequestParamsError(jsPromptResult, request);
            return;
        }
        this.request = request;
        this.jsPromptResult = jsPromptResult;
        //covert parameter
        RequestConnexOwned connexOwned = JSRequest.convertParams(rawJson, RequestConnexOwned.class);

        if (connexOwned == null || TextUtils.isEmpty(connexOwned.getAddress())) {
            responseRequestParamsError(jsPromptResult, request);
            return;
        }

        String address = connexOwned.getAddress();
        address = HexUtils.HEX_PREFIX + HexUtils.cleanHexPrefix(address);

        dAppAction.onCheckOwnAddress(address, this);
    }

    @Override
    public void onOwned(boolean isOwn) {

        responseSuccess(jsPromptResult, request, isOwn);
    }
}
