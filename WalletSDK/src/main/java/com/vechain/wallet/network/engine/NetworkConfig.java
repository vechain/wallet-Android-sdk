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

package com.vechain.wallet.network.engine;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkConfig {


    //single instance
    public static NetworkConfig get() {
        return ConfigHolder.config;
    }

    private static class ConfigHolder {
        private static final NetworkConfig config = new NetworkConfig();
    }

    private static final String HOST = "https://vethor-node.digonchain.com";

    public static final int DEFAULT_TIMEOUT = 30;
    public static final int CONNECT_TIMEOUT = 30;


    private Retrofit retrofit;
    private ApiDescribe apiDescribe;

    private NetworkConfig(){


        OkHttpClient.Builder builder = new OkHttpClient.Builder()
//                .cache(cache)
                .retryOnConnectionFailure(true)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(HOST)
                .build();

        apiDescribe = retrofit.create(ApiDescribe.class);
    }

    public ApiDescribe getApi(){
        return  apiDescribe;
    }


    public Flowable<Response<ResponseBody>> load(String url, HashMap<String, String> header, HashMap<String, String> querys) {
        if (header == null) header = new HashMap<>();
        if (querys == null) querys = new HashMap<>();
        if (apiDescribe == null)
            apiDescribe = retrofit.create(ApiDescribe.class);
        url = getQuesysUrl(url, querys);
        return apiDescribe.load(header, url);
    }


    public Flowable<Response<ResponseBody>> load(String url, HashMap<String, String> header, HashMap<String, String> querys, Object body) {
        if (header == null) header = new HashMap<>();
        if (querys == null) querys = new HashMap<>();
        if (apiDescribe == null)
            apiDescribe = retrofit.create(ApiDescribe.class);
        url = getQuesysUrl(url, querys);
        return apiDescribe.load(header, url, body);
    }




    private String getQuesysUrl(String url, HashMap<String, String> querys) {
        if (querys == null || querys.isEmpty()) return url;
        if (!url.contains("?")) {
            url = url + "?";
        }
        StringBuffer buffer = new StringBuffer();
        Iterator<String> iterator = querys.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = "";
            try {
                value = URLEncoder.encode(querys.get(key), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                value = "";
            }
            buffer.append(key).append("=").append(value);
            if (iterator.hasNext()) buffer.append("&");
        }
        url = url + buffer.toString();
        buffer = null;
        return url;
    }


    public Call<ResponseBody> downloadSync(String url, HashMap<String,String> header){
        if (apiDescribe == null)
            apiDescribe = retrofit.create(ApiDescribe.class);

        Call<ResponseBody> call = apiDescribe.callDownload(header,url);
        return call;
    }



    public Flowable<Response<ResponseBody>> upload(String url,String fileType, HashMap<String, String> header, HashMap<String, String> querys, String filePath) {

        if (TextUtils.isEmpty(filePath)) return null;
        File file = new File(filePath);

        if(!file.exists())return null;
        if (header == null) header = new HashMap<>();
        if (querys == null) querys = new HashMap<>();

        url = getQuesysUrl(url, querys);

        if (apiDescribe == null)
            apiDescribe = retrofit.create(ApiDescribe.class);

        MultipartBody.Part body = getMulBodyPart(file,fileType);

        return apiDescribe.upload(header,url,body);
    }

    public Flowable<Response<ResponseBody>> upload(String url,String fileType, HashMap<String, String> header, HashMap<String, String> querys, List<String> files) {

        if(files==null || files.isEmpty())return null;

        HashMap<String, MultipartBody.Part> map = getMulBodyParts(files,fileType);

        if(map==null ||map.isEmpty()) return null;

        if (header == null) header = new HashMap<>();
        if (querys == null) querys = new HashMap<>();

        url = getQuesysUrl(url, querys);

        if (apiDescribe == null)
            apiDescribe = retrofit.create(ApiDescribe.class);


        return apiDescribe.upload(header,url, map);
    }


    private MultipartBody.Part getMulBodyPart(File file,String fileType){
        //requestbody
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        //resquestbody to MultipartBody.Part
        MultipartBody.Part body = MultipartBody.Part.createFormData(fileType, file.getName(), requestFile);

        return body;
    }

    private HashMap<String, MultipartBody.Part> getMulBodyParts(List<String> files,String fileType){
        if(files==null || files.isEmpty())return null;
        HashMap<String, MultipartBody.Part> map = new HashMap<>();
        for(String path:files){
            File file = new File(path);
            if(!file.exists())
                continue;
            MultipartBody.Part body = getMulBodyPart(file,fileType);
            map.put(path,body);
        }
        return map;
    }



}
