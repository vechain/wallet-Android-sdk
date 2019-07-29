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


import java.util.HashMap;

import io.reactivex.Flowable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;


public interface ApiDescribe {

    @GET
    Flowable<Response<ResponseBody>> load(@HeaderMap HashMap<String, String> header, @Url String url);

    @POST
    Flowable<Response<ResponseBody>> load(@HeaderMap HashMap<String, String> header, @Url String url, @Body Object body);

    //Synchronous use
    @Streaming
    @GET
    Call<ResponseBody> callDownload(@HeaderMap HashMap<String, String> header, @Url String url);


    //Upload a single file
    @Multipart
    @POST
    Flowable<Response<ResponseBody>> upload(@HeaderMap HashMap<String, String> header, @Url String url, @Part MultipartBody.Part file);

    //Upload multiple files
    @Multipart
    @POST
    Flowable<Response<ResponseBody>> upload(@HeaderMap HashMap<String, String> header, @Url String url, @PartMap HashMap<String, MultipartBody.Part> map);

}
