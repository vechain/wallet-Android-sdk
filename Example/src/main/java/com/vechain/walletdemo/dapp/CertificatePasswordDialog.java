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

package com.vechain.walletdemo.dapp;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.wallet.WalletUtils;
import com.vechain.wallet.bip32.ValidationException;
import com.vechain.wallet.keystore.CipherException;
import com.vechain.wallet.utils.HexUtils;
import com.vechain.walletdemo.R;
import com.vechain.walletdemo.utils.RxUtils;
import com.vechain.walletdemo.view.dialog.BaseDialog;
import com.vechain.walletdemo.view.dialog.LoadingDialog;
import com.vechain.walletdemo.view.dialog.ViewHolder;

import java.util.concurrent.TimeUnit;

public class CertificatePasswordDialog extends BaseDialog {

    public static void takePassword(Activity activity,
                                         String keystoreJson,
                                         String message,
                                         CertificateSignListener certificateSignListener) {
        CertificatePasswordDialog passwordDialog = new CertificatePasswordDialog();

        passwordDialog.setCertificateSignListener(certificateSignListener);
        passwordDialog.setKeystoreJson(keystoreJson);
        passwordDialog.setMessage(message);

        passwordDialog.show(activity.getFragmentManager());
    }


    private EditText editText;
    private LoadingDialog dialog;

    private String keystoreJson;
    private String message;

    private CertificateSignListener certificateSignListener;


    public void setKeystoreJson(String keystoreJson) {
        this.keystoreJson = keystoreJson;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCertificateSignListener(CertificateSignListener certificateSignListener) {
        this.certificateSignListener = certificateSignListener;
    }

    @Override
    public int intLayoutId() {

        setShowBottom(true);//居底部
        setOutCancel(false);

        return R.layout.dialog_password;
    }

    @Override
    public void convertView(ViewHolder holder, BaseDialog dialog) {

        holder.setText(R.id.title,R.string.connex_certificate_title);

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


    private void checkPassword(final String password) {
        startLoading();
        WalletUtils.verifyKeystorePassword(keystoreJson, password, new WalletUtils.OnVerifyPasswordCallback() {
            @Override
            public void onCheckPassword(boolean success) {

                if (success) {
                    certificateSign(password);
                } else {
                    closeLoading();
                    Toast.makeText(getActivity(), "password error", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void certificateSign(String password){

        RxUtils.onFlowable(new RxUtils.BusinessCallBack<String, String>() {

            @Override
            public String map(String password) {

                byte[] data = message.getBytes();
                try {
                    byte[] signature = WalletUtils.sign(keystoreJson, password, data);
                    String signatrueString = HexUtils.toHexString(signature);
                    return signatrueString;
                }catch (CipherException e1){
                    return null;
                }catch (ValidationException e2){
                    return null;
                }
            }

            @Override
            public void onSuccess(String signature) {
                closeLoading();
                dismiss();
                if(certificateSignListener!=null)
                    certificateSignListener.deliverCertificate(signature);
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                closeLoading();
                dismiss();
                if(certificateSignListener!=null)
                    certificateSignListener.deliverCertificate(null);
            }
        },password);
    }

}
