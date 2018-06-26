package com.trywang.androidutilslibrary.photo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 图片的工具类
 * 根据uri读取图片 TODO try 待完善 图片的加载大小
 * @author Try
 * @date 2018/6/22 11:17
 */
public class BitmapUtils {
    private static final String TAG = BitmapUtils.class.getSimpleName();

    /**
     * 通过uri返回bitmap
     *
     * @param context context
     * @param uri     uri
     * @return Bitmap
     */
    public static Bitmap getBitmapByUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        FileInputStream fis = null;
        ParcelFileDescriptor pfd = null;
        Bitmap b = null;
        try {
            pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            assert pfd != null;
            fis = new FileInputStream(pfd.getFileDescriptor());

            //TODO try 在此处做压缩
            b = BitmapFactory.decodeStream(fis);
            if (b == null) {
                return null;
            }

            Matrix m = getMatrixForBitmapByUri(context, uri);
            if (m != null) {
                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, false);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (pfd != null) {
                    pfd.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return b;
    }

    /**
     * 根据图片uri获取是否需要旋转的matrix
     *
     * @param context context
     * @param uri     图片uri
     * @return matrix 不需要旋转则返回为null;
     */
    public static Matrix getMatrixForBitmapByUri(Context context, Uri uri) {
        ParcelFileDescriptor pfdDegree = null;
        Matrix m = null;
        try {
            m = new Matrix();
            int degree;//图片应该旋转的角度（顺时针）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //如果复用pfd则无法获取角度异常：应该是fis在读取造成的
                //ExifInterface: Invalid image: ExifInterface got an unsupported image format file
                // (ExifInterface supports JPEG and some RAW image formats only) or a corrupted JPEG file to ExifInterface.
                //    java.io.EOFException
                pfdDegree = context.getContentResolver().openFileDescriptor(uri, "r");
                assert pfdDegree != null;
                degree = getPicDegree(pfdDegree.getFileDescriptor());
            } else {
                //此处如果target=23则需要WRITE_EXTERNAL_STORAGE权限，否则报FileNotFindException
                //所以最好是在打开相册的时候就获取此权限
                degree = getPicDegree(BitmapPathUtils.getPath(context, uri));
            }

            if (degree == 0) {
                return null;
            }
            m.postRotate(degree);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pfdDegree != null) {
                    pfdDegree.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return m;
    }

    /**
     * 根据文件真实路径获取图片的旋转角度
     * 注意此处只能在23以及23以下的target中使用  否则报FileNotFindException
     * 24获取则使用{@link #getPicDegree(FileDescriptor)}
     *
     * @param path 真实路径
     * @return 旋转的角度
     */
    public static int getPicDegree(String path) {
        try {
            return getPicDegree(new ExifInterface(path));
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * 根据文件获取图片的旋转角度
     *
     * @param fileDescriptor fileDescriptor
     * @return 旋转的角度
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static int getPicDegree(FileDescriptor fileDescriptor) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return getPicDegree(new ExifInterface(fileDescriptor));
            }
        } catch (IOException e) {
            return 0;
        }
        return 0;
    }

    /**
     * 根据ExifInterface获取图片的旋转角度
     *
     * @param exifInterface exifInterface
     * @return 旋转的角度
     */
    public static int getPicDegree(ExifInterface exifInterface) {
        if (exifInterface == null) {
            return 0;
        }
        int degree = 0;
        try {
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("TAG", "readPictureDegree: 旋转了 = " + degree);
        return degree;
    }


}
