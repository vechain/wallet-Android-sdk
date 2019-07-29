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

package com.vechain.wallet.dapp.plugin.web3;

import android.webkit.JsPromptResult;

import com.vechain.wallet.dapp.bean.JSRequest;
import com.vechain.wallet.dapp.bean.WalletResponse;
import com.vechain.wallet.dapp.bean.request.RequestWeb3Transaction;
import com.vechain.wallet.dapp.plugin.BasePlugin;
import com.vechain.wallet.dapp.plugin.PluginEntry;
import com.vechain.wallet.dapp.plugin.inject.AddDAppAction;
import com.vechain.wallet.dapp.plugin.inject.DAppAction;
import com.vechain.wallet.dapp.plugin.inject.TransferResultCallback;
import com.vechain.wallet.dapp.utils.ClauseUtil;
import com.vechain.wallet.utils.StringUtils;
import com.vechain.wallet.thor.tx.Clause;

import java.util.ArrayList;
import java.util.List;


public class SendTransaction extends BasePlugin implements AddDAppAction, TransferResultCallback {

    private static String METHOD = "send";

    private DAppAction dAppAction;
    private JSRequest request;


    public static PluginEntry get() {
        return PluginEntry.get(METHOD, SendTransaction.class.getName());
    }

    public SendTransaction() {
    }

    public void setdAppAction(DAppAction dAppAction) {
        this.dAppAction = dAppAction;
    }

    public void execute(final JSRequest request, final String rawJson, final JsPromptResult jsPromptResult) {

        if (jsPromptResult != null) jsPromptResult.confirm("");

        if (dAppAction == null) {
            callbackDAppInitError(null, request);
            return;
        }

        RequestWeb3Transaction requestWeb3Transaction = JSRequest.convertParams(rawJson, RequestWeb3Transaction.class);
        if (requestWeb3Transaction == null || dAppAction == null) {
            //参数错误
            callbackRequestParamsError(null, request);
            return;
        }

        String from = requestWeb3Transaction.getFrom();
        String to = requestWeb3Transaction.getTo();
        String data = requestWeb3Transaction.getData();
        String value = requestWeb3Transaction.getValue();
        String gas = requestWeb3Transaction.getGas();


        if (gas !=null && !StringUtils.checkDecimalString(gas)) {
            gas = StringUtils.hexString2DecimalString(gas);
        }

        long gasValue = StringUtils.string2Long(gas);

        Clause clause = new Clause();
        clause.setTo(to);
        clause.setValue(value);
        clause.setData(data);

        try {
            if (!ClauseUtil.checkClause(clause)) {
                //parameter error
                callbackRequestParamsError(null, request);
                return;
            }
        } catch (Exception e) {
            //parameter error
            callbackRequestParamsError(null, request);
            return;
        }

        this.request = request;
        List<Clause> clauseList = new ArrayList<>();
        clauseList.add(clause);

        dAppAction.onTransfer(clauseList, gasValue, from,this);
    }


    @Override
    public void onTransferResult(String signerAddress, String txId) {

        //success
        WalletResponse response = WalletResponse.success(request, txId);
        String callbackId = request.getCallbackId() + request.getRequestId();
        callback(callbackId, response, null);
    }
}
