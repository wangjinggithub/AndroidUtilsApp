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
import java.lang.ref.WeakReference;

/**
 * 拍照相册 选取图片管理类
 *
 * @author Try
 * @date 2018/6/25 15:03
 */
public class PhotoManager {
    public static final int REQUEST_CODE_ALBUM = 1000;
    public static final int REQUEST_CODE_TAKE_PHOTO = 1001;
    public static final int REQUEST_CODE_CLIP = 1002;
    public static final int REQUEST_CODE_PERMISSION_CAMERA = 1003;

    private File mFile;
    private Uri mUri;
    private Uri mUriOrigin;//需要读取的图片源Uir
    private IPermissionGrantCallback mPermissionGrant;
    private IPermissionDenyCallback mPermissionDeny;
    private boolean isCheckPermission;//是否检查拍照等权限
    private String mAuthority;
    WeakReference<Activity> mContext;

    public PhotoManager(Activity activity,String authority) {
        mContext = new WeakReference<>(activity);
        this.mAuthority = authority;
    }

    public PhotoManager setPath(String path) {
        this.mFile = new File(path);
        return this;
    }

    /**
     * 如果使用此方法切记一定要设置mAuthority
     *
     * @param file file
     * @return this
     */
    public PhotoManager setFile(File file) {
        this.mFile = file;
        return this;
    }

    public PhotoManager setUri(Uri uri) {
        this.mUri = uri;
        return this;
    }

    public PhotoManager setPermissionGrantCallback(IPermissionGrantCallback callback) {
        this.mPermissionGrant = callback;
        return this;
    }

    public PhotoManager setPermissionDenyCallback(IPermissionDenyCallback callback) {
        this.mPermissionDeny = callback;
        return this;
    }

    public PhotoManager setCheckPermission(boolean check) {
        this.isCheckPermission = check;
        return this;
    }

    /**
     * 如果使用默认的file 或者path则必须设置此参数
     *
     * @param authority authority FileProvider
     * @return this
     */
    public PhotoManager setAuthority(String authority) {
        this.mAuthority = authority;
        return this;
    }

    /**
     * 打开相册
     */
    public void openAlbum() {
        //Intent intent = new Intent(Intent.ACTION_PICK);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        //临时授权
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        mContext.get().startActivityForResult(intent, REQUEST_CODE_ALBUM);
    }

    /**
     * 拍照
     *
     * @return 照片存储文件
     */
    public File takePhoto() {
        if (this.isCheckPermission) {
            int permissionCamera = ContextCompat.checkSelfPermission(mContext.get(), Manifest.permission.CAMERA);
            int permissionWES = ContextCompat.checkSelfPermission(mContext.get(), Manifest.permission.CAMERA);
            if (permissionCamera != PackageManager.PERMISSION_GRANTED
                    || permissionWES != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mContext.get(), new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CODE_PERMISSION_CAMERA);
                return null;
            } else {
                return takePhotoCheckFile();
            }
        } else {
            return takePhotoCheckFile();
        }
    }

    /**
     * 裁剪图片
     *
     * @return 输出文件
     */
    public File clip() {
        try {
            if (mUri == null) {
                throw new NullPointerException("origin mUri is null");
            }
            if (mFile == null) {
                mFile = createClipFile();
            }

            if (mAuthority == null || "".equalsIgnoreCase(mAuthority)) {
                throw new NullPointerException("mAuthority is null");
            }

            Uri outputUri = BitmapPathUtils.createUri(mContext.get(), mAuthority, mFile);
            return clip(outputUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 裁剪 照片
     *
     * @param outputUri outputUri
     * @return 输出文件
     */
    private File clip(Uri outputUri) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            // 授予uri目录临时共享权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(mUri, "image/*");
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

            ResolveInfo info = mContext.get().getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (info == null) {
                Toast.makeText(mContext.get(), "没有裁剪图片的软件！", Toast.LENGTH_SHORT).show();
                return null;
            }
            //允许裁剪软件对outputUri此Uri进行读写
            mContext.get().grantUriPermission(info.activityInfo.packageName, outputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mContext.get().grantUriPermission(info.activityInfo.packageName, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            mContext.get().startActivityForResult(intent, REQUEST_CODE_CLIP);// 启动裁剪程序
            mUri = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mFile;
    }


    public boolean onRequestPermissionsResult(int requestCode,
                                              @NonNull String[] permissions,
                                              @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && mPermissionGrant != null) {
                mPermissionGrant.permissionGranted();
            } else {
                if (mPermissionDeny != null) {
                    mPermissionDeny.permissionDenied();
                } else {
                    Toast.makeText(mContext.get(), "未授权使用相机！", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
        return false;
    }


    private File takePhotoCheckFile() {
        if (mUri != null) {
            String path = BitmapPathUtils.getPath(mContext.get(), mUri);
            if (path != null && !"".equalsIgnoreCase(path)) {
                this.mFile = new File(path);
            } else {
                this.mFile = null;
            }
        } else {
            if (mAuthority == null || "".equalsIgnoreCase(mAuthority)) {
                throw new NullPointerException("mAuthority is null!");
            }
            if (mFile == null) {
                mFile = createTakePhotoFile();
            }
            mUri = BitmapPathUtils.createUri(mContext.get(), mAuthority, mFile);
        }
        return takePhotoReal();
    }

    private File takePhotoReal() {
        try {
            if (mUri == null) {
                return null;
            }
            Activity activity = mContext.get();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            //安全调用
            ComponentName componentName = intent.resolveActivity(activity.getPackageManager());
            //安全调用
            if (componentName != null) {
                // 授予目录临时共享权限
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                activity.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
                mUri = null;
            } else {
                Toast.makeText(activity, "没有相机应用！", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mFile;
    }

    private File createTakePhotoFile() {
        String fileName = "take_photo_";
        fileName += String.valueOf(("try_image_" + System.currentTimeMillis()).hashCode());
        fileName += ".jpg";
        return BitmapPathUtils.createTempFile(mContext.get().getExternalCacheDir(), fileName);
    }

    private File createClipFile() {
        String fileName = "clip_";
        fileName += String.valueOf(("try_image_" + System.currentTimeMillis()).hashCode());
        fileName += ".jpg";
        return BitmapPathUtils.createTempFile(mContext.get().getExternalCacheDir(), fileName);
    }

    interface IPermissionGrantCallback {
        void permissionGranted();
    }

    interface IPermissionDenyCallback {
        void permissionDenied();
    }
}
