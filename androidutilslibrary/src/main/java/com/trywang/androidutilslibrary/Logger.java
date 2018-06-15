package com.trywang.androidutilslibrary;


import android.annotation.SuppressLint;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Stack;

/**
 * log工具
 *
 * @author Try
 * @date 2017/11/6 14:36
 */
public class Logger {
    private static final int METHOD_COUNT = 2;
    private static final char METHOD_SEPARATOR = '\n';


    /**
     * v/verbose：用以打印非常详细的日志，例如如果你需要打印网络请求及返回的数据。
     */
    public static final int VERBOSE = Log.VERBOSE;

    /**
     * d/debug：用以打印便于调试的日志，例如网络请求返回的关键结果或者操作是否成功。
     */
    public static final int DEBUG = Log.DEBUG;

    /**
     * i/information：用以打印为以后调试或者运行中提供运行信息的日志，例如进入或退出了某个函数、进入了函数的某个分支等。
     */
    public static final int INFO = Log.INFO;

    /**
     * w/warning：用以打印不太正常但是还不是错误的日志。
     */
    public static final int WARN = Log.WARN;

    /**
     * e/error：用以打印出现错误的日志，一般用以表示错误导致功能无法继续运行。
     */
    public static final int ERROR = Log.ERROR;

    public static final String TAG_DEFAULT = "SunmiLogger";

    /**
     * 当前的输出等级，当n>=LEVEL时才会输出<br>
     * 如果当前为调试模式，默认应该设置为i/info<br>
     * 如果想完全禁用输出，如发布，则应该设置为Integer.MAX_VALUE<br>
     * 默认为完全禁用
     */
    private static int LEVEL = VERBOSE;

    /**
     * 初始化logger工具
     * @param isDebug 是否是debug打印
     */
    public static final void isDebug(boolean isDebug){
        LEVEL = isDebug ? VERBOSE : Integer.MAX_VALUE;
    }

    /**
     * 检查当前是否需要输出对应的level。
     * <p/>
     * 如果直接输出字符串，则无需检查。如Log.d("test", "this is a error");
     */
    public static boolean isLoggable(int level) {
        return level >= LEVEL;
    }

    /**
     * v/verbose：用以打印非常详细的日志，例如如果你需要打印网络请求及返回的数据。
     */
    public static void v(String tag, String log) {
        if (LEVEL <= VERBOSE) {
            Log.v(tag, getLogInfo(log));
        }
    }

    /**
     * v/verbose：用以打印非常详细的日志，例如如果你需要打印网络请求及返回的数据。
     * <p/>
     * 附带具体的Exception，一般必须带有tag。不允许在默认的tag中输出Exception
     */
    public static void v(String tag, String log, Throwable e) {
        if (LEVEL <= VERBOSE) {
            Log.v(tag, getLogInfo(log), e);
        }
    }

    public static void v(String tag, String format, Object... args) {
        v(tag, String.format(format, args));
    }

    public static void v(String tag, JSONObject json) {
        try {
            v(tag, json.toString(2));
        } catch (JSONException e) {
            v(tag, json.toString());
        }
    }

    /**
     * v/verbose：用以打印非常详细的日志，例如如果你需要打印网络请求及返回的数据。
     */
    public static void v(String log) {
        v(TAG_DEFAULT, log);
    }

    /**
     * d/debug：用以打印便于调试的日志，例如网络请求返回的关键结果或者操作是否成功。
     */
    public static void d(String tag, String log) {
        if (LEVEL <= DEBUG) {
            Log.d(tag, getLogInfo(log));
        }
    }

    /**
     * d/debug：用以打印便于调试的日志，例如网络请求返回的关键结果或者操作是否成功。
     * <p/>
     * 附带具体的Exception，一般必须带有tag。不允许在默认的tag中输出Exception
     */
    public static void d(String tag, String log, Throwable e) {
        if (LEVEL <= DEBUG) {
            Log.d(tag, getLogInfo(log), e);
        }
    }

    public static void d(String tag, String format, Object... args) {
        d(tag, String.format(format, args));
    }

    public static void d(String tag, JSONObject json) {
        try {
            d(tag, json.toString(2));
        } catch (JSONException e) {
            d(tag, json.toString());
        }
    }

    /**
     * d/debug：用以打印便于调试的日志，例如网络请求返回的关键结果或者操作是否成功。
     */
    public static void d(String log) {
        d(TAG_DEFAULT, log);
    }

    /**
     * i/information：用以打印为以后调试或者运行中提供运行信息的日志，例如进入或退出了某个函数、进入了函数的某个分支等。
     */
    public static void i(String tag, String log) {
        if (LEVEL <= INFO) {
            Log.i(tag, getLogInfo(log));
        }
    }

    /**
     * i/information：用以打印为以后调试或者运行中提供运行信息的日志，例如进入或退出了某个函数、进入了函数的某个分支等。
     * <p/>
     * 附带具体的Exception，一般必须带有tag。不允许在默认的tag中输出Exception
     */
    public static void i(String tag, String log, Throwable e) {
        if (LEVEL <= INFO) {
            Log.i(tag, getLogInfo(log), e);
        }
    }

    public static void i(String tag, String format, Object... args) {
        i(tag, String.format(format, args));
    }

    public static void i(String tag, JSONObject json) {
        try {
            i(tag, json.toString(2));
        } catch (JSONException e) {
            i(tag, json.toString());
        }
    }

    /**
     * i/information：用以打印为以后调试或者运行中提供运行信息的日志，例如进入或退出了某个函数、进入了函数的某个分支等。
     */
    public static void i(String log) {
        i(TAG_DEFAULT, log);
    }

    /**
     * w/warning：用以打印不太正常但是还不是错误的日志。
     */
    public static void w(String tag, String log) {
        if (LEVEL <= WARN) {
            Log.w(tag, getLogInfo(log));
        }
    }

    /**
     * w/warning：用以打印不太正常但是还不是错误的日志。
     * <p/>
     * 附带具体的Exception，一般必须带有tag。不允许在默认的tag中输出Exception
     */
    public static void w(String tag, String log, Throwable e) {
        if (LEVEL <= WARN) {
            Log.w(tag, getLogInfo(log), e);
        }
    }

    public static void w(String tag, String format, Object... args) {
        w(tag, String.format(format, args));
    }

    public static void w(String tag, JSONObject json) {
        try {
            w(tag, json.toString(2));
        } catch (JSONException e) {
            w(tag, json.toString());
        }
    }

    /**
     * w/warning：用以打印不太正常但是还不是错误的日志。
     */
    public static void w(String log) {
        w(TAG_DEFAULT, log);
    }

    /**
     * e/error：用以打印出现错误的日志，一般用以表示错误导致功能无法继续运行。
     */
    public static void e(String tag, String log) {
        if (LEVEL <= ERROR) {
            Log.e(tag, getLogInfo(log));
        }
    }

    /**
     * e/error：用以打印出现错误的日志，一般用以表示错误导致功能无法继续运行。
     * <p/>
     * 附带具体的Exception，一般必须带有tag。不允许在默认的tag中输出Exception
     */
    public static void e(String tag, String log, Throwable e) {
        if (LEVEL <= ERROR) {
            Log.e(tag, getLogInfo(log), e);
        }
    }

    public static void e(String tag, Throwable e) {
        e(tag, Log.getStackTraceString(e));
    }

    public static void e(String tag, String format, Object... args) {
        e(tag, String.format(format, args));
    }

    public static void e(String tag, JSONObject json) {
        try {
            e(tag, json.toString(2));
        } catch (JSONException e) {
            e(tag, json.toString());
        }
    }

    public static void e(String log) {
        e(TAG_DEFAULT, log);
    }

    @SuppressLint("DefaultLocale")
    public static String getLogInfo(String log) {
        try {
            // 找到第一个非Logger的类
            StackTraceElement[] elements = new Exception().getStackTrace();
            int lastMethodIndex = 0;
            while (lastMethodIndex < elements.length &&  Logger.class.getName().equals(elements[lastMethodIndex].getClassName())) {
                lastMethodIndex++;
            }

            // 未找到，或在Logger中log
            if (lastMethodIndex >= elements.length) {
                return log;
            }

            // 输出指定数量的method
            // 忽略ZygoteInit.java和Method.java
            // com.android.internal.os.ZygoteInit
            Stack<String> stringStack = new Stack<>();
            for (int i = lastMethodIndex; i < elements.length && stringStack.size() < METHOD_COUNT; i++) {
                StackTraceElement element = elements[i];
                if (!Method.class.getName().equals(element.getClassName())) {
                    stringStack.push(String.format("[.%s(%s:%d)]%c",
                            element.getMethodName(),
                            element.getFileName(),
                            element.getLineNumber(),
                            METHOD_SEPARATOR));
                }
            }

            // 创建sb
            StringBuilder sb = new StringBuilder();
            sb.append(" ").append(METHOD_SEPARATOR);
            while (!stringStack.isEmpty()) {
                sb.append(stringStack.pop());
            }

            // 输出log
            return sb.append(log).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return log;
    }

}