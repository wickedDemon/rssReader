package com.rssreader.listener;

import android.view.View;

public interface ItemClickListener {

    void onClick(View view, int position, long id);

    void onLongClick(View view, int position, long id);
}