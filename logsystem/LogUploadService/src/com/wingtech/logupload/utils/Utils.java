package com.wingtech.logupload.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

import com.mediatek.common.featureoption.FeatureOption;
import com.wingtech.logupload.model.UploadInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class Utils {

    public static final String TAG = "LogUploadUtils";

    public static final int MILLIS_PER_SECOND = 1000;
    public static final int MILLIS_PER_MINUTE = 60 * 1000;
    public static final int MILLIS_PER_HOUR = 60 * 60 * 1000;
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";

    public static String longTimeToString() {
        SimpleDateFormat mSDF = new SimpleDateFormat("yyyyMMddhhmmss");
        return mSDF.format(new Date());
    }

    public static boolean checkSoftVersionChanged(Context context) {
        String currentVersion = Utils.getSoftVersion();
        String savedVersion = SharePreferenceUtils.newInstance(context).getString(
                SharePreferenceUtils.SOFT_VERSION, "");

        if (savedVersion != null && !savedVersion.equals(currentVersion)) {
            return true;
        }
        return false;
    }

    public static void initDataBase(Context context) {
        if (Utils.checkSoftVersionChanged(context)) {
            SharePreferenceUtils spUtils = SharePreferenceUtils.newInstance(context);
            WTLogger.d(TAG, "soft version change");
            spUtils.setLong(SharePreferenceUtils.CURRENT_REALTIME, 0);
            spUtils.setLong(SharePreferenceUtils.LAST_REALTIME, 0);
            spUtils.setLong(SharePreferenceUtils.LAST_UPLOAD_REALTIME, 0);
            spUtils.setInt(SharePreferenceUtils.LOWRAM_COUNT, 0);
            spUtils.setLong(SharePreferenceUtils.UPLOAD_PERIOD_TIMEOUT, -1);
            String newVersion = Utils.getSoftVersion();
            spUtils.setString(SharePreferenceUtils.SOFT_VERSION, newVersion);
        }

        UploadInfoHelper uploadInfoHelper = UploadInfoHelper.getInstance(context);
        UploadInfo statusInfo = uploadInfoHelper.getStatusInfo();
        if (statusInfo == null) {
            WTLogger.d(TAG, "init statusInfo info in database");
            statusInfo = new UploadInfo();
            Uri uri = uploadInfoHelper.addUploadInfo(statusInfo);
            int reportInfoId = Integer.parseInt(uri.getLastPathSegment());
            statusInfo.setId(reportInfoId);
            statusInfo.setUploadType("auto-status");
            statusInfo.setSoftVersion(Utils.getSoftVersion());
            statusInfo.setElapsedRealTime(0L);
            statusInfo.setLowRAMCount(0);
            statusInfo.setZipTime(Utils.longTimeToString());
            statusInfo.setUploadStatus(0);
            uploadInfoHelper.updateUploadInfo(statusInfo);
        }
    }

    public static String longTimeToString(long time) {
        Date date = new Date(time);
        return new SimpleDateFormat(TIME_FORMAT, Locale.US).format(date);
    }

    public static boolean isRunningService(Context context, String value) {
        Iterator<RunningServiceInfo> iterator = ((ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE))
                .getRunningServices(500).iterator();
        String runningServiceName;
        do {
            if (!iterator.hasNext()) {
                return false;
            }
            runningServiceName = ((ActivityManager.RunningServiceInfo) iterator
                    .next()).service.getClassName().toString();
        } while (!value.equals(runningServiceName));
        Log.e(TAG, value + " is running");
        return true;
    }

    public static void setAlarm(Context context, String action,
                                Class<?> className, long time) {
        Intent intent = new Intent(action);
        intent.setClass(context, className);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //alarm.setExact(AlarmManager.RTC, time, intent);
        alarm.set(AlarmManager.RTC, time, pendingIntent);
    }

    public static void setRepeatingAlarm(Context context, String action,
                                         Class<?> className, long waitTime) {
        Intent intent = new Intent(action);
        intent.setClass(context, className);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long time = System.currentTimeMillis() + waitTime;
        AlarmManager alarm = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC, time, waitTime, pi);
    }

    public static void cancelAlarm(Context context, String action, Class<?> className) {
        Intent intent = new Intent(action);
        intent.setClass(context, className);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pi != null) {
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pi);
            pi.cancel();
            Log.e(TAG, "Alarm is running, cancel alarm!");
        }
    }

    public static String getProductName() {
        String product = SystemProperties.get("ro.build.product", "");
        if (product == null || product.equals("")) {
            product = Build.PRODUCT;
        }
        return product;
    }


    public static String getSoftVersion() {
        String version = SystemProperties.get("ro.build.display.wtid", "");
        if (version == null || version.equals("")) {
            version = FeatureOption.WT_INNER_VERSION;
        }
        if (version == null || version.equals("")) {
            version = Build.DISPLAY;
        }
        return version;
    }

    public static String getHardwareVersion() {
        String version = FeatureOption.WT_HARDWARE_VERSION;
        if (version == null || version.equals("")) {
            version = SystemProperties.get("ro.hw_version", "");
        }
        if (version == null || version.equals("")) {
            version = SystemProperties.get("ro.product.wt.boardid", "");
        }
        if (version == null || version.equals("")) {
            version = Build.HARDWARE;
        }
        return version;
    }


    public static String getSerialno() {
        return SystemProperties.get("ro.serialno", "");
    }

}
