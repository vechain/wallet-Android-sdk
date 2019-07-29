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

package com.vechain.walletdemo.importwallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.wallet.WalletUtils;
import com.vechain.walletdemo.HomeActivity;
import com.vechain.walletdemo.R;
import com.vechain.walletdemo.utils.FileUtils;

import java.util.concurrent.TimeUnit;

/**
 * Keystore imports Wallet
 */

public class ImportWalletByKeystoreFragment extends Fragment {

    public final static ImportWalletByKeystoreFragment get() {
        ImportWalletByKeystoreFragment fragment = new ImportWalletByKeystoreFragment();
        return fragment;
    }

    private TextView titleTextView;
    private EditText keystoreEditText;
    private EditText passwordEditText;
    private Button confirmButton;

    private ProgressDialog dialog;

    private String keystoreJson;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_wallet, container, false);
        initView(view);

        return view;
    }

    private void initView(View view) {
        titleTextView = view.findViewById(R.id.contentTitle);
        keystoreEditText = view.findViewById(R.id.wallet_import_content_editText);
        passwordEditText = view.findViewById(R.id.password);
        confirmButton = view.findViewById(R.id.confirmImport);

        titleTextView.setText(R.string.import_wallet_item2);

        RxView.clicks(confirmButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //import Wallet
                    importWalletByKeystore();
                });
        //Fill in the content for the test
        keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";
        keystoreEditText.setText(keystoreJson);
        passwordEditText.setText("123456");
    }

    private void importWalletByKeystore() {
        keystoreJson = keystoreEditText.getText().toString();
        if (TextUtils.isEmpty(keystoreJson)) {
            Toast.makeText(getActivity(), R.string.import_wallet_please_input_keystore, Toast.LENGTH_SHORT).show();
            return;
        }
        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), R.string.create_wallet_hint_password, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!WalletUtils.isValidKeystore(keystoreJson)) {
            Toast.makeText(getActivity(), R.string.import_wallet_please_input_keystore_error, Toast.LENGTH_SHORT).show();
            return;
        }
        startLoading();
        startImportWalletByKeystore(password);
    }

    private void startImportWalletByKeystore(String password) {

        WalletUtils.verifyKeystorePassword(keystoreJson, password, new WalletUtils.OnVerifyPasswordCallback() {
            @Override
            public void onCheckPassword(boolean success) {
                if (success) {
                    //success
                    onImportSuccess();
                    FileUtils.writeCache(getActivity(), FileUtils.KEYSTORE_NAME, keystoreJson);
                } else {
                    //errors
                    onImportWalletError();
                }
            }
        });
    }

    private void onImportWalletError() {
        closeLoading();
        Toast.makeText(getActivity(), R.string.import_wallet_fail, Toast.LENGTH_SHORT).show();
    }

    private void onImportSuccess() {
        closeLoading();
        Activity activity = getActivity();
        HomeActivity.open(activity, keystoreJson);
        activity.finish();
    }

    private void startLoading() {
        closeLoading();
        dialog = new ProgressDialog(this.getActivity());
        dialog.show();
    }

    private void closeLoading() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
