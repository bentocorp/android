package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.PictureDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.nostra13.universalimageloader.cache.disc.impl.BaseDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

public class ImageUtils {

    private static ImageLoader imageLoader;
    private static DisplayImageOptions imgOptions;
    private static File cacheDir;

    public static ImageLoader initImageLoader() {
        if (imageLoader == null) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(BentoApplication.instance).threadPriority(5)
                    .diskCache(new BaseDiskCache(getImageDirectory(BentoApplication.instance)) {
                    })// .writeDebugLogs()
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .memoryCacheExtraOptions(800, 800)
                    .build();
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
        }
        return imageLoader;
    }


    private static File getImageDirectory(final Context context) {
        if (cacheDir == null)
            cacheDir = StorageUtils.getCacheDirectory(context);

        return cacheDir;
    }

    public static void clearCacheFromImages() {
        initImageLoader().clearDiskCache();
    }

    public static DisplayImageOptions dishImageOptions() {
        if (imgOptions == null)
            imgOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.menu_placeholder)
                    .showImageForEmptyUri(R.drawable.menu_placeholder)
                    .showImageOnFail(R.drawable.menu_placeholder)
                    .cacheInMemory(true).cacheOnDisk(true)
                    .bitmapConfig(Config.ARGB_8888)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
        return imgOptions;
    }


    /**
     * Method that crops a bitmap in the center and make it a square
     *
     * @param srcBmp The Bitmap that is going to be crop
     * @return Bitmap The bitmap croping in the center
     */
    public static Bitmap cropCenterBitmap(Bitmap srcBmp) {
        Bitmap bitmap = null;

        if (srcBmp.getWidth() >= srcBmp.getHeight())
            bitmap = Bitmap.createBitmap(srcBmp, srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2, 0, srcBmp.getHeight(), srcBmp.getHeight());
        else
            bitmap = Bitmap.createBitmap(srcBmp, 0, srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2, srcBmp.getWidth(), srcBmp.getWidth());

        return bitmap;
    }

    public static Bitmap getBitmapThumbnailSmall(Bitmap bitmap) {
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 300, 300);
        return bitmap;
    }

    public static Bitmap decodeSampledBitmapFromPath(String uriPath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uriPath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(uriPath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap pictureDrawable2Bitmap(PictureDrawable pictureDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(), pictureDrawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(pictureDrawable.getPicture());
        return bitmap;
    }

    public static Bitmap rotate(final int degrees, Bitmap mBitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        return mBitmap;
    }

    public static Bitmap getImageFromFile(Context context, String sAbsolutePath) {
        return initImageLoader().loadImageSync(sAbsolutePath);
    }


    public static Bitmap flipImageExif(Bitmap bmp, int orientation) {
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                bmp = ImageUtils.rotate(90, bmp);
                DebugUtils.logDebug("Otientation", "90");
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                DebugUtils.logDebug("Otientation", "180");
                bmp = ImageUtils.rotate(180, bmp);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                bmp = ImageUtils.rotate(270, bmp);
                break;
            default:
                DebugUtils.logDebug("Otientation", "default");
                break;
            // etc.
        }
        return bmp;
    }


}
