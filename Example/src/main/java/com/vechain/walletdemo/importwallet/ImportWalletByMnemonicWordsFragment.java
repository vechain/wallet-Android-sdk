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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * mnemonic words imports Wallet
 */
public class ImportWalletByMnemonicWordsFragment extends Fragment {

    public final static ImportWalletByMnemonicWordsFragment get() {
        ImportWalletByMnemonicWordsFragment fragment = new ImportWalletByMnemonicWordsFragment();
        return fragment;
    }

    private TextView titleTextView;
    private EditText mnemonicWordsEditText;
    private EditText passwordEditText;
    private Button confirmButton;

    private ProgressDialog dialog;

    private List<String> words;
    private String keystore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_wallet, container, false);
        initView(view);

        return view;
    }

    private void initView(View view) {
        titleTextView = view.findViewById(R.id.contentTitle);
        mnemonicWordsEditText = view.findViewById(R.id.wallet_import_content_editText);
        passwordEditText = view.findViewById(R.id.password);
        confirmButton = view.findViewById(R.id.confirmImport);

        titleTextView.setText(R.string.import_wallet_item1);

        RxView.clicks(confirmButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //import Wallet
                    mnemonicWordsImportWallet();
                });

        //Fill in the content for the test
        String words = "enact again rate alone congress scheme solid theory flush length twenty head";
        mnemonicWordsEditText.setText(words);
        passwordEditText.setText("123456");
    }

    private void mnemonicWordsImportWallet() {

        String wordString = mnemonicWordsEditText.getText().toString();
        if (TextUtils.isEmpty(wordString)) {
            Toast.makeText(getActivity(), R.string.import_wallet_please_input_mnemonic, Toast.LENGTH_SHORT).show();
            return;
        }
        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), R.string.create_wallet_hint_password, Toast.LENGTH_SHORT).show();
            return;
        }

        //verify mnemonic words
        words = getMnemonicWords();
        if (words == null ) {
            //The number of mnemonic words is incorrect
            Toast.makeText(getActivity(), R.string.create_wallet_hint_password, Toast.LENGTH_SHORT).show();
            words = null;
            return;
        }
        if (!WalletUtils.isValidMnemonicWords(words)) {
            //Mnemonic errors
            onImportWalletError(true);
            return;
        }

        startLoading();
        startImportWalletByWords(password);
    }

    private List<String> getMnemonicWords() {
        List<String> mnemonicWords = new ArrayList<>();
        String content = mnemonicWordsEditText.getText().toString();
        if (TextUtils.isEmpty(content)) return mnemonicWords;
        content = content.toLowerCase();
        content = content.trim();//Remove the front and back blanks
        String[] strings = content.split(" ");

        if (strings == null ) return mnemonicWords;
        for (String item : strings) {
            mnemonicWords.add(item);
        }

        return mnemonicWords;
    }


    private void startImportWalletByWords(String password) {


        WalletUtils.createWallet(words, password, new WalletUtils.OnCreateWalletCallback() {
            @Override
            public void onCreate(WalletUtils.Wallet wallet) {

                closeLoading();
                if(wallet!=null) {
                    keystore = wallet.getKeystore();
                    if (!TextUtils.isEmpty(keystore))
                        FileUtils.writeCache(getContext(), FileUtils.KEYSTORE_NAME, keystore);

                    //success
                    onImportSuccess();
                }else{
                    //errors
                    onImportWalletError(false);
                }
            }

        });

    }

    private void onImportWalletError(boolean isWordError) {
        closeLoading();
        if (isWordError) {
            Toast.makeText(getActivity(), R.string.import_wallet_mnemonic_word_format, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.import_wallet_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void onImportSuccess() {
        closeLoading();
        Activity activity = getActivity();
        HomeActivity.open(activity, keystore);
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
