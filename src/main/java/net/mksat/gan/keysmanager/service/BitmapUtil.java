package net.mksat.gan.keysmanager.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: Oleksandr Cherepko
 * Date: 10/1/13
 * Time: 11:56 AM
 */
public class BitmapUtil {
    private static final String TAG = "BitmapUtil";
    private Context context;

    public BitmapUtil(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Decode input stream into bitmap object.
     * If stream is null this method will return null. After decoding stream will be closed.
     *
     * @param stream InputStream which will be decoded into bitmap object
     * @return Bitmap object or null if error occurred
     */
    public static Bitmap getBitmapFromStream(InputStream stream) {
        if (stream == null)
            return null;
        Bitmap bm = BitmapFactory.decodeStream(stream);
        try {
            stream.close();
        } catch (IOException e) {
            Log.e(TAG, "getBitmapFromStream() - Can't close input stream", e);
        }
        return bm;
    }

    /**
     * Convert bitmap according to device screen size to avoid OutOfMemory exception in future.
     *
     * @param srcFileAbsolutePath Absolute path to source      bitmap file to be converted
     * @param dstFileAbsolutePath Absolute path to destination bitmap file to convert to
     * @return true if bitmap was converted successfully, false otherwise
     */
    public boolean convertBitmap(String srcFileAbsolutePath, String dstFileAbsolutePath) {
        BitmapFactory.Options options = getBitmapBoundaries(srcFileAbsolutePath);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        if ((imageHeight <= 0) || (imageWidth <= 0)) {
            Log.v(TAG, "convertBitmap() - Can't determine image size");
            return false;
        }

        Log.v(TAG, String.format("convertBitmap() - Original image size: W: %d px, H: %d px", imageWidth, imageHeight));
        int maxWidth = 800; // AndroidUtil.getScreenWidth(context); ????????????????????????????????????????????????????????????????????????
        Log.v(TAG, String.format("convertBitmap() - Screen size: W: %d px", maxWidth));
        if (maxWidth > imageWidth)
            maxWidth = imageWidth;
        int inSampleSize = getInSampledSize(srcFileAbsolutePath, maxWidth);
        Log.v(TAG, String.format("convertBitmap() - Reading bitmap with sampling factor %d", inSampleSize));
        Bitmap sampledSrcBitmap = decodeInSampled(srcFileAbsolutePath, inSampleSize);
        if (sampledSrcBitmap == null) {
            Log.v(TAG, "convertBitmap() - Can't decode source bitmap");
            return false;
        }
        Log.v(TAG, String.format("convertBitmap() - Sampled bitmap W: %dpx, H: %dpx", sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight()));
        float desiredScale = (float) maxWidth / sampledSrcBitmap.getWidth();
        Log.v(TAG, String.format("convertBitmap() - Converting bitmap with scaling of %.2f", desiredScale));
        Bitmap scaledBitmap = rescaleBitmap(sampledSrcBitmap, desiredScale);
        if (scaledBitmap == null) {
            Log.v(TAG, "convertBitmap() - Can't create scaled bitmap");
            return false;
        }
        return saveBitmap(scaledBitmap, dstFileAbsolutePath);
    }

    /**
     * Rescale bitmap using provided scale keeping existing bitmap aspect ratio.
     *
     * @param bitmap Not null Bitmap to be rescaled
     * @param scale  Scale ratio
     * @return rescaled bitmap or null if error occurred
     */
    public Bitmap rescaleBitmap(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix(); // может пакет(библиотека) не тот import android.graphics.Matrix; ?????????????????????
        matrix.postScale(scale, scale);
        try {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "rescaleBitmap() - Can't create scaled bitmap. Arguments are out of range", e);
        }
        return null;
    }

    /**
     * Save bitmap to png file.
     *
     * @param bitmap Bitmap to be saved
     * @param path   Path to file
     * @return true if bitmap was saved successfully, false otherwise
     */
    public boolean saveBitmap(Bitmap bitmap, String path) {
        boolean result = false;
        try {
            FileOutputStream out = new FileOutputStream(path);
            result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "saveBitmap() - Can't write to file", e);
        } catch (IOException e) {
            Log.e(TAG, "saveBitmap() - Can't close output stream", e);
        }
        return result;
    }

    /**
     * Decode bitmap file by path with provided insample ratio.
     * This method should be using for reading large images to prevent OutOfMemory exception
     *
     * @param path         Path to file
     * @param inSampleSize insample size ratio. Preferred to be power of 2
     * @return Decoded bitmap file
     */
    public Bitmap decodeInSampled(String path, int inSampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inSampleSize = inSampleSize;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Decode bitmap without loading it into memory
     *
     * @param path Path to bitmap
     * @return Decoded bitmap options
     */
    public BitmapFactory.Options getBitmapBoundaries(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options;
    }

    /**
     * Decode bitmap boundaries and calculate insampled size can be used to decode image with specified width
     *
     * @param path  Path to bitmap
     * @param width Width in px to fit
     * @return InSampled size
     */
    public int getInSampledSize(String path, int width) {
        int imageWidth = getBitmapBoundaries(path).outWidth;
        Log.v(TAG, String.format("getInSampledSize() - Image width %d px", imageWidth));
        int inSampleSize = 1;
        while (imageWidth / 2 >= width) {
            imageWidth /= 2;
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    /**
     * Decode bitmap boundaries and calculate insampled size can be used to decode image with specified width or height.
     * The calculated insampled size will be based on bigger dimension value
     *
     * @param path   Path to bitmap
     * @param width  Width in px to fit
     * @param height Height in px to fit
     * @return InSampled size
     */
    public int getInSampledSize(String path, int width, int height) {
        int bmDimension, sampledDimension;
        if (width > height) {
            bmDimension = getBitmapBoundaries(path).outWidth;
            sampledDimension = width;
        } else {
            bmDimension = getBitmapBoundaries(path).outHeight;
            sampledDimension = height;
        }
        Log.v(TAG, String.format("getInSampledSize() - Image width %d px", bmDimension));
        int inSampleSize = 1;
        while (bmDimension / 2 >= sampledDimension) {
            bmDimension /= 2;
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    /**
     * Parse color string and return color as integer
     *
     * @param color        Color string to be parsed
     * @param defaultColor Color shoud be returned in case of error
     * @return Return color int
     */
    public static int colorFromString(String color, int defaultColor) {
        int result = defaultColor;
        try {
            if (!TextUtils.isEmpty(color)) {
                color = color.replace("#", "");
                result = Integer.valueOf(color, 16);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Can't parse hex string to int", e);
        }
        return result | 0xFF000000;
    }
}
