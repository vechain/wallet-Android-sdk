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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding3.view.RxView;
import com.vechain.walletdemo.R;
import com.vechain.walletdemo.view.NotDragViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Keystore import wallet or mnemonic import
 */

public class ImportWalletActivity extends AppCompatActivity {

    public final static void open(Context context){
        Intent intent = new Intent(context,ImportWalletActivity.class);
        context.startActivity(intent);
    }


    private TextView item1TextView;
    private View itemLine1View;
    private RelativeLayout item1Layout;
    private TextView item2TextView;
    private View itemLine2View;
    private RelativeLayout item2Layout;
    private NotDragViewPager viewPager;

    private List<Fragment> pages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_wallet);
        initView();
    }

    private void initView(){
        item1TextView = findViewById(R.id.itemText1);
        itemLine1View = findViewById(R.id.itemLine1);
        item1Layout = findViewById(R.id.item1);

        item2TextView = findViewById(R.id.itemText2);
        itemLine2View = findViewById(R.id.itemLine2);
        item2Layout = findViewById(R.id.item2);

        viewPager = findViewById(R.id.viewPager);

        selectItem(0);
        RxView.clicks(item1Layout)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    selectItem(0);
                    viewPager.setCurrentItem(0);
                });

        RxView.clicks(item2Layout)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> {
                    selectItem(1);
                    viewPager.setCurrentItem(1);
                });

        pages = new ArrayList<Fragment>();

        Fragment page = ImportWalletByMnemonicWordsFragment.get();
        pages.add(page);

        page = ImportWalletByKeystoreFragment.get();
        pages.add(page);


        viewPager.setAdapter(new NavigationPageAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);
    }

    private void selectItem(int i) {
        if (i == 0) {
            item1TextView.setSelected(true);
            itemLine1View.setVisibility(View.VISIBLE);

            item2TextView.setSelected(false);
            itemLine2View.setVisibility(View.INVISIBLE);

        } else if (i == 1) {
            item1TextView.setSelected(false);
            itemLine1View.setVisibility(View.INVISIBLE);

            item2TextView.setSelected(true);
            itemLine2View.setVisibility(View.VISIBLE);
        }
    }


    class NavigationPageAdapter extends FragmentPagerAdapter {
        public NavigationPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return pages.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "" + position;
        }

        @Override
        public int getCount() {
            return pages.size();
        }
    }
}
