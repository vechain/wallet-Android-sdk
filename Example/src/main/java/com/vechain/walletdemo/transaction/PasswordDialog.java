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

package com.vechain.walletdemo.transaction;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.wallet.thor.tx.ParameterException;
import com.vechain.wallet.thor.tx.TransferParameter;
import com.vechain.wallet.WalletUtils;
import com.vechain.wallet.thor.tx.Clause;
import com.vechain.wallet.utils.HexUtils;
import com.vechain.walletdemo.R;
import com.vechain.walletdemo.view.dialog.BaseDialog;
import com.vechain.walletdemo.view.dialog.LoadingDialog;
import com.vechain.walletdemo.view.dialog.ViewHolder;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PasswordDialog extends BaseDialog {


    public static void showInputPassword(Activity activity,
                                         String keystoreJson, List<Clause> clauseList, long gas,
                                         TransferResultListener transferResultListener) {
        PasswordDialog passwordDialog = new PasswordDialog();

        passwordDialog.setTransferResultListener(transferResultListener);

        passwordDialog.setKeystoreJson(keystoreJson);
        passwordDialog.setClauseList(clauseList);
        passwordDialog.setGas(gas);

        passwordDialog.show(activity.getFragmentManager());
    }


    private EditText editText;
    private LoadingDialog dialog;

    private TransferResultListener transferResultListener;

    private String keystoreJson;
    private List<Clause> clauseList;
    private long gas;
    private String password;

    private String chainTag;
    private String blockRef;


    public void setTransferResultListener(TransferResultListener transferResultListener) {
        this.transferResultListener = transferResultListener;
    }

    public void setKeystoreJson(String keystoreJson) {
        this.keystoreJson = keystoreJson;
    }

    public void setClauseList(List<Clause> clauseList) {
        this.clauseList = clauseList;
    }

    public void setGas(long gas) {
        this.gas = gas;
    }

    @Override
    public int intLayoutId() {

        setShowBottom(true);//居底部
        setOutCancel(false);

        return R.layout.dialog_password;
    }

    @Override
    public void convertView(ViewHolder holder, BaseDialog dialog) {

        editText = holder.getView(R.id.password);
        ImageView backImagView = holder.getView(R.id.back);
        RxView.clicks(backImagView)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    dismiss();
                });

        Button confirmTextView = holder.getView(R.id.confirm);
        RxView.clicks(confirmTextView)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //Create Wallet
                    String password = editText.getText().toString();
                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(dialog.getActivity(),
                                R.string.create_wallet_hint_password, Toast.LENGTH_LONG).show();
                    } else {
                        checkPassword(password);
                    }
                });
    }


    private void startLoading() {

        dialog = LoadingDialog.show(getActivity());
    }

    private void closeLoading() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }


    private void checkPassword(String password) {
        this.password = password;
        startLoading();
        WalletUtils.verifyKeystorePassword(keystoreJson, password, new WalletUtils.OnVerifyPasswordCallback() {
            @Override
            public void onCheckPassword(boolean success) {

                if (success) {
                    getChainTag();
                } else {
                    closeLoading();
                    Toast.makeText(getActivity(), "password error", Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    private void getChainTag() {

        WalletUtils.getChainTag(new WalletUtils.OnGetChainTagCallback() {
            @Override
            public void onGetChainTagResult(String chainTag) {
                PasswordDialog.this.chainTag = chainTag;
                if (TextUtils.isEmpty(chainTag)) {
                    closeLoading();
                    Toast.makeText(getActivity(), "Failed to get chainTag", Toast.LENGTH_SHORT).show();
                } else {
                    getBlockReference();
                }
            }
        });
    }

    private void getBlockReference() {

        WalletUtils.getBlockReference(new WalletUtils.OnGetBlockReferenceCallback() {
            @Override
            public void onGetBlockReferenceResult(String blockRef) {
                PasswordDialog.this.blockRef = blockRef;
                if (TextUtils.isEmpty(PasswordDialog.this.blockRef)) {
                    closeLoading();
                    Toast.makeText(getActivity(), "Failed to get blockRef", Toast.LENGTH_SHORT).show();
                } else {
                    signAndSendTransfer();
                }
            }
        });
    }

    private void signAndSendTransfer() {


        String hexNonce = null;
        byte[] nonceBytes = new byte[8];
        Random random = new Random();
        random.nextBytes(nonceBytes);
        hexNonce = HexUtils.toHexString(nonceBytes);

        TransferParameter.Builder builder = new TransferParameter.Builder();
        TransferParameter transferParameter = null;
        try {
            transferParameter = builder.setClauseList(clauseList)
                    .setGas(gas)
                    .setChainTag(chainTag)
                    .setBlockRef(blockRef)
                    .setNonce(hexNonce)
                    .build();
        } catch (ParameterException e) {
            e.printStackTrace();
            closeLoading();
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        WalletUtils.signAndSendTransfer(keystoreJson, password, transferParameter, new WalletUtils.OnSignCallback() {
            @Override
            public void onSignResult(String result) {
                closeLoading();
                if (transferResultListener != null)
                    transferResultListener.onTransferResult(result);
                dismiss();

            }
        });
    }

}
