
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

package com.vechain.walletdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.vechain.walletdemo.createwallet.NoWalletKeystoreActivity;
import com.vechain.walletdemo.utils.FileUtils;

import java.lang.ref.WeakReference;

/**
 * launcher
 */

public class LauncherActivity extends AppCompatActivity {


    private LauncherHandler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launcher);
        handler = new LauncherHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessageDelayed(0, 1500);
    }

    public void gotoPage() {
        String keystoreJson = getKeystore();
        if (!TextUtils.isEmpty(keystoreJson)) {
            //Keystore already exists
            HomeActivity.open(this, keystoreJson);
        } else {
            //No wallet has been created or imported locally
            NoWalletKeystoreActivity.open(this);
        }
        finish();
    }

    private String getKeystore() {
        //Is there a keystore locally?
        String keystore = FileUtils.readCache(this, FileUtils.KEYSTORE_NAME);
        return keystore;
    }


    private static class LauncherHandler extends Handler {

        private WeakReference<LauncherActivity> reference;

        public LauncherHandler(LauncherActivity activity) {
            reference = new WeakReference<LauncherActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (reference == null) return;
            LauncherActivity activity = reference.get();
            if (activity != null) activity.gotoPage();
        }
    }
}
