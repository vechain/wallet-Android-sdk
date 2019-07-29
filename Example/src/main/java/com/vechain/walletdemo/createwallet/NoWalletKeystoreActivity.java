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

package com.vechain.walletdemo.createwallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.walletdemo.R;
import com.vechain.walletdemo.importwallet.ImportWalletActivity;

import java.util.concurrent.TimeUnit;

/**
 * No wallet has been created or imported locally
 */
public class NoWalletKeystoreActivity extends AppCompatActivity {

    public static void open(Context context){
        Intent intent = new Intent(context,NoWalletKeystoreActivity.class);
        context.startActivity(intent);
    }

    private Button createButton;
    private Button importButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_wallet_keystore);

        initView();
    }

    private void initView(){
        createButton = findViewById(R.id.createWallet);
        importButton = findViewById(R.id.importKeystore);

        RxView.clicks(createButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //Create Wallet
                    CreateWalletActivity.open(NoWalletKeystoreActivity.this);
                    NoWalletKeystoreActivity.this.finish();
                });

        RxView.clicks(importButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //Import Wallet
                    ImportWalletActivity.open(NoWalletKeystoreActivity.this);
                    NoWalletKeystoreActivity.this.finish();
                });
    }



}
