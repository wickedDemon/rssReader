package com.rssreader.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rssreader.R;
import com.rssreader.listener.ItemClickListener;
import com.rssreader.provider.FeedData;
import com.rssreader.utils.ImageUtils;
import com.rssreader.utils.TimeUtils;

import java.util.Date;

public class FeedEntryAdapter extends BaseRecyclerViewAdapter<FeedEntryAdapter.ViewHolder> {

    private int namePos, imagePos, datePos;

    private ItemClickListener clickListener;
    private Context context;

    public FeedEntryAdapter(Context context) {
        super(context, null);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entry_feed, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, long id) {
                if (clickListener != null) {
                    clickListener.onClick(view, position, id);
                }
            }

            @Override
            public void onLongClick(View view, int position, long id) {}
        });

        return holder;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView itemImage;
        TextView itemTitle;
        TextView itemDate;
        public long id;

        public ItemClickListener clickListener;

        public ViewHolder(View view) {
            super(view);
            itemTitle = (TextView) view.findViewById(R.id.entryTitle);
            itemDate = (TextView) view.findViewById(R.id.entryDate);
            itemImage = (ImageView) view.findViewById(R.id.entryImage);
            view.setOnLongClickListener(this);
            view.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        public void setItemId(long id) {
            this.id = id;
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onClick(view, getAdapterPosition(), id);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (clickListener != null) {
                clickListener.onClick(view, getAdapterPosition(), id);
            }
            return true;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final Cursor cursor, final int position) {
        holder.itemTitle.setText(cursor.getString(namePos));
        setElapsedTime(holder.itemDate, cursor.getLong(datePos));
        holder.setItemId(getItemId(position));
        ImageUtils.loadImageToVeiw(context, holder.itemImage, cursor.getString(imagePos), R.drawable.ic_no_image, 0, false);
    }

    private void setElapsedTime(TextView textView, long entryTime) {
        long duration = new Date().getTime() - entryTime;
        textView.setText(TimeUtils.getHumanReadableTimeDuration(duration) + " ago");
    }

    @Override
    public void reunit(Cursor cursor) {
        if (cursor != null) {
            namePos = cursor.getColumnIndex(FeedData.FeedEntries.NAME);
            imagePos = cursor.getColumnIndex(FeedData.FeedEntries.IMAGE_URL);
            datePos = cursor.getColumnIndex(FeedData.FeedEntries.PUB_DATE);
        }
    }
}
