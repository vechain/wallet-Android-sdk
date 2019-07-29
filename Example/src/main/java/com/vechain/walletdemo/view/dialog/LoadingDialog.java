
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

package com.vechain.walletdemo.view.dialog;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.vechain.walletdemo.R;


public class LoadingDialog extends Dialog {

    private Context context = null;
    private String loadingText = null;

    public LoadingDialog(Context context) {
        super(context);
        this.context = context;
    }

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public LoadingDialog(Context context, int theme, CharSequence loadingTip) {
        super(context, theme);
        this.context = context;
        if (loadingTip != null) {
            this.loadingText = String.valueOf(loadingTip);
        }
    }

    public static LoadingDialog show(Context context) {
        return show(context, null);
    }

    public static LoadingDialog show(Context context, CharSequence message) {
        return show(context, null, message);
    }

    public static LoadingDialog show(Context context, CharSequence title, CharSequence message) {
        return show(context, title, message, false);
    }

    public static LoadingDialog show(Context context, CharSequence title, CharSequence message,
                                     boolean cancelable) {
        return show(context, title, message, cancelable, null);
    }

    public static LoadingDialog show(final Context context, CharSequence title, CharSequence message,
                                     boolean cancelable, OnCancelListener cancelListener) {

        if (context == null) return null;

        LoadingDialog dialog = new LoadingDialog(context, R.style.LoadingDialog, message);
        if (title != null) {
            dialog.setTitle(title);
        }
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        if (context instanceof Service) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialog.show();
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_progress, null);
        TextView loadingTextView = view.findViewById(R.id.loadingText);
        if (loadingTextView != null && loadingText != null) {
            loadingTextView.setVisibility(View.VISIBLE);
            loadingTextView.setText(loadingText);
        } else {
            assert loadingTextView != null;
            loadingTextView.setVisibility(View.INVISIBLE);
        }
        setContentView(view);
        super.onCreate(savedInstanceState);
    }

    public void setMessage(String msg) {
        loadingText = msg;
        TextView loadingTextView = this.findViewById(R.id.loadingText);
        if (loadingTextView != null && loadingText != null)
            loadingTextView.setText(loadingText);
    }

}