package com.trywang.androidutilslibrary.photo;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.File;

/**
 * 拍照相册裁剪工具类 也可以试用PhotoManager代替试用
 *
 * @author Try
 * @date 2018/6/22 15:22
 */
public class TakePhotoUtils {
    public static final String AUTHORITIES = "com.trywang.widgetall.fileProvider";
    public static final int REQUEST_CODE_ALBUM = 1000;
    public static final int REQUEST_CODE_TAKE_PHOTO = 1001;
    public static final int REQUEST_CODE_CLIP = 1002;
    public static final int REQUEST_CODE_PERMISSION_CAMERA = 1003;

    /**
     * 打开相册
     *
     * @param activity activity
     */
    public static void openAlbum(Activity activity) {
        if (activity == null) {
            return;
        }
//        Intent intent = new Intent(Intent.ACTION_PICK);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        //临时授权
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, REQUEST_CODE_ALBUM);
    }

    /**
     * 拍照
     * 如果创建.png后缀的图片在牌照完成之后传递到裁剪相册时有的手机会把图片逆时针旋转90度
     * 比如：三星，但是小米5x不会
     *
     * @param activity activity
     * @return
     */
    public static File takePhoto(Activity activity) {
        File file = BitmapPathUtils.createTakePhotoFile(activity);
        return takePhoto(activity, file);
    }

    /**
     * 裁剪图片
     *
     * @param activity  activity
     * @param originUri originUri
     */
    public static void clip(Activity activity, Uri originUri) {
        try {
            File clipFile = BitmapPathUtils.createTempFile(activity.getExternalCacheDir(), "cat.jpg");
            Uri outputUri = BitmapPathUtils.createUri(activity, AUTHORITIES, clipFile);
            clip(activity, originUri, outputUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean onRequestPermissionsResult(Activity activity, int requestCode,
                                                     @NonNull String[] permissions,
                                                     @NonNull int[] grantResults,
                                                     IPermissionGrantCallback grantCallback) {
        if (requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (grantCallback != null) {
                    grantCallback.permissionGranted();
                } else {
                    Toast.makeText(activity, "已权限使用相机！", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, "没有权限使用相机！", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        return false;
    }

    /**
     * 检查权限打开相机
     * @param activity activity
     * @return 图片文件
     */
    public static File takePhotoWithPermis(Activity activity) {
        int permissionCamera = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int permissionWES = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        if (permissionCamera != PackageManager.PERMISSION_GRANTED
                || permissionWES != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CODE_PERMISSION_CAMERA);
        } else {
            return takePhoto(activity);
        }
        return null;
    }

    public static File takePhoto(Activity activity, File file) {
        if (file == null) {
            return null;
        }
        Uri fileUri = BitmapPathUtils.createUri(activity, AUTHORITIES, file); // 先创建一个临时文件
        return takePhoto(activity, fileUri);
    }

    public static File takePhoto(Activity activity, Uri uri) {
        if (uri == null) {
            return null;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        //安全调用
        ComponentName componentName = intent.resolveActivity(activity.getPackageManager());
        //安全调用
        if (componentName != null) {
            // 授予目录临时共享权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
        } else {
            Toast.makeText(activity, "没有相机应用！", Toast.LENGTH_SHORT).show();
        }

        String path = BitmapPathUtils.getPath(activity, uri);

        if (path != null && !"".equalsIgnoreCase(path)) {
            return new File(path);
        }
        return null;
    }



    /**
     * 裁剪图片
     *
     * @param activity   activity
     * @param originUri  originUri
     * @param outputFile outputFile
     */
    public static void clip(Activity activity, Uri originUri, File outputFile) {
        try {
            Uri outputUri = BitmapPathUtils.createUri(activity, AUTHORITIES, outputFile);
            clip(activity, originUri, outputUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 裁剪 照片
     *
     * @param activity  activity
     * @param originUri originUri
     * @param outputUri outputUri
     */
    public static void clip(Activity activity, Uri originUri, Uri outputUri) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            // 授予uri目录临时共享权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(originUri, "image/*");
            intent.putExtra("scale", true);
            intent.putExtra("outputX", 300);
            intent.putExtra("outputY", 300);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            //return-data为true时，直接返回bitmap，可能会很占内存，不建议，小米等个别机型会出异常！！！
            //所以适配小米等个别机型，裁切后的图片，不能直接使用data返回，应使用uri指向
            //裁切后保存的URI，不属于我们向外共享的，所以可以使用fill://类型的URI
//            Uri outputUri = Uri.fromFile(outputFile);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            intent.putExtra("return-data", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true);

            ResolveInfo info = activity.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (info == null) {
                Toast.makeText(activity, "没有裁剪图片的软件！", Toast.LENGTH_SHORT).show();
                return;
            }
            //允许裁剪软件对outputUri此Uri进行读写
            activity.grantUriPermission(info.activityInfo.packageName, outputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.grantUriPermission(info.activityInfo.packageName, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            activity.startActivityForResult(intent, REQUEST_CODE_CLIP);// 启动裁剪程序
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    interface IPermissionGrantCallback {
        void permissionGranted();
    }
}
