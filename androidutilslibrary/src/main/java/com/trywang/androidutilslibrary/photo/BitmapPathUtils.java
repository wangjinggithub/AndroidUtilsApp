package com.trywang.androidutilslibrary.photo;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * 图片路径的工具类
 *
 * @author Try
 * @date 2018/6/22 11:35
 */
public class BitmapPathUtils {
    public static final String TAG = BitmapPathUtils.class.getSimpleName();

    public static File createTakePhotoFile(Context context) {
        String fileName = "take_photo";
        fileName += String.valueOf(("try_image_" + System.currentTimeMillis()).hashCode());
        fileName += ".jpg";
        return createTempFile(context.getExternalCacheDir(), fileName);
    }


    /**
     * 创建一个临时图片文件 为.jpg或者.jpeg结尾，其他一律转换为.png后缀
     * @param parent file
     * @param fileName fileName
     * @return
     */
    public static File createTempFile(File parent, String fileName) {
        File file = null;
        try {
            if (fileName == null || "".equalsIgnoreCase(fileName)) {
                return null;
            } else {
                String[] res = fileName.split("\\.");
                String suffix = res[res.length - 1];
                if (!suffix.equalsIgnoreCase("jpg")
                        && !suffix.equalsIgnoreCase("jpeg")) {
                    fileName += ".jpg";
                }
            }
            file = new File(parent, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 创建临时文件
     * 文件路径在getExternalCacheDir()
     *
     * @param context context
     * @return uri
     */
    public static Uri createUri(Context context) {
        return createUri(context, context.getPackageName() + ".fileProvider");
    }

    /**
     * 创建临时文件
     * 文件路径在getExternalCacheDir()
     *
     * @param context   context
     * @param authority authority
     * @return uri
     */
    public static Uri createUri(Context context, String authority) {
        try {
            return createUri(context, authority, createTakePhotoFile(context));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建临时文件
     *
     * @param context   context
     * @param authority authority
     * @param path      path
     * @return uri
     */
    public static Uri createUri(Context context, String authority, String path) {
        if (path == null) {
            return createUri(context, authority);
        } else {
            return createUri(context, authority, new File(path));
        }
    }

    /**
     * 创建临时文件
     *
     * @param context   contxt
     * @param authority authority
     * @param file      file
     * @return uri
     */
    public static Uri createUri(Context context, String authority, File file) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 支持Android7.0 如果在6.0使用则获取不到真实路径
                return FileProvider.getUriForFile(context, authority, file);
            } else {
                return Uri.fromFile(file);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "createUri: 文件路径异常！检查xml中的provider_path.xml是否包含了创建的目录");
            if (file != null) {
                Log.e(TAG, "createUri: 文件路径:" + file.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取uri的真实路径
     * //  4.4以上  content://com.android.providers.media.documents/document/image:3952
     * //  4.4以下  content://media/external/images/media/3951
     *
     * @param context
     * @param uri
     * @return
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Android 4.4以下版本自动使用该方法
     * 7.0通过FileProvider生成的uri无法获取真实路径
     *
     * @param context
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * 从URI 中获取图片真实地址
     * TODO 此方法不全 禁用
     *
     * @param data uri
     * @return path
     */
    public static String getImagePath(Context context, Uri data) {
        if (data == null) return null;
        Cursor returnCursor = null;
        String path = null;
        try {
            //此处两种方式在各个版本都可以（测试机器小米5x 7.0，三星6.0）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                returnCursor = context.getContentResolver().query(data, null, null, null, null);
            } else {
                CursorLoader cursorLoader = new CursorLoader(context, data, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                returnCursor = cursorLoader.loadInBackground();
            }

            if (returnCursor == null) {
                return null;
            }
            int dataIndex = returnCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            if (dataIndex < 0) {
                return null;
            }

            returnCursor.moveToFirst();
            path = returnCursor.getString(dataIndex);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (returnCursor != null) {
                returnCursor.close();
            }
        }
        Log.i("TAG", "getImagePath: " + path);
        return path;
    }


}
