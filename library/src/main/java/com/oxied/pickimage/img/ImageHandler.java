package com.oxied.pickimage.img;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import com.oxied.pickimage.bundle.PickSetup;
import com.oxied.pickimage.enums.EPickType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static android.graphics.BitmapFactory.decodeStream;

/**
 * Created by jrvansuita build 07/02/17.
 */

public class ImageHandler {

    private Context context;
    private Uri originalUri;
    private Uri outputUri;
    private EPickType provider;
    private PickSetup setup;

    public ImageHandler(Context context) {
        this.context = context;
    }

    public static ImageHandler with(Context context) {
        return new ImageHandler(context);
    }

    public ImageHandler uri(Uri uri) {
        this.originalUri = uri;
        return this;
    }

    public ImageHandler outputUri(Uri uri) {
        this.outputUri = uri;
        return this;
    }

    public ImageHandler provider(EPickType provider) {
        this.provider = provider;
        return this;
    }

    public ImageHandler setup(PickSetup setup) {
        this.setup = setup;
        return this;
    }

    private Bitmap rotateIfNeeded(Bitmap bitmap) {
        int rotation;

        if (provider == EPickType.CAMERA) {
            rotation = getRotationFromCamera();
        } else {
            rotation = getRotationFromGallery();
        }

        return rotate(bitmap, rotation);
    }

    private int getRotationFromCamera() {
        int rotate = 0;
        try {

            ExifInterface exif = new ExifInterface(originalUri.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                default:
                    rotate = 0;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    private int getRotationFromGallery() {
        int result = 0;
        String[] columns = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(originalUri, columns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
                result = cursor.getInt(orientationColumnIndex);
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null)
                cursor.close();
        }

        return result;
    }


    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if (bitmap != null && degrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        return bitmap;
    }

    private Bitmap flip(Bitmap bitmap) {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
    }


    public Bitmap decode() throws FileNotFoundException {
        //Notify image changed
        context.getContentResolver().notifyChange(originalUri, null);

        Bitmap bitmap = resize();

        if (bitmap == null)
            throw new FileNotFoundException("File not found");

        if (provider.equals(EPickType.CAMERA) && setup.isFlipped())
            bitmap = flip(bitmap);

        bitmap = rotateIfNeeded(bitmap);

        if (setup.getSaveDecodedToFile()) {
            saveDecodedBitmapToFile(bitmap);
        }

        return bitmap;
    }

    public void saveDecodedBitmapToFile(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        try {
            //convert array of bytes into file
            FileOutputStream fileOutputStream = new FileOutputStream(new File(getOutputUriPath()));
            fileOutputStream.write(byteArray);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Uri getOriginalUri() {
        return originalUri;
    }

    public String getOriginalUriPath() {
        if (provider.equals(EPickType.CAMERA)) {
            return originalUri.getPath();
        } else {
            return getGalleryPath(originalUri);
        }
    }

    public Uri getOutputUri() {
        return outputUri;
    }

    public String getOutputUriPath() {
        if (provider.equals(EPickType.CAMERA) || setup.getSaveDecodedToFile()) {
            return outputUri.getPath();
        } else {
            return getGalleryPath(outputUri);
        }
    }

    private String getGalleryPath(Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return uri.getPath();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Bitmap resize() throws FileNotFoundException {
        BitmapFactory.Options oldOptions = getOptions();
        int newWidth = oldOptions.outWidth;
        int newHeight = oldOptions.outHeight;
        int ratioForPreScale = oldOptions.inSampleSize;

        BitmapFactory.Options newOptions = new BitmapFactory.Options();
        newOptions.inSampleSize = ratioForPreScale;
        Bitmap scaledBitmap = decodeStream(context.getContentResolver().openInputStream(originalUri), null, newOptions);

        return Bitmap.createScaledBitmap(scaledBitmap, newWidth, newHeight, false);
    }

    private BitmapFactory.Options getOptions() throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // to not load bitmap into memory
        decodeStream(context.getContentResolver().openInputStream(originalUri), null, options);

        float w = options.outWidth;
        float h = options.outHeight;
        float scale = 1;

        if (setup.getWidth() == 0 && setup.getHeight() == 0) {
            int newWidth;
            int newHeight;

            if (w <= setup.getMaxSize() && h <= setup.getMaxSize()) {
                newWidth = (int) w;
                newHeight = (int) h;
            } else {
                float scaleWidth = w / setup.getMaxSize();
                float scaleHeight = h / setup.getMaxSize();

                if (scaleWidth >= scaleHeight) {
                    scale = scaleWidth;
                } else {
                    scale = scaleHeight;
                }

                newWidth = (int) (w / scale);
                if (newWidth > setup.getMaxSize())
                    newWidth = setup.getMaxSize();

                newHeight = (int) (h / scale);
                if (newHeight > setup.getMaxSize())
                    newHeight = setup.getMaxSize();
            }

            options.outWidth = newWidth;
            options.outHeight = newHeight;
        } else {
            if (w > setup.getWidth() || h > setup.getHeight()) {
                float scaleWidth = w / setup.getWidth();
                float scaleHeight = h / setup.getHeight();
                scale = scaleWidth >= scaleHeight ? scaleWidth : scaleHeight;
            }

            options.outWidth = setup.getWidth();
            options.outHeight = setup.getHeight();
        }

        options.inSampleSize = (int) scale;
        return options;
    }

}
