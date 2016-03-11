package com.rssreader.adapter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
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

public class BaseFeedAdapter extends BaseRecyclerViewAdapter<BaseFeedAdapter.ViewHolder> {

    private int titlePos, urlPos, isFavoritePos, imagePos;

    private ItemClickListener clickListener;
    private Context context;
    private boolean favoriteMode;


    public BaseFeedAdapter(Context context, boolean favoriteMode) {
        super(context, null);
        this.context = context;
        this.favoriteMode = favoriteMode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feed, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        viewHolder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, long id) {
                if (clickListener != null) {
                    clickListener.onClick(view, position, id);
                }
            }

            @Override
            public void onLongClick(View view, int position, long id) {}
        });
        return viewHolder;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ImageView imageFavorite;
        public TextView feedTitle;
        public TextView feedUrl;
        public ImageView feedImage;
        public long id;

        public ItemClickListener clickListener;

        public ViewHolder(View view) {
            super(view);
            imageFavorite = (ImageView) view.findViewById(R.id.imageFavorite);
            feedTitle = (TextView) view.findViewById(R.id.feedTitle);
            feedImage = (ImageView) view.findViewById(R.id.feedImage);
            feedUrl = (TextView) view.findViewById(R.id.feedUrl);
            view.setOnLongClickListener(this);
            view.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onClick(view, getAdapterPosition(), id);
            }
        }

        public void setItemId(long id) {
            this.id = id;
        }

        @Override
        public boolean onLongClick(View view) {
            if (clickListener != null) {
                clickListener.onLongClick(view, getAdapterPosition(), id);
            }
            return true;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final Cursor cursor, final int position) {
        holder.feedTitle.setText(cursor.getString(titlePos));
        holder.feedUrl.setText(cursor.getString(urlPos));
        ImageUtils.loadImageToVeiw(context, holder.feedImage, cursor.getString(imagePos), R.drawable.ic_no_image, 2, false);

        int isFavorite = cursor.getInt(isFavoritePos);
        final long itemId = getItemId(position);
        final boolean favorite = isFavorite == 1;
        holder.imageFavorite.setImageResource(favorite ? R.drawable.ic_toggle_star : R.drawable.ic_toggle_star_outline);
        holder.imageFavorite.setVisibility(favoriteMode ? View.GONE : View.VISIBLE);
        holder.setItemId(itemId);
        holder.imageFavorite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ContentResolver cr = context.getContentResolver();
                Uri mUriId = ContentUris.withAppendedId(FeedData.FeedNews.CONTENT_URI, itemId);

                ContentValues values = new ContentValues();
                values.put(FeedData.FeedNews.IS_FAVORITE, favorite ? 0 : 1);
                cr.update(mUriId, values, null, null);

                Cursor cursor = cr.query(FeedData.FeedNews.CONTENT_URI, null, null, null, null);
                changeCursor(cursor);
            }
        });
    }

    @Override
    protected void reunit(Cursor cursor) {
        if (cursor != null) {
            titlePos = cursor.getColumnIndex(FeedData.FeedNews.NAME);
            urlPos = cursor.getColumnIndex(FeedData.FeedNews.URL);
            isFavoritePos = cursor.getColumnIndex(FeedData.FeedNews.IS_FAVORITE);
            imagePos = cursor.getColumnIndex(FeedData.FeedNews.IMAGE_URL);
        }
    }
}
