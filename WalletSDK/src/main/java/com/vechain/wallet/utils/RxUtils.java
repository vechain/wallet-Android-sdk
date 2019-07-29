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

package com.vechain.wallet.utils;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.android.ActivityEvent;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class RxUtils {

    public static <T, P> Disposable onFlowable(final BusinessCallBack<T, P> callBack, T params, LifecycleProvider<ActivityEvent> provider) {


        Flowable nextFlowable = Flowable.just(params)
                .map(new Function<T, P>() {
                    @Override
                    public P apply(T params) throws Exception {
                        P p = null;
                        if (callBack != null) p = callBack.map(params);
                        return p;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        if (provider != null)
            nextFlowable = nextFlowable.compose(provider.bindUntilEvent(ActivityEvent.DESTROY));

        Disposable disposable = nextFlowable.subscribe(new Consumer<P>() {
            @Override
            public void accept(P p) throws Exception {
                if (callBack != null) callBack.onSuccess(p);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                if (callBack != null) callBack.onError(throwable);
            }
        });

        return disposable;
    }


    public static  <T, P> Disposable onFlowable(final BusinessCallBack<T, P> callBack, T params){
       return onFlowable( callBack, params,  null);
    }


    public static abstract class BusinessCallBack<T, P> {
        public abstract P map(T t);

        public abstract void onSuccess(P p);

        public void onError(Throwable throwable) {
        }
    }


}
