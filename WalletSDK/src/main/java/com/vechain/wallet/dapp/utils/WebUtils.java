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

package com.vechain.wallet.dapp.utils;


import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.vechain.wallet.network.Api;
import com.vechain.wallet.network.bean.common.HttpResult;
import com.vechain.wallet.network.bean.version.LastVersion;
import com.vechain.wallet.network.bean.version.Version;
import com.vechain.wallet.network.engine.NetCallBack;
import com.vechain.wallet.network.utils.GsonUtils;
import com.vechain.wallet.utils.RxUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class WebUtils {

    private final static String[] JS_FILES = {"dapp/connex.js", "dapp/web3.js"};

    public static void injectWalletJavaScript(final WebView webView){
        if (webView != null) {
            String js = "javascript:(function(){if(!window.connex){return false;}else{ return true;}})()";
            webView.evaluateJavascript(js, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String exist) {

                    if(exist!=null && exist.equalsIgnoreCase("false")){
                        injectJavaScript(webView);
                    }
                }
            });
        }
    }

    private static void injectJavaScript(final WebView webView) {

        for (String fileName : JS_FILES) {
            injectFile(webView, fileName);
        }
    }

    private static void injectFile(final WebView webView, String fileName) {
        if (webView == null) return;
        if (fileName == null || fileName.isEmpty()) return;

        RxUtils.onFlowable(new RxUtils.BusinessCallBack<String, String>() {
            @Override
            public String map(String fileName) {
                Context context = webView.getContext();
                AssetManager assetManager = context.getResources().getAssets();
                String js = readJavaScript(assetManager, fileName);
                return js;
            }

            @Override
            public void onSuccess(String js) {

                if (webView != null) {
                    webView.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }
        }, fileName);

    }

    private static String readJavaScript(AssetManager assetManager, String fileName) {
        try {
            InputStream inputStream = assetManager.open(fileName);
            return "javascript:" + readStringFromInputStream(inputStream);
        } catch (Exception e) {

        }
        return null;
    }

    private static String readJavaScriptNoHead(AssetManager assetManager, String fileName) {
        try {
            InputStream inputStream = assetManager.open(fileName);
            return readStringFromInputStream(inputStream);
        } catch (Exception e) {

        }
        return null;
    }

    private static String readStringFromInputStream(InputStream inputStream) {
        String content = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int length = -1;
            while ((length = inputStream.read(buffer)) > -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            byteArrayOutputStream.close();
            inputStream.close();

            byte[] data = byteArrayOutputStream.toByteArray();

            content = new String(data, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }


    private static final String DEFAULT_CHARSET = "utf-8";
    private static final String DEFAULT_MIME_TYPE = "text/html";
    private static final String ASSETS_DAPP_JS = "androidvechainwallet";
    private  boolean isGetTypeInject = true;

    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

        Uri uri = request.getUrl();

        if (isGetTypeInject && request.isForMainFrame() && request.getMethod().equalsIgnoreCase("GET")) {
            //version update check
            updateVersion(view.getContext());
            //inject our JS when loading resources
            WebResourceResponse response = shouldInterceptForMainFrameRequest(view, request);
            if (response != null)
                return response;
        }else if(request.isForMainFrame() && request.getMethod().equalsIgnoreCase("POST")){
            isGetTypeInject = false;
        }

        String host = uri.getHost();
        if (host != null && host.equalsIgnoreCase(WebUtils.ASSETS_DAPP_JS)) {
            //replace https://androidvechainwallet/*** resources
            String path = uri.getPath();
            WebResourceResponse response = getAssetsWebResource(view, path);
            if (response != null)
                return response;
        }

        return null;
    }

    private static WebResourceResponse shouldInterceptForMainFrameRequest(WebView view, WebResourceRequest request) {

        Map<String, String> headers = request.getRequestHeaders();
//        headers.put("Accept", "*/*");
//        headers.put("Cache-Control", "no-cache");

        Context context = view.getContext();

        String url = request.getUrl().toString();
        String method = request.getMethod();
        JsInjectorResponse response;
        try {
            response = loadUrl(context, method, url, headers);
        } catch (Exception ex) {
            return null;
        }
        if (response == null || response.data == null) {
            return null;
        } else {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(response.data.getBytes());
            WebResourceResponse webResourceResponse = new WebResourceResponse(
                    response.mime, response.charset, response.code, response.reasonPhrase, response.responseHeaders, inputStream);
            return webResourceResponse;
        }
    }

    @Nullable
    private static JsInjectorResponse loadUrl(Context context, String method, final String url, final Map<String, String> headers) {

        HashMap<String, String> hashMap = new HashMap<>();
        if (headers != null)
            hashMap.putAll(headers);

        JsInjectorResponse result = null;
        try {
            Call<ResponseBody> call = Api.getUrl(url, hashMap);
            Response<ResponseBody> response = call.execute();
            result = buildResponse(context, response);
        } catch (Exception ex) {
            // Log.d("REQUEST_ERROR", "", ex);
            ex.printStackTrace();
        }
        return result;
    }


    @Nullable
    private static JsInjectorResponse buildResponse(Context context, Response<ResponseBody> response) {
        String html = null;
        int code = response.code();
        ResponseBody body = null;
        try {
            //if (response.isSuccessful()) {
            body = response.body();
            //}
            if (body != null) {
                html = body.string();
            }
            if (!response.isSuccessful() && html == null)
                return null;
        } catch (IOException e) {
            // Log.d("READ_BODY_ERROR", "Ex", e);
            e.printStackTrace();
        }
        okhttp3.Response okhttp3Response = response.raw();
        Request request = okhttp3Response.request();
        Headers headers = okhttp3Response.headers();
        String reasonPhrase = response.message();
        if (TextUtils.isEmpty(reasonPhrase)) {
            if (code >= 200 && code <= 399)
                reasonPhrase = "OK";
            else
                reasonPhrase = "Error";
        }
        Map<String, String> headMap = getHeadMap(headers);
        okhttp3.Response prior = okhttp3Response.priorResponse();
        boolean isRedirect = prior != null && prior.isRedirect();
        String result = injectJavaScript(context, html);
        String contentType = getContentTypeHeader(okhttp3Response);
        String charset = getCharset(contentType);
        String mime = getMimeType(contentType);
        String finalUrl = request.url().toString();
        return new JsInjectorResponse(result, code, reasonPhrase, finalUrl, mime, charset, isRedirect, headMap);
    }

    private static Map<String, String> getHeadMap(Headers headers) {
        if (headers == null) return null;
        Set<String> headNameSet = headers.names();
        if (headNameSet == null) return null;
        Iterator<String> iterator = headNameSet.iterator();
        Map<String, String> headMap = new HashMap<>();
        while (iterator.hasNext()) {
            String name = iterator.next();
            String value = headers.get(name);
            headMap.put(name, value);
        }

        return headMap;
    }

    private static String injectJavaScript(Context context, String message) {

        if (message == null || message.isEmpty()) return message;

        StringBuffer buffer = new StringBuffer();
        for (String fileName : JS_FILES) {

            buffer.append("<script type=\"text/javascript\" ");
            buffer.append("src=\"https://").append(ASSETS_DAPP_JS).append("/").append(fileName).append("\";");
            buffer.append("></script>");

        }

        String headTag = "<head>";

        String myScript = buffer.toString();

        int index = message.indexOf(headTag);
        if (index > 0) {
            StringBuffer htmlBuffer = new StringBuffer();
            htmlBuffer.append(message);

            int length = headTag.length();
            htmlBuffer.insert(index + length, myScript);
            message = htmlBuffer.toString();
        }

        return message;
    }

    private static String getMimeType(String contentType) {
        Matcher regexResult = Pattern.compile("^.*(?=;)").matcher(contentType);
        if (regexResult.find()) {
            return regexResult.group();
        }
        return DEFAULT_MIME_TYPE;
    }

    private static String getCharset(String contentType) {
        Matcher regexResult = Pattern.compile("charset=([a-zA-Z0-9-]+)").matcher(contentType);
        if (regexResult.find()) {
            if (regexResult.groupCount() >= 2) {
                return regexResult.group(1);
            }
        }
        return DEFAULT_CHARSET;
    }

    @Nullable
    private static String getContentTypeHeader(okhttp3.Response response) {
        Headers headers = response.headers();
        String contentType;
        if (TextUtils.isEmpty(headers.get("Content-Type"))) {
            if (TextUtils.isEmpty(headers.get("content-Type"))) {
                contentType = "text/data; charset=utf-8";
            } else {
                contentType = headers.get("content-Type");
            }
        } else {
            contentType = headers.get("Content-Type");
        }
        if (contentType != null) {
            contentType = contentType.trim();
        }
        return contentType;
    }


    private static WebResourceResponse getAssetsWebResource(WebView view, String fileName) {
        //Get the JS file content in assets
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }

        Context context = view.getContext();
        AssetManager assetManager = context.getResources().getAssets();
        String js = readJavaScriptNoHead(assetManager, fileName);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(js.getBytes());
        WebResourceResponse webResourceResponse = new WebResourceResponse(
                DEFAULT_MIME_TYPE, DEFAULT_CHARSET, inputStream);
        return webResourceResponse;
    }

    private static class JsInjectorResponse {
        final int code;
        final String reasonPhrase;
        final String data;
        final String url;
        final String mime;
        final String charset;
        final boolean isRedirect;
        final Map<String, String> responseHeaders;

        JsInjectorResponse(String data, int code, String reasonPhrase, String url, String mime, String charset, boolean isRedirect, Map<String, String> responseHeaders) {
            this.code = code;
            this.reasonPhrase = reasonPhrase;
            this.data = data;
            this.url = url;
            this.mime = mime;
            this.charset = charset;
            this.isRedirect = isRedirect;
            this.responseHeaders = responseHeaders;
        }
    }


    private static boolean hasLoadVersion = false;
    private static boolean isMustUpdate = false;
    private static boolean canUpdate = false;
    private static LastVersion lastVersion;

    public static boolean isMustUpdate() {
        return isMustUpdate;
    }

    private static void updateVersion(Context context) {

        logVersion();
        if (isMustUpdate || canUpdate || hasLoadVersion) return;
        hasLoadVersion = true;

        String language = getLanguage(context);
        Api.getVersion(language, new NetCallBack() {
            @Override
            public void onSuccess(Object result) {

                hasLoadVersion = false;
                if (result != null && result instanceof HttpResult) {
                    HttpResult<LastVersion> httpResult = (HttpResult) result;
                    lastVersion = httpResult.getData();
                }
                if (lastVersion != null) {
                    isMustUpdate = (lastVersion.getUpdate() > 0);
                    String versionCode = lastVersion.getLatestVersion();
                    canUpdate = !(Version.VERSION.equals(versionCode));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                hasLoadVersion = false;
            }
        }, null);

    }

    public static void logVersion() {
        String TAG = "VeChain";
        if (isMustUpdate) {
            if (lastVersion != null) {

                String time = GsonUtils.toJson(new Date(lastVersion.getReleasets() * 1000));
                Log.e(TAG, "Wallet SDK update version url:" + lastVersion.getUrl());
                Log.e(TAG, "Current version:" + Version.VERSION);
                Log.e(TAG, "Latest version:" + lastVersion.getLatestVersion());
                Log.e(TAG, "WHAT'S NEW:" + lastVersion.getDescription());
                Log.e(TAG, "Updated:" + time);

            }
        } else if (canUpdate) {
            if (lastVersion != null) {

                String time = GsonUtils.toJson(new Date(lastVersion.getReleasets() * 1000));
                Log.d(TAG, "Wallet SDK update version url:" + lastVersion.getUrl());
                Log.d(TAG, "Current version:" + Version.VERSION);
                Log.d(TAG, "Latest version:" + lastVersion.getLatestVersion());
                Log.d(TAG, "WHAT'S NEW:" + lastVersion.getDescription());
                Log.d(TAG, "Updated:" + time);
            }
        }
    }

    private static Locale getLocale(Resources res) {
        Configuration config = res.getConfiguration();
        return Build.VERSION.SDK_INT >= 24 ? config.getLocales().get(0) : config.locale;
    }

    private static String getLanguage(Context context) {

        Resources resources = context.getResources();
        Locale locale = getLocale(resources);
        String defaultLanguage = "en";

        String current = locale.getLanguage();
        //If the user does not set the language and the user's mobile language is Chinese, the default value is Chinese.
        if (current.contains("zh")) {
            defaultLanguage = "zh_Hans";
        }
        return defaultLanguage;
    }

}
