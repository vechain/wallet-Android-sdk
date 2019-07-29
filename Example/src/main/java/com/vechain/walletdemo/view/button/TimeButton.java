
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

package com.vechain.walletdemo.view.button;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;


public class TimeButton extends android.support.v7.widget.AppCompatButton {

    private int downTimeStringId;
    private boolean isUnit = true;

    private Resources resources;

    private Disposable disposable;

    public TimeButton(Context context) {
        super(context);
        initView(context);
    }

    public TimeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TimeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView(context);
    }

    private void initView(Context context) {
        resources = context.getResources();
    }

    public void setUnit(boolean unit) {
        isUnit = unit;
    }

    public void setDownTimeStringId(int stringId) {
        this.downTimeStringId = stringId;
        setEnabled(true);
        String msg = resources.getString(downTimeStringId, "");
        TimeButton.this.setText(msg);
    }

    public void startCountdown(final long count, final CountListener countListener) {
        if(count<=0){
            setEnabled(true);
            String msg = resources.getString(downTimeStringId, "");
            TimeButton.this.setText(msg);
            return;
        }
        setEnabled(false);
        if (countListener != null) {
            countListener.onStartCountListener();
        }
        //倒计时count秒
        //设置0延迟，每隔一秒发送一条数据
        disposable = Flowable.interval(0, 1, TimeUnit.SECONDS)
                .take(count + 1) //设置循环次数
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long value) throws Exception {

                        return count - value;
                    }
                })
                //线程切换，将观察者切换到主线程中，才可以更新UI什么的
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEach(new Subscriber<Long>() {
                    @Override
                    public void onSubscribe(Subscription s) {

                    }

                    @Override
                    public void onNext(Long progress) {
                        String msg = resources.getString(downTimeStringId, getTimeString(progress));
                        TimeButton.this.setText(msg);
                        setEnabled(false);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {
                        setEnabled(true);
                        if (countListener != null) {
                            countListener.onEndCountListener();
                        }
                        String msg = resources.getString(downTimeStringId, "");
                        TimeButton.this.setText(msg);
                    }
                })
                .subscribe();
    }

    private String getTimeString(Long progress){
        if(isUnit) {
            return "(" + String.valueOf(progress) + "s)";
        }else{
            return  String.valueOf(progress);
        }
    }

    public void stopTime() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    public interface CountListener{
        void onStartCountListener();
        void onEndCountListener();
    }

}
