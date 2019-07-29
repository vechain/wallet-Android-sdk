
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

package com.vechain.walletdemo.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;


import com.vechain.walletdemo.R;
import com.vechain.walletdemo.view.adapter.CommonAdapter;
import com.vechain.walletdemo.view.adapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;



public class PopupBuilder {
    private Context context;
    private BaseAdapter adapter;
    private ArrayList<String> datas;
    private AdapterView.OnItemClickListener onItemClickListener;
    private View anchorView;
    public PopupBuilder(Context context){
        this.context = context;
    }

    public PopupBuilder setDatas(String[] items) {
        datas = new ArrayList<>();
        for(String item:items){
            datas.add(item);
        }
        this.adapter = new MenuAdapter(context,R.layout.item_popup_list,datas);
        return this;
    }

    public PopupBuilder setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public PopupBuilder setAnchorView(View anchorView) {
        this.anchorView = anchorView;
        return this;
    }

    public PopupWindow build(){
        View popupView = LayoutInflater.from(context).inflate(R.layout.layout_popup, null);
        ListView listView = (ListView) popupView.findViewById(R.id.listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);

        Resources resources = context.getResources();
        int width = resources.getDimensionPixelSize(R.dimen.menu_popup_width);
        int height = resources.getDimensionPixelSize(R.dimen.menu_popup_height);
        PopupWindow window = new PopupWindow(popupView, width, height);
        // Set animation
        window.setAnimationStyle(R.style.popup_window_anim);
        // Set the background color
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00FFFFFF")));
        Drawable drawable = resources.getDrawable(R.drawable.dialog_menu_bg);
        window.setBackgroundDrawable(drawable);
        // Settings to get focus
        window.setFocusable(true);
        // Set the area outside the pop-up box that you can touch
        window.setOutsideTouchable(true);
        // Update the status of popup window
        window.update();
        //  Display in a drop-down manner, and you can set the display location
        window.showAsDropDown(anchorView, 0, 10);
        return window;
    }


    private class MenuAdapter extends CommonAdapter<String> {

        public MenuAdapter(Context context, final int layoutId, List<String> datas){
            super(context,layoutId,datas);
        }
        @Override
        protected void convert(ViewHolder viewHolder, String item, int position) {

            viewHolder.setText(R.id.text,item);

        }
    }
}
