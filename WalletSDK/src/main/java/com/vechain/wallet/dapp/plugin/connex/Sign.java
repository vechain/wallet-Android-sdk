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

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.JsPromptResult;

import com.vechain.wallet.dapp.bean.JSRequest;
import com.vechain.wallet.dapp.bean.request.RequestConnexSignCert;
import com.vechain.wallet.dapp.bean.request.RequestConnexSignTx;
import com.vechain.wallet.dapp.bean.request.connex.ConnexAnnex;
import com.vechain.wallet.dapp.bean.request.connex.ConnexCertPayload;
import com.vechain.wallet.dapp.bean.request.connex.ConnexCertResult;
import com.vechain.wallet.dapp.bean.request.connex.ConnexCertificate;
import com.vechain.wallet.dapp.bean.request.connex.ConnexSignOptions;
import com.vechain.wallet.dapp.bean.request.connex.ConnexTxMessage;
import com.vechain.wallet.dapp.bean.response.ResponseConnexSign;
import com.vechain.wallet.dapp.plugin.BasePlugin;
import com.vechain.wallet.dapp.plugin.PluginEntry;
import com.vechain.wallet.dapp.plugin.inject.AddDAppAction;
import com.vechain.wallet.dapp.plugin.inject.CertificateCallback;
import com.vechain.wallet.dapp.plugin.inject.DAppAction;
import com.vechain.wallet.dapp.plugin.inject.TransferResultCallback;
import com.vechain.wallet.dapp.utils.ClauseUtil;
import com.vechain.wallet.network.engine.ServerTimer;
import com.vechain.wallet.network.utils.GsonUtils;
import com.vechain.wallet.thor.tx.Clause;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class Sign extends BasePlugin implements AddDAppAction, TransferResultCallback, CertificateCallback {

    //sign
    private static String METHOD = "sign";

    public static PluginEntry get() {
        return PluginEntry.get(METHOD, Sign.class.getName());
    }

    private final String SIGN_TX = "tx";
    private final String SIGN_CERT = "cert";

    private DAppAction dAppAction;
    private JSRequest request;
    private ConnexCertificate certificate;


    public Sign() {

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

        String params = JSRequest.getParamsValue(rawJson, "params");
        if (TextUtils.isEmpty(params)) {
            //参数错误
            callbackRequestParamsError(null, request);
            return;
        }
        this.request = request;

        String kind = null;
        try {
            JSONObject jsonObject = new JSONObject(params);
            kind = jsonObject.getString("kind");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(kind) ||
                (!kind.equalsIgnoreCase(SIGN_TX) && !kind.equalsIgnoreCase(SIGN_CERT))) {
            //parameter error
            callbackRequestParamsError(null, request);
            return;
        }

        if (kind.equalsIgnoreCase(SIGN_TX)) {
            toSignTx(request, rawJson);
        } else {
            toSignCert(request, rawJson);
        }
    }


    private void toSignTx(final JSRequest request, final String rawJson) {

        RequestConnexSignTx requestConnexSignTx = JSRequest.convertParams(rawJson, RequestConnexSignTx.class);
        if (requestConnexSignTx == null) {
            //parameter error
            callbackRequestParamsError(null, request);
            return;
        }

        ConnexSignOptions options = requestConnexSignTx.getOptions();
        List<ConnexTxMessage> clauses = requestConnexSignTx.getClauses();
        String kind = requestConnexSignTx.getKind();
        if (kind == null || !SIGN_TX.equalsIgnoreCase(kind) || clauses == null
                || clauses.size() < 1) {
            //参数错误
            callbackRequestParamsError(null, request);
            return;
        }

        List<Clause> clauseList = new ArrayList<>();
        int size = clauses.size();
        for (int i = 0; i < size; i++) {
            Clause clause = new Clause();
            clause.setTo(clauses.get(i).getTo());
            clause.setValue(clauses.get(i).getValue());
            clause.setData(clauses.get(i).getData());

            clauseList.add(clause);
        }

        try {
            if (!ClauseUtil.checkClauseList(clauseList)) {
                // parameter error
                callbackRequestParamsError(null, request);
                return;
            }
        } catch (Exception e) {
            callbackRequestParamsError(null, request);
            return;
        }
        long gas = 0;
        if (options != null) gas = options.getGas();

        String signer = null;
        if (options != null) signer = options.getSigner();

        dAppAction.onTransfer(clauseList, gas, signer, this);

    }


    private void toSignCert(final JSRequest request, final String rawJson) {


        RequestConnexSignCert requestConnexSignCert = JSRequest.convertParams(rawJson, RequestConnexSignCert.class);
        if (requestConnexSignCert == null) {
            //parameter error
            callbackRequestParamsError(null, request);
            return;
        }
        String kind = requestConnexSignCert.getKind();
        ConnexCertificate certificate = requestConnexSignCert.getClauses();
        if (certificate == null || !kind.equalsIgnoreCase(SIGN_CERT)
                || TextUtils.isEmpty(certificate.getPurpose())) {
            //parameter error
            callbackRequestParamsError(null, request);
            return;
        }

        ConnexCertPayload payload = certificate.getPayload();
        if (payload == null || TextUtils.isEmpty(payload.getContent())
                || TextUtils.isEmpty(payload.getType())) {
            //parameter error
            callbackRequestParamsError(null, request);
            return;
        }
        ConnexSignOptions options = requestConnexSignCert.getOptions();
        String signer = null;
        if (options != null) signer = options.getSigner();
        certificate.setSigner(signer);

        String url = webView.getUrl();
        Uri uri = Uri.parse(url);
        String host = uri.getHost();

        certificate.setDomain(host);

        certificate.setTimestamp(ServerTimer.getServerTime() / 1000);
        if (TextUtils.isEmpty(signer))
            certificate.setSigner("%1$s");//Add Address to Authentication

        this.certificate = certificate;
        String message = getSortJson();

        dAppAction.onCertificate(message, signer, this);

    }

    public String getSortJson() {

        String json = GsonUtils.toJson(certificate);
        String payload = GsonUtils.toJson(certificate.getPayload());
        try {
            JSONObject jsonObject = new JSONObject(json);
            jsonObject.remove("signature");

            JSONObject payloadObject = new JSONObject(payload);
            JSONObject sortPayloadObject = sortJson(payloadObject);

            jsonObject.put("payload", sortPayloadObject);
            JSONObject sortJSONObject = sortJson(jsonObject);

            return sortJSONObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private JSONObject sortJson(JSONObject jsonObject) {
        if (jsonObject == null) return null;

        try {
            int size = jsonObject.length();
            //No content, or only one field
            if (size <= 1) return jsonObject;

            Iterator<String> iteratorKeys = jsonObject.keys();
            String[] names = new String[size];

            int i = 0;
            while (iteratorKeys.hasNext()) {
                names[i] = iteratorKeys.next();
                i++;
            }

            Arrays.sort(names, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {

                    int index = o1.compareTo(o2);

                    return index;
                }
            });

            JSONObject sortJSON = new JSONObject(jsonObject, names);

            return sortJSON;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    @Override
    public void onCertificate(String signerAddress, String signature) {

        if (signature != null && signerAddress != null) {
            signerAddress = signerAddress.toLowerCase();
            ConnexAnnex annex = new ConnexAnnex();
            annex.setDomain(certificate.getDomain());
            annex.setSigner(signerAddress);
            annex.setTimestamp(certificate.getTimestamp());

            ConnexCertResult result = new ConnexCertResult();
            result.setAnnex(annex);
            result.setSignature(signature);

            callbackSuccess(null, request, result);
        } else {
            callbackRequestParamsError(null, request);
        }
    }

    @Override
    public void onTransferResult(String signerAddress, String txId) {

        if (signerAddress != null && txId != null) {
            ResponseConnexSign connexSign = new ResponseConnexSign();
            connexSign.setSigner(signerAddress);
            connexSign.setTxId(txId);

            callbackSuccess(null, request, connexSign);
        } else {
            callbackRequestParamsError(null, request);
        }
    }

}

