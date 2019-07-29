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

import android.webkit.JsPromptResult;

import com.vechain.wallet.dapp.bean.JSRequest;
import com.vechain.wallet.dapp.plugin.BasePlugin;
import com.vechain.wallet.dapp.plugin.PluginEntry;
import com.vechain.wallet.network.Api;
import com.vechain.wallet.network.bean.core.Block;
import com.vechain.wallet.network.engine.NetCallBack;


public class GetGenesisBlock extends BasePlugin {

    private static String METHOD = "getGenesisBlock";

    public static PluginEntry get(){
        return PluginEntry.get(METHOD,GetGenesisBlock.class.getName());
    }

    public void execute(final JSRequest request, final String rawJson, final JsPromptResult jsPromptResult) {

        String blockId = "0";

        Api.getBlock(blockId, new NetCallBack() {

            @Override
            public void onSuccess(Object result) {

                if(result!=null && result instanceof Block){
                    responseSuccess(jsPromptResult,request,result);
                }else{
                    responseDataError(jsPromptResult,request);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                responseThrowable(throwable,jsPromptResult,request);
            }
        },null);
    }
}
