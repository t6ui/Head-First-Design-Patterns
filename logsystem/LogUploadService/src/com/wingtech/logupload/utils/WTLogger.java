package com.wingtech.logupload.utils;

import android.util.Log;

public class WTLogger {
    public static final String APP_TAG = "logupload";
    public static final boolean DEBUG_V = true;
    public static final boolean DEBUG_I = true;
    public static final boolean DEBUG_D = true;
    public static final boolean DEBUG_E = true;
    public static final boolean DEBUG_W = true;
    private static final String TZH_TAG = "wtlog-";

    public WTLogger() {
    }

//    public static void v(String message) {
//        if (DEBUG_V) {
//            Log.v(APP_TAG, message);
//        }
//    }
//
//    public static void i(String message) {
//        if (DEBUG_I) {
//            Log.i(APP_TAG, message);
//        }
//    }
//
//    public static void d(String message) {
//        if (DEBUG_D) {
//            Log.d(APP_TAG, message);
//        }
//    }
//
//    public static void e(String message) {
//        if (DEBUG_E) {
//            Log.e(APP_TAG, message);
//        }
//    }
//
//    public static void e(String message, Exception ex) {
//        if (DEBUG_E) {
//            Log.e(APP_TAG, message, ex);
//        }
//    }
//
//    public static void w(String message) {
//        if (DEBUG_W) {
//            Log.w(APP_TAG, message);
//        }
//    }

    public static void v(String tag, String message) {
        if (DEBUG_V) {
            Log.v(TZH_TAG + tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (DEBUG_I) {
            Log.i(TZH_TAG + tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG_D) {
            Log.d(TZH_TAG + tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (DEBUG_E) {
            Log.e(TZH_TAG + tag, message);
        }
    }

    public static void e(String tag, String message, Exception ex) {
        if (DEBUG_E) {
            Log.e(TZH_TAG + tag, message, ex);
        }
    }

    public static void w(String tag, String message) {
        if (DEBUG_W) {
            Log.w(TZH_TAG + tag, message);
        }
    }

    public static void v(String tag, String prefix, String message) {
        if (DEBUG_V) {
            Log.v(TZH_TAG + tag, prefix + message);
        }
    }

    public static void i(String tag, String prefix, String message) {
        if (DEBUG_I) {
            Log.i(TZH_TAG + tag, prefix + message);
        }
    }

    public static void d(String tag, String prefix, String message) {
        if (DEBUG_D) {
            Log.d(TZH_TAG + tag, prefix + message);
        }
    }

    public static void e(String tag, String prefix, String message) {
        if (DEBUG_E) {
            Log.e(TZH_TAG + tag, prefix + message);
        }
    }

    public static void e(String tag, String prefix, String message, Exception ex) {
        if (DEBUG_E) {
            Log.e(TZH_TAG + tag, prefix + message, ex);
        }
    }

    public static void w(String tag, String prefix, String message) {
        if (DEBUG_W) {
            Log.w(TZH_TAG + tag, prefix + message);
        }
    }
}
