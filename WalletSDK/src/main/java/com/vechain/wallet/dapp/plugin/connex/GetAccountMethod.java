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
import com.vechain.wallet.dapp.bean.request.RequestConnexMethodAsCall;
import com.vechain.wallet.dapp.bean.request.connex.ConnexCallOptions;
import com.vechain.wallet.dapp.plugin.BasePlugin;
import com.vechain.wallet.dapp.plugin.PluginEntry;
import com.vechain.wallet.dapp.utils.AddressCheckUtil;
import com.vechain.wallet.network.Api;
import com.vechain.wallet.network.bean.core.call.CallData;
import com.vechain.wallet.network.bean.core.call.CallResult;
import com.vechain.wallet.network.engine.NetCallBack;
import com.vechain.wallet.utils.StringUtils;
import com.vechain.wallet.thor.tx.Clause;

import org.json.JSONArray;


public class GetAccountMethod extends BasePlugin {

    private static String METHOD = "methodAsCall";

    public static PluginEntry get() {
        return PluginEntry.get(METHOD, GetAccountMethod.class.getName());
    }

    public void execute(final JSRequest request, final String rawJson, final JsPromptResult jsPromptResult) {

        if (jsPromptResult != null) jsPromptResult.confirm("");
        // convert parameter
        RequestConnexMethodAsCall methodAsCall = JSRequest.convertParams(rawJson, RequestConnexMethodAsCall.class);


        //post simulate
        CallData callData = new CallData();

        ConnexCallOptions options = methodAsCall.getOpts();
        if (options != null) {

            String caller = options.getCaller();
            if (caller!=null && !isOkAddress(caller)) {
                //parameter error
                callbackRequestParamsError(null, request);
                return;
            }
            callData.setCaller(caller);


            int gas = options.getGas();
//            if (gas <= 0) {
//                //parameter error
//                callbackRequestParamsError(null, request);
//                return;
//            }
            callData.setGas(gas);

            String gasPrice = options.getGasPrice();
            if (!isOkDecOrHex(gasPrice)) {
                //parameter error
                responseRequestParamsError(null, request);
                return;
            }
            callData.setGasPrice(gasPrice);
        }

        String revision = methodAsCall.getRevision();

        Clause clause = methodAsCall.getClause();

        String contractAddress = clause.getTo();

        callData.setData(clause.getData());
        callData.setValue(clause.getValue());


        NetCallBack callBack = new NetCallBack() {
            @Override
            public void onSuccess(Object result) {
                if (result != null && result instanceof CallResult) {

//                    CallResult callResult = (CallResult) result;
//                    String data = callResult.getData();
//                    ArrayMap<String, Object> decodedMap = getDecodeMap(data, abi);
//
//                    callResult.setDecoded(decodedMap);

                    callbackSuccess(null, request, result);
                } else {
                    callbackDataError(null, request);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                callbackThrowable(throwable, null, request);
            }
        };

        if (contractAddress != null) {

            Api.simulateAContractCall(callData, contractAddress, revision, callBack, null);
        } else {
            Api.simulateAContractCall(callData, revision, callBack, null);
        }


    }


    private boolean isOkAddress(String address) {
        if (address == null || !StringUtils.isHexString(address)) {
            //Parameter error
            return false;
        }

        if (!AddressCheckUtil.checkAddressSum(address)) {
            //Parameter error
            return false;
        }
        return true;
    }

    private boolean isOkDecOrHex(String value) {
        if (value != null && !StringUtils.checkDecimalString(value) && !StringUtils.isHexString(value)) {
            //Parameter error
            return false;
        }
        return true;
    }

    private String object2String(Object object) {
        if (object == null) return null;
        String value = object.toString();
        int index = value.indexOf('.');//包含小数
        if (index >= 0) {
            value = value.substring(0, index);
        }

        return value;
    }


    private String[] getArgs(String rawJson) {

        String paramsJson = JSRequest.getParamsValue(rawJson, "params");
        String argsJson = JSRequest.getParamsValue(paramsJson, "args");

        String[] args = null;
        if (!TextUtils.isEmpty(argsJson)) {
            try {
                JSONArray jsonArray = new JSONArray(argsJson);
                int size = jsonArray.length();
                args = new String[size];

                for (int i = 0; i < size; i++)
                    args[i] = jsonArray.getString(i);

            } catch (Exception e) {

            }
        }
        return args;
    }





}

