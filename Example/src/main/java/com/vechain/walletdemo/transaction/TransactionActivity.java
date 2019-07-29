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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.wallet.network.Api;
import com.vechain.wallet.network.bean.common.Token;
import com.vechain.wallet.network.bean.core.TokenBalance;
import com.vechain.wallet.network.bean.core.VETBalance;
import com.vechain.wallet.network.engine.NetCallBack;
import com.vechain.wallet.network.utils.GsonUtils;
import com.vechain.wallet.utils.StringUtils;
import com.vechain.wallet.thor.tx.Clause;
import com.vechain.wallet.utils.HexUtils;
import com.vechain.walletdemo.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sending transaction
 */

public class TransactionActivity extends AppCompatActivity {

    private final static String KEYSTORE = "keystore";

    public static void open(Context context, String keystoreJson, String walletAddress, Token token) {

        if (TextUtils.isEmpty(walletAddress) || token == null) return;

        String tokenString = GsonUtils.toString(token);
        Intent intent = new Intent(context, TransactionActivity.class);

        intent.putExtra(KEYSTORE, keystoreJson);
        intent.putExtra(ADDRESS, walletAddress);
        intent.putExtra(Token.class.getName(), tokenString);

        context.startActivity(intent);
    }

    private final static String ADDRESS = "address";


    private EditText toAddressEditText;
    private EditText amountEditText;
    private TextView totalBalanceTextView;

    private Button transferNextButton;

    private String keystoreJson;
    private String walletAddress;
    private Token currentToken;
    private String vthoBalance;
    private String vetBalance;

    private boolean isFirst = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activit_transact);
        initParams();
        initView();
    }

    private void initParams() {

        Intent intent = getIntent();
        walletAddress = intent.getStringExtra(ADDRESS);
        keystoreJson = intent.getStringExtra(KEYSTORE);
        String json = intent.getStringExtra(Token.class.getName());
        currentToken = GsonUtils.fromJson(json, Token.class);
    }

    private void initView() {
        toAddressEditText = findViewById(R.id.receiver_wallet_address);
        amountEditText = findViewById(R.id.transfer_count);
        totalBalanceTextView = findViewById(R.id.total_balance);
        transferNextButton = findViewById(R.id.transfer_next_button);


        toAddressEditText.setText("0xe002107ad60e83f7762105504fac50730805f3ab");

        ImageView tokenImageView = findViewById(R.id.tokenIcon);
        TextView selectTokenButton = findViewById(R.id.select_token_button);

        String symbol = "";
        if (currentToken != null) symbol = currentToken.getSymbol();
        selectTokenButton.setText(symbol);
        if (Token.VET.equals(symbol)) {
            tokenImageView.setImageResource(R.drawable.logo_list);
        } else if (Token.VTHO.equals(symbol)) {
            tokenImageView.setImageResource(R.drawable.thunder_x_node);
        }

        RxView.clicks(transferNextButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    transfer();

                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            isFirst = false;
            if (currentToken.getSymbol().equals(Token.VET)) {
                getVetBalance();
            }
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
                    vetBalance = (String) result;

                    if (currentToken.getSymbol().equals(Token.VET)) {
                        totalBalanceTextView.setText(vetBalance);
                        totalBalanceTextView.setTag(vetBalance);
                    }
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
                    if (decimals < Token.RETAIN) decimals = Token.RETAIN;
                    value = StringUtils.hexString2DecimalString(hex, decimals, Token.RETAIN);
                }

                return value;
            }

            @Override
            public void onSuccess(Object result) {

                if (result != null && result instanceof String) {
                    vthoBalance = (String) result;

                    if (currentToken.getSymbol().equals(Token.VTHO)) {
                        totalBalanceTextView.setText(vthoBalance);
                        totalBalanceTextView.setTag(vthoBalance);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }
        }, null);

    }


    private void transfer() {

        if (!checkAddress()) return;
        if (!checkAmount()) return;

        inputPasswordToTransfer();
    }

    private boolean checkAddress() {
        //to Wallet Check
        String toAddress = toAddressEditText.getText().toString();
        if (TextUtils.isEmpty(toAddress)) {
            Toast.makeText(this, R.string.transfer_coin_to_address_error_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        //Must start with 0x
        if (!isValidPrefix(toAddress)) {
            Toast.makeText(this, R.string.transfer_coin_to_address_error_format, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (toAddress.length() != 42) {
            Toast.makeText(this, R.string.transfer_coin_to_address_error_format, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (toAddress.equalsIgnoreCase(walletAddress)) {
            //Can't pass on to oneself
            Toast.makeText(this, R.string.transfer_coin_to_from_same_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        //Is there case or not?
        String tempLowerToAddress = toAddress.toLowerCase();

        if (!StringUtils.isHexString(tempLowerToAddress)) {
            Toast.makeText(this, R.string.address_char_not_hex, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

    private boolean checkAmount() {
        //转账金额
        String valueString = amountEditText.getText().toString();
        if (TextUtils.isEmpty(valueString)) {
            Toast.makeText(this, R.string.transfer_coin_value_empty_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (valueString.length() >= 2) {
            char[] charArray = valueString.toCharArray();
            //The number at the beginning of 0, the second is the point, and the third is the point.
            if (charArray[0] == '0' && (charArray[1] != '.' || charArray.length < 3)) {
                Toast.makeText(this, R.string.transfer_coin_value_format_error, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        //balance
        String balanceString = (String) totalBalanceTextView.getTag();
        if (TextUtils.isEmpty(balanceString)) balanceString = "0.00";

        //VTHO balance
        String vthoBalanceString = "0";
        if (vthoBalance != null) {
            vthoBalanceString = vthoBalance;
        }


        BigDecimal balanceDecimal = StringUtils.commaString2BigDecimal(balanceString);
        BigDecimal valueDecimal = StringUtils.commaString2BigDecimal(valueString);
        BigDecimal vthoBalanceDecimal = StringUtils.commaString2BigDecimal(vthoBalanceString);

        int result = valueDecimal.compareTo(new BigDecimal("0"));
        if (result < 0) {
            Toast.makeText(this, R.string.transfer_wallet_value_not_negative, Toast.LENGTH_SHORT).show();
            return false;
        }
        //Except VET, the transfer amount should not be 0.
        if (result == 0 && !Token.VET.equalsIgnoreCase(currentToken.getSymbol())) {
            Toast.makeText(this, R.string.transaction_value_not_zero, Toast.LENGTH_SHORT).show();
            return false;
        }


        if (Token.VTHO.equalsIgnoreCase(currentToken.getSymbol())) {
            //Is the balance sufficient?
            result = vthoBalanceDecimal.compareTo(valueDecimal);
            if (result < 0) {
                //Sorry, your credit is running low
                Toast.makeText(this, R.string.transfer_wallet_send_balance_not_enough, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            //Is the balance sufficient?
            result = balanceDecimal.compareTo(valueDecimal);
            if (result < 0) {
                //Sorry, your credit is running low
                Toast.makeText(this, R.string.transfer_wallet_send_balance_not_enough, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }


    private boolean isValidPrefix(String address) {
        int prefixLength = "0x".length();
        if (address == null || address.length() < prefixLength)
            return false;

        String prefix = address.substring(0, prefixLength);

        return prefix.equalsIgnoreCase("0x");
    }


    private void inputPasswordToTransfer() {

        Clause clause = null;
        String symbol = currentToken.getSymbol();
        if (Token.VET.equals(symbol)) {

            clause = getVETParams();
        } else {
            clause = getTokenParams();
        }

        List<Clause> clauseList = new ArrayList<>();
        clauseList.add(clause);


        PasswordDialog.showInputPassword(this, keystoreJson, clauseList, 0, new TransferResultListener() {
            @Override
            public void onTransferResult(String result) {

                if (result != null) {
                    Log.v("success", "txId=" + result);
                } else {
                    Toast.makeText(TransactionActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private Clause getVETParams() {

        Clause clause = new Clause();

        String toAddress = toAddressEditText.getText().toString();
        toAddress = toAddress.toLowerCase();
        clause.setTo(toAddress);

        String amount = amountEditText.getText().toString();
        //Convert to Wei value(amount*10^18）(hex values),VET decimals = 18
        String hexAmountWei = StringUtils.string2LargeHex(amount, Token.DECIMALS);
        clause.setValue(hexAmountWei);

        clause.setData(null);

        return clause;
    }

    private Clause getTokenParams() {

        Clause clause = new Clause();

        //token contract address
        clause.setTo(currentToken.getAddress());
        clause.setValue("0");


        String toAddress = toAddressEditText.getText().toString();
        toAddress = toAddress.toLowerCase();

        String amount = amountEditText.getText().toString();
        //Convert to Wei value（amount*10^18）(hex values),The value of VTHO decimal is 18,
        // and other tokens need to be queried by the decimals () method of the token address.
        // blockChain node Url+/accounts/{token address}  send post data: {"value":"0x0","data":"0x313ce567"}
        //@see Api.getTokenDecimals(String tokenAddress, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider)
        String hexAmountWei = StringUtils.string2LargeHex(amount, Token.DECIMALS);


        String[] args = {toAddress, hexAmountWei};

        String data = getTransferABIEncode(args);
        clause.setData(data);

        return clause;
    }

    private String getTransferABIEncode(String[] args) {

        //token transfer: transfer(address,uint256)
        //Keccak-256("transfer(address,uint256)")="0xa9059cbb2ab09eb219583f4a59a5d0623ade346d962bcd4e46b11da047c9049b"
        //Take the first four bytes：0xa9059cbb
        //Reference resources：
        //1.https://www.4byte.directory/signatures/
        //2.https://github.com/vechain/VIPs/blob/master/VIP-180.md
        String tokenMethodId = "0xa9059cbb";
        StringBuffer buffer = new StringBuffer();
        buffer.append(tokenMethodId);

        for (String item : args) {
            buffer.append(getParamValue(item));
        }

        return buffer.toString();
    }

    private String getParamValue(String item) {
        int size = 0;
        if (item == null || item.isEmpty())
            size = 0;
        else
            size = item.length();
        String hexValue = "";

        if (size > 0) {
            //去掉0x开头
            hexValue = HexUtils.cleanHexPrefix(item);
            size = hexValue.length();
        }

        int count = 64;
        StringBuffer buffer = new StringBuffer();
        int n = count - size;
        if (n > 0) {
            for (int i = 0; i < n; i++)
                buffer.append("0");
        }
        buffer.append(hexValue);
        return buffer.toString();
    }


}
