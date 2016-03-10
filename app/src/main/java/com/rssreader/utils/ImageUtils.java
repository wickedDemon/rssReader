package com.rssreader.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageUtils {

    private static final Pattern IMG_URL_PATTERN = Pattern.compile("^https?://(?:[a-z0-9\\-]+\\.)+[a-z]{2,6}(?:/[^/#?]+)+\\.(?:jpg|png)$", Pattern.CASE_INSENSITIVE);

    public static void loadImageToVeiw(Context context, ImageView imageView, String imageUrl,
                                       int errorDrawable, int cornerRadius, boolean fitImage) {
        if (imageUrl == null) {
            imageView.setImageResource(errorDrawable);
            return;
        }

        RequestCreator picassoCreator = Picasso.with(context).load(imageUrl);
        if (fitImage) {
            picassoCreator.fit();
        }
        picassoCreator.error(errorDrawable)
                .transform(new ImageUtils.RoundedTransformation(cornerRadius, 0))
                .into(imageView);
    }

    public static class RoundedTransformation implements com.squareup.picasso.Transformation {

        private final int radius;
        private final int margin;

        public RoundedTransformation(final int radius, final int margin) {
            this.radius = radius;
            this.margin = margin;
        }

        @Override
        public Bitmap transform(final Bitmap source) {
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP));

            Bitmap output = Bitmap.createBitmap(source.getWidth(),
                    source.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawRoundRect(new RectF(margin, margin, source.getWidth()
                    - margin, source.getHeight() - margin), radius, radius, paint);

            if (source != output) {
                source.recycle();
            }

            return output;
        }

        @Override
        public String key() {
            return "rounded";
        }
    }

    public static boolean isCorrectImage(String imgUrl) {
        Matcher matcher = IMG_URL_PATTERN.matcher(imgUrl);
        return matcher.matches();
    }
}
