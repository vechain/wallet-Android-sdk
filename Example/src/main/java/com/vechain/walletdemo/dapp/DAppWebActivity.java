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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.wallet.WalletUtils;
import com.vechain.wallet.dapp.plugin.PluginManager;
import com.vechain.wallet.dapp.plugin.inject.CertificateCallback;
import com.vechain.wallet.dapp.plugin.inject.DAppAction;
import com.vechain.wallet.dapp.plugin.inject.GetWalletAddressCallback;
import com.vechain.wallet.dapp.plugin.inject.OwnedAddressCallback;
import com.vechain.wallet.dapp.plugin.inject.TransferResultCallback;
import com.vechain.wallet.dapp.web.DAppWebChromeClient;
import com.vechain.wallet.dapp.web.DAppWebViewClient;
import com.vechain.wallet.network.utils.DeviceUtils;
import com.vechain.wallet.thor.tx.Clause;
import com.vechain.walletdemo.R;
import com.vechain.walletdemo.transaction.PasswordDialog;
import com.vechain.walletdemo.transaction.TransferResultListener;
import com.vechain.walletdemo.utils.FileUtils;
import com.vechain.walletdemo.view.WebProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class DAppWebActivity extends Activity implements DAppAction {


    private static final String PARAMS_OPEN_URL = "url";


    private ImageView leftBackImageView;
    private TextView titleTextView;
    private FrameLayout fragmentLayout;
    private WebProgressBar progressBar;
    private WebView webView;


    private boolean keyBackPressed = false;
    private String url;
    private boolean isInit = false;

    private PluginManager pluginManager;

    public static void open(Context context, String url) {
        if (!DeviceUtils.checkNetWork(context)) {
            Toast.makeText(context, R.string.no_network_hint, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(url))
            return;
        Intent intent = new Intent(context, DAppWebActivity.class);
        intent.putExtra(PARAMS_OPEN_URL, url);
        context.startActivity(intent);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        //Support for DApp must be initialized
        pluginManager = new PluginManager();
        pluginManager.setdAppAction(this);

        initParams(savedInstanceState);
        initView();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(PARAMS_OPEN_URL, url);

        super.onSaveInstanceState(outState);
    }

    private void initParams(Bundle savedInstanceState) {

        if (savedInstanceState != null) {

            url = savedInstanceState.getString(PARAMS_OPEN_URL);
        } else {
            Intent intent = getIntent();
            url = intent.getStringExtra(PARAMS_OPEN_URL);
        }

    }


    private void initView() {

        leftBackImageView = findViewById(R.id.leftBackIcon);
        titleTextView = findViewById(R.id.title);
        webView = findViewById(R.id.web);

        // Add progress bar
        fragmentLayout = this.findViewById(R.id.fragment);
        progressBar = new WebProgressBar(this, null);
        fragmentLayout.addView(progressBar,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.TOP)
        );

        leftBackImageView.setImageResource(R.drawable.icon_back_black);
        RxView.clicks(leftBackImageView)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    DAppWebActivity.this.finish();
                });

        int color = getResources().getColor(R.color.color_202c56);
        titleTextView.setTextColor(color);
        //android:ellipsize="end"
        titleTextView.setEllipsize(TextUtils.TruncateAt.END);

        initWebSetting();
    }


    private void initWebSetting() {
        // display picture
        WebSettings set = webView.getSettings();
        set.setJavaScriptEnabled(true);
        set.setAllowFileAccess(false);
        set.setAllowFileAccessFromFileURLs(false);
        set.setAllowUniversalAccessFromFileURLs(false);
        set.setLoadsImagesAutomatically(true);


        set.setDefaultTextEncodingName("UTF-8");
        set.setCacheMode(WebSettings.LOAD_DEFAULT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            set.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }


        webView.setWebViewClient(new MyWebClient());
        webView.setWebChromeClient(new MyWebChromeClient(pluginManager));

        set.setDomStorageEnabled(true);
        set.setDatabaseEnabled(true);

    }


    @Override
    public void setTitle(int titleId) {
        titleTextView.setText(titleId);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (titleTextView != null) {
            titleTextView.setSingleLine();
            // Title up to 12 characters
            //titleTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
            titleTextView.setText(title);
        }
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!isInit) {
            isInit = true;
            webView.loadUrl(url);
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView != null && webView.canGoBack()) {
                keyBackPressed = true;
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (keyBackPressed) {
                    keyBackPressed = false;
                    if (webView != null && webView.canGoBack()) {
                        webView.goBack();
                        return true;
                    }
                }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null)
            webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null)
            webView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null)
            webView.destroy();
        webView = null;

        //Memory Recycling to Prevent Memory Leakage
        pluginManager.setdAppAction(null);
        pluginManager.clearWebView();
        pluginManager = null;

        super.onDestroy();
    }



    /**
     *  Interfaces that must be implemented to support the DApp environment
     *  App developer implementation when dapp calls checkOwn address function
     *
     *  When will this method be called? When DAPP needs to know if it has address locally
     *
     * @param address Address from dapp
     * @param dAppCallback Callback after the end，notify DAP if the address is locally owned
     */
    public void onCheckOwnAddress(String address, OwnedAddressCallback dAppCallback){

        //Read the wallet keystore from a file or database
        String keystore = FileUtils.readCache(this, FileUtils.KEYSTORE_NAME);
        final String walletAddress = WalletUtils.getAddress(keystore);
        boolean isOwn = walletAddress.equalsIgnoreCase(address);
        if(dAppCallback!=null)
            dAppCallback.onOwned(isOwn);
    }

    /**
     * Interfaces that must be implemented to support the DApp environment
     *
     * When will this method be called? This method is called when DAPP needs to issue certificate using a local Wallet
     *
     * @param message  Information to be signed
     * @param dAppCallback Callback method after signature information is completed，notify DAPP when the result is completed
     */
    @Override
    public void onCertificate(final String message, final String signer,final CertificateCallback dAppCallback) {
        //Interfaces that must be implemented to support the DApp environment
        //Read the wallet keystore from a file or database
        String keystore = FileUtils.readCache(this, FileUtils.KEYSTORE_NAME);
        final String walletAddress = WalletUtils.getAddress(keystore);

        String certificateMessage = message;
        // if signer not null, Enforces the specified address to sign the certificate
        if(signer!=null && !signer.equalsIgnoreCase(walletAddress)){
            if (dAppCallback != null) dAppCallback.onCertificate(null,null);
            return;
        }else{
            // if signer is null,Add signer for certificate information
            if(signer==null)
                certificateMessage = WalletUtils.addSigner(message,walletAddress.toLowerCase());
        }


        //Prompt user to enter wallet password
        CertificatePasswordDialog.takePassword(this, keystore, certificateMessage,new CertificateSignListener() {
            @Override
            public void deliverCertificate(String signature) {
                // notify DApp
                if(dAppCallback!=null)dAppCallback.onCertificate(walletAddress,signature);
            }
        });
    }

    /**
     * Interfaces that must be implemented to support the DApp environment
     *
     * When will this method be called? When DAPP needs to use a local wallet to send transactions
     *
     * @param clauses Dapp group of transaction parameters
     * @param gas Miners'Fees Needed by Exchanges
     * @param dAppCallback Transaction Sending Completion Callback Method,notify Dapp the results of the transaction and the address of the wallet that sent the transaction
     */
    @Override
    public void onTransfer(List<Clause> clauses, long gas, final String signer,final TransferResultCallback dAppCallback) {

        //Read the wallet keystore from a file or database
        String keystore = FileUtils.readCache(this, FileUtils.KEYSTORE_NAME);
        final String address = WalletUtils.getAddress(keystore);

        // if signer not null, Enforces the specified address to sign the transaction
        if(signer!=null && !signer.equalsIgnoreCase(address)){
            if(dAppCallback!=null)dAppCallback.onTransferResult(null,null);
            return;
        }

        PasswordDialog.showInputPassword(this, keystore, clauses, gas, new TransferResultListener() {
            @Override
            public void onTransferResult(String result) {

                if(dAppCallback!=null)dAppCallback.onTransferResult(address,result);
            }
        });

    }

    /**
     * Interfaces that must be implemented to support the DApp environment
     *
     * When will this method be called? When DAPP needs to get a list of local wallet addresses
     *
     * Get a list of wallet addresses from a local file or database
     *
     * @param dAppCallback Address list to DApp
     */
    @Override
    public void onGetWalletAddress(GetWalletAddressCallback dAppCallback) {
        //Get the wallet keystore from the local database or file cache
        String keystore = FileUtils.readCache(this, FileUtils.KEYSTORE_NAME);
        String address = WalletUtils.getAddress(keystore);

        //DApp must set addresses
        List<String> addresses = new ArrayList<>();
        addresses.add(address.toLowerCase());

        //Callback method must be executed after completion
        if (dAppCallback != null) dAppCallback.setWalletAddress(addresses);
    }

    /**
     * Interfaces that must be implemented to support the DApp environment
     */
    private class MyWebChromeClient extends DAppWebChromeClient {

        public MyWebChromeClient(PluginManager pluginManager){
            super(pluginManager);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            if (progressBar != null)
                progressBar.setProgress(newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            //must be implemented to support the DApp environment
            super.onReceivedTitle(view, title);

            if (TextUtils.isEmpty(title))
                setTitle("");
            else
                setTitle(title);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {

            //must be implemented to support the DApp environment
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

    }


    /**
     * Interfaces that must be implemented to support the DApp environment
     */
    private class MyWebClient extends DAppWebViewClient {

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

            //must be implemented to support the DApp environment
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (progressBar != null) {
                progressBar.setProgress(0);
                progressBar.show();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            // Hide progress bar
            if (progressBar != null) {
                progressBar.setProgress(100);
                progressBar.hide();
            }
            super.onPageFinished(view, url);
        }


        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            if (progressBar != null) {
                progressBar.setProgress(100);
                progressBar.hide();
            }

            if (view != null)
                view.stopLoading();
        }
    }

}
