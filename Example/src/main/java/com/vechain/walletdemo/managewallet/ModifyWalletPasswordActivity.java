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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.wallet.WalletUtils;
import com.vechain.walletdemo.HomeActivity;
import com.vechain.walletdemo.R;
import com.vechain.walletdemo.utils.FileUtils;

import java.util.concurrent.TimeUnit;

/**
 * modify wallet password
 */

public class ModifyWalletPasswordActivity extends AppCompatActivity {

    private final static String KEYSTORE = "keystore";

    public final static void open(Context context, String keystoreJson) {

        if (TextUtils.isEmpty(keystoreJson)) return;

        Intent intent = new Intent(context, ModifyWalletPasswordActivity.class);
        intent.putExtra(KEYSTORE, keystoreJson);

        context.startActivity(intent);
    }


    EditText oldPasswordEditText;
    EditText newPasswordEditText;
    EditText confirmEditText;
    Button confirmButton;

    private String keystoreJson;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modify_wallet_password);
        initParams();
        initView();
    }

    private void initParams() {
        Intent intent = getIntent();
        keystoreJson = intent.getStringExtra(KEYSTORE);
    }

    private void initView() {
        oldPasswordEditText = findViewById(R.id.et_oldpassword);
        newPasswordEditText = findViewById(R.id.et_newpassword);
        confirmEditText = findViewById(R.id.et_new2password);
        confirmButton = findViewById(R.id.btn_confirm);

        RxView.clicks(confirmButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //modify wallet password
                    modifyPassword();
                });
    }

    private void modifyPassword() {
        String oldPassword = oldPasswordEditText.getText().toString();
        String newPassword1 = newPasswordEditText.getText().toString();
        String newPassword2 = confirmEditText.getText().toString();

        if (TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(this, R.string.modify_password_old_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(newPassword1) || !newPassword1.equals(newPassword2)) {
            Toast.makeText(this, R.string.modify_password_new_error, Toast.LENGTH_SHORT).show();
            return;
        }
        startLoading();
        startModifyPassword(oldPassword, newPassword1);
    }

    private void startModifyPassword(final String oldPassword, final String newPassword) {


        WalletUtils.modifyKeystorePassword(keystoreJson, oldPassword, newPassword,
                new WalletUtils.OnModifyKeystorePasswordCallback() {
                    @Override
                    public void onModifyResult(String newKeystore) {

                        if (!TextUtils.isEmpty(newKeystore)) {
                            keystoreJson = newKeystore;
                            FileUtils.writeCache(getBaseContext(), FileUtils.KEYSTORE_NAME, newKeystore);

                            //success
                            onModifyPasswordSuccess();
                        } else {
                            //errors
                            onModifyPasswordError();
                        }
                    }
                });
    }

    private void onModifyPasswordError() {
        closeLoading();
        Toast.makeText(this, R.string.modify_password_failr, Toast.LENGTH_SHORT).show();
    }

    private void onModifyPasswordSuccess() {
        closeLoading();
        HomeActivity.open(this, keystoreJson);
        finish();
    }

    private void startLoading() {
        closeLoading();
        dialog = new ProgressDialog(this);
        dialog.show();
    }

    private void closeLoading() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

}
