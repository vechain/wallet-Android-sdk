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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.wallet.network.NodeUrl;
import com.vechain.walletdemo.R;

import java.util.concurrent.TimeUnit;

public class NodeServerSettingActivity extends Activity {


    public final static int REQUEST_URL_CODE = 9;

    public final static void open(Activity activity){
        Intent intent = new Intent(activity, NodeServerSettingActivity.class);
        activity.startActivityForResult(intent,REQUEST_URL_CODE);
    }


    private EditText inputHostNameEditText;
    private EditText inputUrlEditText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_setting);
        initView();
    }

    private void initView() {
        ImageView leftBackImageView = findViewById(R.id.leftBackIcon);
        TextView titleTextView = findViewById(R.id.title);
        titleTextView.setText(R.string.host_setting_title);

        leftBackImageView.setImageResource(R.drawable.icon_back_black);
        RxView.clicks(leftBackImageView)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    finish();
                });

        int color = getResources().getColor(R.color.color_202c56);
        titleTextView.setTextColor(color);

        inputHostNameEditText = findViewById(R.id.inputHostName);
        inputUrlEditText = findViewById(R.id.inputHost);

        String name  = NodeUrl.CUSTOM_NODE.getName();
        String url = NodeUrl.CUSTOM_NODE.getUrl();

        inputHostNameEditText.setText(name);
        inputUrlEditText.setText(url);

        Button button = findViewById(R.id.confirm);
        RxView.clicks(button)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    confirmedInputHost();
                });
    }


    private void confirmedInputHost() {
        String url = inputUrlEditText.getText().toString();
        String name = inputHostNameEditText.getText().toString();
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(name)) {
            return;
        }

        NodeUrl.CUSTOM_NODE.setUrl(url);
        NodeUrl.CUSTOM_NODE.setName(name);

        setResult(Activity.RESULT_OK);
        finish();
    }

}
