package com.trywang.androidutilslibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;

/**
 * activity相关
 *
 * @author Try
 * @date 2017/11/3 16:47
 */
public class ActivityUtils {

    /**
     * 检查activity是否destroyed
     *
     * @param activity activity
     * @return if destroyed return true
     */
    @SuppressLint("NewApi")
    public static boolean activityIsDestroyed(Activity activity) {
        if (activity == null) {
            return true;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isFinishing()) {
            return true;
        } else if (activity.isDestroyed() || activity.isFinishing()) {
            return true;
        }
        return false;
    }

}
