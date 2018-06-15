package com.trywang.androidutilslibrary;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * 包相关的工具类
 *
 * @author Try
 * @date 2018/5/16 16:41
 */
public class PackageUtils {

    /**
     * 获取packageInfo
     * @param context context
     * @return packageInfo
     */
    public static PackageInfo getPackageInfo(Context context) {
        if (context == null) {
            return null;
        }

        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * 获取版本code
     * @param context context
     * @return versionCode
     */
    public static int getVersionCode(Context context){
        PackageInfo info = getPackageInfo(context);
        return info != null ? info.versionCode : 0;
    }

    /**
     * 获取versionName
     * @param context context
     * @return versionName
     */
    public static String getVersionName(Context context){
        PackageInfo info = getPackageInfo(context);
        return info != null ? info.versionName : "0";
    }
}
