package com.wingtech.logupload.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharePreferenceUtils {
    public static final String SOFT_VERSION = "soft_version";
    public static final String RECORD_LAST_REALTIME = "record_last_realtime";
    public static final String LAST_REALTIME = "last_realtime";
    public static final String LAST_UPLOAD_REALTIME = "last_upload_realtime";
    public static final String CURRENT_REALTIME = "current_realtime";
    public static final String LOWRAM_COUNT = "lowRAM_Count";
    public static final String UPLOAD_PERIOD_TIMEOUT = "upload_period_timeout";
    public static final String UPLOAD_LOG_WHEN_CONNECT_WIFI = "upload_log_when_connect_wifi";
    public static final String UPLOAD_REPORT_WHEN_CONNECT_NETWORK = "upload_report_when_connect_network";
    private static SharedPreferences sp;
    private static SharePreferenceUtils utils;
    private static String PREF_SETTINGS = "settings";

    public static SharePreferenceUtils newInstance(Context context) {
        if (sp == null) {
            synchronized (SharePreferenceUtils.class) {
                sp = PreferenceManager.getDefaultSharedPreferences(context);
            }
        }
        if (utils == null) {
            synchronized (SharePreferenceUtils.class) {
                utils = new SharePreferenceUtils();
            }
        }
        return utils;
    }

    public static SharePreferenceUtils newInstance(Context context,
                                                   String spName) {
        if (sp == null) {
            synchronized (SharePreferenceUtils.class) {
                sp = context.getSharedPreferences(spName,
                        Context.MODE_PRIVATE);
            }
        }
        if (utils == null) {
            synchronized (SharePreferenceUtils.class) {
                utils = new SharePreferenceUtils();
            }
        }
        return utils;
    }

    public void setString(String key, String value) {
        sp.edit().putString(key, value).commit();
    }

    public String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public void setInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public void setBoolean(String key, boolean value) {
        sp.edit().putBoolean(key, value).commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public void setLong(String key, long value) {
        sp.edit().putLong(key, value).apply();
    }

    public long getLong(String key, long defValue) {
        return sp.getLong(key, defValue);
    }
}
