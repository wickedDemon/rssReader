package com.rssreader.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

public abstract class BaseRecyclerViewAdapter<T extends  RecyclerView.ViewHolder> extends CursorRecyclerViewAdapter<T> {

    public BaseRecyclerViewAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void changeCursor(Cursor cursor) {
        reunit(cursor);
        super.changeCursor(cursor);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        reunit(newCursor);
        return super.swapCursor(newCursor);
    }

    protected abstract void reunit(Cursor cursor);
}

