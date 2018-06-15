package com.trywang.androidutilslibrary;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;

/**
 * 视图的工具类
 *
 * @author Try
 * @date 2017/11/4 17:39
 */
public class ViewUtils {

    /**
     * dp to sp
     * @param context context
     * @param dpVal dp value
     * @return sp value
     */
    public static int dp2sp(Context context, float dpVal) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics()));
    }

    /**
     * sp to dp
     * @param context context
     * @param spVal sp value
     * @return dp value
     */
    public static int sp2dp(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

    /**
     * dp to px
     * @param context context
     * @param dpValue dp value
     * @return px value
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px to dp
     * @param context context
     * @param pxValue px value
     * @return dp value
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * measure view width and height
     *
     * @param view target view
     * @return int[0]:view width; int[1] view height
     */
    public static int[] measureView(View view) {
        if (view == null) {
            return null;
        }

        int[] widthAndHeight = new int[2];
        int width = View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
        widthAndHeight[0] = view.getMeasuredWidth();
        widthAndHeight[1] = view.getMeasuredHeight();
        return widthAndHeight;
    }

    /**
     * get the state real-height
     * @param context context
     * @return state real-height , if error return -1
     */
    public static int getStateHeight(Context context) {
        int statusBarHeight = -1;
        if (context == null) {
            return statusBarHeight;
        }
        //get status_bar_height resourceID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

}
