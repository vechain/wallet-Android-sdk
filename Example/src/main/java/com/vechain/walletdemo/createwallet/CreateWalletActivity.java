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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.wallet.WalletUtils;
import com.vechain.walletdemo.HomeActivity;
import com.vechain.walletdemo.R;
import com.vechain.walletdemo.utils.FileUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Create a wallet and display mnemonics and keystore
 */

public class CreateWalletActivity extends AppCompatActivity {

    public final static void open(Context context) {

        Intent intent = new Intent(context, CreateWalletActivity.class);
        context.startActivity(intent);
    }

    private View passwordLayout;
    private EditText passwordEditText;
    private Button confirmPasswordButton;

    private View keystoreLayout;
    private TextView mnemonicTextView;
    private TextView keystoreTextView;
    private Button completeButton;

    private ProgressDialog dialog;

    private List<String> words;
    String keystore;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);
        initView();
    }

    private void initView() {
        passwordLayout = findViewById(R.id.passwordConfirmLayout);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordButton = findViewById(R.id.confirmCreate);

        keystoreLayout = findViewById(R.id.keystoreLayout);
        mnemonicTextView = findViewById(R.id.mnemonic);
        keystoreTextView = findViewById(R.id.keystore);
        completeButton = findViewById(R.id.complete);

        mnemonicTextView.setTextIsSelectable(true);
        keystoreTextView.setTextIsSelectable(true);

        passwordLayout.setVisibility(View.VISIBLE);
        keystoreLayout.setVisibility(View.GONE);

        RxView.clicks(confirmPasswordButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //Create Wallet
                    createWallet();
                });

        RxView.clicks(completeButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //Create Wallet
                    HomeActivity.open(CreateWalletActivity.this, keystore);
                    finish();
                });
    }




    private void createWallet() {

        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your wallet password", Toast.LENGTH_SHORT).show();
            return;
        }

        startDialog();

        WalletUtils.OnCreateWalletCallback callback = new WalletUtils.OnCreateWalletCallback() {
            @Override
            public void onCreate(WalletUtils.Wallet wallet) {

                closeDialog();

                if (wallet == null) {
                    //fail
                    createFail();

                }else {
                    //success
                    words = wallet.getWords();
                    keystore = wallet.getKeystore();
                    String privateKey = wallet.getPrivateKey();
                    String address = wallet.getAddress();

                    //Keystore is saved in files or databases
                    FileUtils.writeCache(CreateWalletActivity.this, FileUtils.KEYSTORE_NAME, keystore);

                    createSuccess();
                }
            }
        };

        WalletUtils.createWallet(password, callback);
    }

    private void createSuccess(){
        Toast.makeText(this, "Create success!", Toast.LENGTH_SHORT).show();
        passwordLayout.setVisibility(View.GONE);
        keystoreLayout.setVisibility(View.VISIBLE);

        StringBuffer buffer = new StringBuffer();
        for (String item : words) {
            buffer.append(item).append(" ");
        }
        String mnemonicWords = buffer.toString();
        //Log.v("mnemonicWords",mnemonicWords);
        mnemonicTextView.setText(mnemonicWords);
        keystoreTextView.setText(keystore);
    }

    private void createFail(){
        Toast.makeText(this, "Create failure!", Toast.LENGTH_SHORT).show();
    }


    private void startDialog() {
        closeDialog();
        dialog = new ProgressDialog(this);
        dialog.show();
    }

    private void closeDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

}
