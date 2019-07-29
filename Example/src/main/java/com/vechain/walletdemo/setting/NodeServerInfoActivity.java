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

package com.vechain.walletdemo.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.wallet.network.Api;
import com.vechain.wallet.network.NodeUrl;
import com.vechain.walletdemo.R;

import java.util.concurrent.TimeUnit;


public class NodeServerInfoActivity extends Activity {


    public static void open(Context context){
        Intent intent = new Intent(context, NodeServerInfoActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_info);

        initView();

    }

    private void initView() {
        ImageView leftBackImageView = findViewById(R.id.leftBackIcon);
        TextView titleTextView = findViewById(R.id.title);


        leftBackImageView.setImageResource(R.drawable.icon_back_black);
        RxView.clicks(leftBackImageView)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    finish();
                });

        int color = getResources().getColor(R.color.color_202c56);
        titleTextView.setTextColor(color);


        TextView hostInfoTextView = findViewById(R.id.host_info);
        TextView typeTextView = findViewById(R.id.type);
        TextView urlTextView = findViewById(R.id.url);

        boolean isDebug = false;
        if (Api.getNodeUrl() == NodeUrl.MAIN_NODE) {
            isDebug = false;
        } else if (Api.getNodeUrl() == NodeUrl.TEST_NODE) {
            isDebug = true;
        }

        String host = Api.getBlockChainHost();

        if (isDebug) {
            titleTextView.setText(R.string.host_info_test);
            hostInfoTextView.setText(R.string.host_info_test_msg);
            typeTextView.setText(R.string.host_info_test);
        } else {
            titleTextView.setText(R.string.host_info_release);
            hostInfoTextView.setText(R.string.host_info_release_msg);
            typeTextView.setText(R.string.host_info_release);
        }
        urlTextView.setText(host);
    }
}
