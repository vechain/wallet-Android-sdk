
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

package com.vechain.walletdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.wallet.WalletUtils;
import com.vechain.wallet.network.Api;
import com.vechain.wallet.network.NodeUrl;
import com.vechain.wallet.network.bean.common.Token;
import com.vechain.wallet.network.bean.core.TokenBalance;
import com.vechain.wallet.network.bean.core.VETBalance;
import com.vechain.wallet.network.engine.NetCallBack;
import com.vechain.wallet.utils.StringUtils;
import com.vechain.walletdemo.createwallet.NoWalletKeystoreActivity;
import com.vechain.walletdemo.managewallet.ManageWalletActivity;
import com.vechain.walletdemo.setting.NodeServerInfoActivity;
import com.vechain.walletdemo.setting.NodeServerSettingActivity;
import com.vechain.walletdemo.transaction.TransactionActivity;
import com.vechain.walletdemo.view.PopupBuilder;
import com.vechain.walletdemo.dapp.DAppWebActivity;

import java.util.concurrent.TimeUnit;

/**
 * Wallet details
 */
public class HomeActivity extends Activity {

    private final static String KEYSTORE = "keystore";
    public final static void open(Context context, String keystoreJson) {

        if (TextUtils.isEmpty(keystoreJson)) return;

        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(KEYSTORE, keystoreJson);

        context.startActivity(intent);
    }

    private TextView titleTextView;
    private TextView selectedHostTextView;
    private EditText inputHostEditText;
    private Button goWebButton;

    private TextView keystoreTextView;
    private TextView vetBalanceTextView;
    private TextView vthoBalanceTextView;
    private PopupWindow menu;


    private String keystoreJson;
    private String walletAddress;

    private boolean isFirst = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Intent intent = getIntent();
        initParams(intent);
        initView();
        Window window = getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        initParams(intent);
    }

    private void initParams(Intent intent) {

        keystoreJson = intent.getStringExtra(KEYSTORE);
        walletAddress = WalletUtils.getAddress(keystoreJson);

        isFirst = true;
    }

    private void initView() {

        //title bar
        titleTextView = findViewById(R.id.titleName);
        selectedHostTextView = findViewById(R.id.selectedNetwork);
        selectedHostTextView.setText(R.string.host_select_txt);
        RxView.clicks(selectedHostTextView)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //select node host
                    openMenuDialog(selectedHostTextView);
                });
        //环境
        hoseNameTags = this.getResources().getStringArray(R.array.host_name_tag_array);
        String hostState = Api.getNodeUrl() == NodeUrl.TEST_NODE ? hoseNameTags[1] : hoseNameTags[0];
        titleTextView.setText("VeChainThorWallet" + hostState);

        //edit host
        inputHostEditText = findViewById(R.id.input);
        inputHostEditText.setText("https://bc66.github.io/lucky-airdrop/#/");
        goWebButton = findViewById(R.id.go);
        RxView.clicks(goWebButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //open Dapp web page
                    String url = inputHostEditText.getText().toString();
                    if (TextUtils.isEmpty(url)) return;

                    DAppWebActivity.open(HomeActivity.this, url);
                });

        keystoreTextView = findViewById(R.id.keystore);
        vetBalanceTextView = findViewById(R.id.vetBalance);
        vthoBalanceTextView = findViewById(R.id.vthoBalance);

        if (!TextUtils.isEmpty(walletAddress)) {
            String name = getString(R.string.home_asset_wallet_address);
            keystoreTextView.setText(name + walletAddress);
        }
        RxView.clicks(keystoreTextView)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //manage wallet
                    ManageWalletActivity.open(HomeActivity.this, keystoreJson);
                });


        vetBalanceTextView.setText("0.00");
        vthoBalanceTextView.setText("0.00");

        View vetLayout = findViewById(R.id.vetLayout);
        View vthoLayout = findViewById(R.id.vthoLayout);
        RxView.clicks(vetLayout)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //VET transfer
                    Token vetToken = Token.getVETToken(walletAddress.toLowerCase());
                    if (vetToken != null)
                        TransactionActivity.open(HomeActivity.this, keystoreJson, walletAddress, vetToken);
                });

        RxView.clicks(vthoLayout)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    //VTHO transfer
                    Token vthoToken = Token.getVTHOToken();
                    TransactionActivity.open(HomeActivity.this, keystoreJson, walletAddress, vthoToken);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            isFirst = false;
            getVetBalance();
            getTokenBalance(Token.getVTHOToken());
        }
    }

    private void getVetBalance() {

        String address = walletAddress.toLowerCase();
        Api.getVETBalance(address, new NetCallBack() {
            @Override
            public Object map(Object result) {

                String value = "0.00";
                if (result != null && result instanceof VETBalance) {
                    VETBalance vetBalance = (VETBalance) result;

                    String hex = vetBalance.getBalance();
                    value = StringUtils.hexString2DecimalString(hex, Token.DECIMALS, Token.RETAIN);

                }

                return value;
            }

            @Override
            public void onSuccess(Object result) {

                if (result != null && result instanceof String) {
                    String value = (String) result;
                    vetBalanceTextView.setText(value);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }
        }, null);

    }

    private void getTokenBalance(final Token token) {

        String address = walletAddress.toLowerCase();

        Api.getTokenBalance(token.getAddress(), address, new NetCallBack() {
            @Override
            public Object map(Object result) {
                String value = "0.00";
                if (result != null && result instanceof TokenBalance) {
                    TokenBalance tokenBalance = (TokenBalance) result;

                    String hex = tokenBalance.getData();

                    int decimals = token.getDecimals();
                    value = StringUtils.hexString2DecimalString(hex, decimals, decimals);


                }

                return value;
            }

            @Override
            public void onSuccess(Object result) {

                if (result != null && result instanceof String) {
                    String balance = (String) result;
                    balance = StringUtils.decString2Comma(balance);
                    vthoBalanceTextView.setText(balance);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }
        }, null);

    }

    private String[] hostNames;
    private String[] hoseNameTags;
    private AdapterView.OnItemClickListener menuItemClickListener;

    private void openMenuDialog(View view) {
        closeMemu();

        if (hostNames == null) {
            hostNames = this.getResources().getStringArray(R.array.host_name_array);
        }
        String name  = NodeUrl.CUSTOM_NODE.getName();
        if(TextUtils.isEmpty(name)) {
            NodeUrl.CUSTOM_NODE.setName(hostNames[2]);
        }else{
            hostNames[2] = name;
        }
        if (menuItemClickListener == null) {
            menuItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    changeHostState(i);
                    closeMemu();
                }
            };
        }
        PopupBuilder builder = new PopupBuilder(this);
        menu = builder.setAnchorView(view)
                .setDatas(hostNames)
                .setOnItemClickListener(menuItemClickListener)
                .build();
    }

    private void closeMemu() {
        if (menu != null) {
            menu.dismiss();
            menu = null;
        }
    }

    private void changeHostState(int i){
        switch (i){
            case 0:
                WalletUtils.setNodeUrl(NodeUrl.MAIN_NODE);
                break;
            case 1:
                WalletUtils.setNodeUrl(NodeUrl.TEST_NODE);
                break;
            case 2:
                //open setting
                NodeServerSettingActivity.open(this);
                return;
        }
        if(i>=0 && i<2 && i<hoseNameTags.length ) {
            String hostState = hoseNameTags[i];
            titleTextView.setText("VeChainThorWallet" + hostState);

            getVetBalance();
            getTokenBalance(Token.getVTHOToken());

            NodeServerInfoActivity.open(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == NodeServerSettingActivity.REQUEST_URL_CODE) {

                WalletUtils.setNodeUrl(NodeUrl.CUSTOM_NODE);

                String hostState = hoseNameTags[2];
                titleTextView.setText("VeChainThorWallet" + hostState);

                getVetBalance();
                getTokenBalance(Token.getVTHOToken());
            }else if(requestCode == ManageWalletActivity.WALLET_MANAGER){
                boolean isDeleteWallet = data.getBooleanExtra("isDeleteWallet",false);
                if(isDeleteWallet){
                    NoWalletKeystoreActivity.open(this);
                    finish();
                }
            }
        }

    }

}
