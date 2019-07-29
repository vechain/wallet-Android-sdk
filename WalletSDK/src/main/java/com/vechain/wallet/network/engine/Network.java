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

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.vechain.wallet.network.utils.GsonUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;


public class Network {


    public static void load(String url, HashMap<String, String> header, HashMap<String, String> querys, Type type,
                            final NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {

        if (url == null || type == null) {
            if (callBack != null) {
                callBack.onError(null);
            }
            return;
        }


        Flowable<Response<ResponseBody>> response = NetworkConfig.get().load(url, header, querys);

        execute(response, type, callBack, provider);

    }

    public static void load(String url, HashMap<String, String> querys, Type type, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        load(url, null, querys, type, callBack, provider);
    }


    public static void load(String url, Type type, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {

        load(url, null, null, type, callBack, provider);
    }


    public static void upload(String url, HashMap<String, String> header, HashMap<String, String> querys, Object body, Type type,
                              final NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        if (url == null || type == null) {
            if (callBack != null) {
                callBack.onError(null);
            }
            return;
        }


        Flowable<Response<ResponseBody>> response = NetworkConfig.get().load(url, header, querys, body);

        execute(response, type, callBack, provider);

    }

    public static void upload(String url, HashMap<String, String> querys, Object body, Type type,
                              final NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        upload(url, null, querys, body, type, callBack, provider);
    }


    private static void execute(final Flowable<Response<ResponseBody>> responseFlowable, final Type type, final NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        Flowable<Object> nextFlowable = responseFlowable.subscribeOn(Schedulers.io())
                .map(new Function<Response<ResponseBody>, Object>() {

                    public Object apply(Response<ResponseBody> response) throws Exception {
                        //parse results
                        if (!response.isSuccessful()) {
                            throw new HttpException(response);
                        }
                        Object object = parse(response, type);
                        if (callBack != null) {
                            object = callBack.map(object);
                        }
                        return object;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());


        if (provider != null)
            nextFlowable = nextFlowable.compose(provider.bindUntilEvent(ActivityEvent.DESTROY));

        Disposable disposable = nextFlowable.subscribe(new Consumer<Object>() {
                                                           @Override
                                                           public void accept(Object result) throws Exception {
                                                               if (callBack != null) {
                                                                   callBack.onSuccess(result);
                                                               }
                                                           }
                                                       }, new Consumer<Throwable>() {
                                                           @Override
                                                           public void accept(Throwable throwable) {
                                                               if (callBack != null) {
                                                                   Throwable t  = (Throwable) throwable;
                                                                   //abnormal
                                                                   onThrowable(t, callBack);
                                                               }
                                                           }
                                                       }
        );

        //Notify page can cancel method
        if (callBack != null) {
            callBack.onDisposable(disposable);
        }
    }

    private static Object parse(Response<ResponseBody> response, Type type) {
        if (response == null) return null;
        Object result = null;
        try {
            //get server time
            Headers headers = response.headers();
            if (headers != null) {
                Date date = headers.getDate("date");
                if (date != null) {
                    long serverTime = date.getTime();
                    long localTime = System.currentTimeMillis();
                    long offset = serverTime - localTime;
                    ServerTimer.setTimeOffset(offset);
                }
            }
            ResponseBody body = response.body();
            if (body == null) return null;
            String msg = body.string();

            result = GsonUtils.fromJson(msg, type);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private static void onThrowable(Throwable throwable, final NetCallBack callBack) {
        try {
            if (throwable != null && throwable instanceof HttpException) {
                ResponseBody errorBody = ((HttpException) throwable).response().errorBody();
                String errorString;
                if (errorBody != null) {
                    errorString = errorBody.string();

                    BaseExceptionResult baseExceptionResult = GsonUtils.fromJson(errorString, BaseExceptionResult.class);

                    if (baseExceptionResult != null) {
                        int code = baseExceptionResult.getCode();
                        String message = baseExceptionResult.getMessage();
                        BaseException baseException = new BaseException(code, message);
                        callBack.onError(baseException);
                        return;
                    }
                }
            }

            callBack.onError(throwable);

        } catch (IOException exception) {
            callBack.onError(throwable);
        }
    }

    private static String getQueryString(HashMap<String, String> querys) {

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

        return buffer.toString();
    }


    public static Call<ResponseBody> download(String url, HashMap<String, String> header) {

        Call<ResponseBody> call = NetworkConfig.get().downloadSync(url, header);

        return call;
    }




}
