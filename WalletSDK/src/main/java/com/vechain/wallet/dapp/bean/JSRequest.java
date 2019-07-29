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

package com.vechain.wallet.dapp.bean;


import com.google.gson.JsonPrimitive;
import com.vechain.wallet.network.utils.GsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class JSRequest<T> {

    //App Receive Method Name
    private String method;

    //parameter
    private T params;

    //Callback to the H5 method name
    private String callbackId;

    //random string
    private String requestId;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public T getParams() {
        return params;
    }

    public void setParams(T params) {
        this.params = params;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }


    public static <E> E convertParams(String rawJson, Class<E> classOfE){

        String json = getParamsValue(rawJson,"params");

        E e = GsonUtils.fromJson(json,classOfE);

        return e;
    }

    public static String getParamsValue(String rawJson,String name){
        String paramsJson = null;
        try {
            JSONObject jsonObject = new JSONObject(rawJson);
            paramsJson = jsonObject.getString(name);
            JsonPrimitive jsonPrimitive = new JsonPrimitive(paramsJson);
            paramsJson = jsonPrimitive.getAsString();
        }catch (JSONException e){
            e.printStackTrace();
        }

        return paramsJson;
    }

}
