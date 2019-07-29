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

package com.vechain.walletdemo.managewallet;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.wallet.WalletUtils;
import com.vechain.walletdemo.R;
import com.vechain.walletdemo.utils.FileUtils;

import java.util.concurrent.TimeUnit;

/**
 * Delete your wallet and change your keystore password
 */

public class ManageWalletActivity extends AppCompatActivity {

    public static final int WALLET_MANAGER = 901;
    private static final String KEYSTORE = "keystore";

    public final static void open(Activity context, String keystoreJson) {

        if (TextUtils.isEmpty(keystoreJson)) return;

        Intent intent = new Intent(context, ManageWalletActivity.class);
        intent.putExtra(KEYSTORE, keystoreJson);

        context.startActivityForResult(intent,WALLET_MANAGER);
    }


    private TextView keystoreTextView;

    private String keystoreJson;
    private String walletAddress;
    private boolean isShowKeystore = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_wallet);
        initParams();
        initView();
    }

    private void initParams() {
        Intent intent = getIntent();
        keystoreJson = intent.getStringExtra(KEYSTORE);
        walletAddress = WalletUtils.getAddress(keystoreJson);
    }

    private void initView() {

        TextView addressTextView = findViewById(R.id.tv_wallet_address);
        addressTextView.setText(walletAddress);

        TextView changePasswordTextView = findViewById(R.id.tv_change_password);
        RxView.clicks(changePasswordTextView)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //modify wallet password
                    ModifyWalletPasswordActivity.open(ManageWalletActivity.this,keystoreJson);
                    finish();
                });

        keystoreTextView = findViewById(R.id.wallet_import_content);
        keystoreTextView.setText(keystoreJson);
        keystoreTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        keystoreTextView.setTextIsSelectable(true);

        TextView showKeystoreTextView = findViewById(R.id.tv_export_keystore);
        RxView.clicks(showKeystoreTextView)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //show keystore
                    if (isShowKeystore) {
                        isShowKeystore = false;
                        keystoreTextView.setVisibility(View.GONE);
                    } else {
                        isShowKeystore = true;
                        keystoreTextView.setVisibility(View.VISIBLE);
                    }
                });

        Button deleteButton = findViewById(R.id.btn_delete_wallet);
        RxView.clicks(deleteButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //delete wallet
                    deleteWallet();
                });
    }


    private void deleteWallet() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.manage_wallet_delete_tip);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(R.string.manage_wallet_delete_message);
        builder.setPositiveButton(R.string.manage_wallet_delete_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                FileUtils.writeCache(getBaseContext(), FileUtils.KEYSTORE_NAME, "");

                Intent intent = new Intent();
                intent.putExtra("isDeleteWallet",true);
                setResult(Activity.RESULT_OK,intent);
                finish();

            }
        });
        builder.setNegativeButton(R.string.manage_wallet_delete_cancel, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
