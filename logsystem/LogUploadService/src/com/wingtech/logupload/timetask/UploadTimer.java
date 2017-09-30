package com.wingtech.logupload.timetask;

import android.content.Context;
import android.os.Handler;

import com.wingtech.logupload.LogCollectSettingActivity;
import com.wingtech.logupload.service.LogCollectService;
import com.wingtech.logupload.utils.NetworkUtils;
import com.wingtech.logupload.utils.SharePreferenceUtils;
import com.wingtech.logupload.utils.Utils;
import com.wingtech.logupload.utils.WTLogger;

public class UploadTimer extends Timer {
    public static final String ACTION_UPLOAD_TIMEOUT = "com.wingtech.upload.UPLOAD_TIMEOUT";
    private static final String TAG = "UploadTimer";
    private static UploadTimer timer = null;

    private UploadTimer(Context context, LogCollectService.LogCollectHandler handler) {
        super(context, handler);
    }

    public static synchronized UploadTimer getInstance(Context context,
                                                       LogCollectService.LogCollectHandler handler) {
        if (null == timer) {
            timer = new UploadTimer(context, handler);
        }
        return timer;
    }

    public void handleTimeOut(Handler handler) {
        WTLogger.v(TAG, "handleTimeOut");

        startTimer();

        int netWorkType = NetworkUtils.getNetWorkType(mContext);
        if (netWorkType == NetworkUtils.NETWORK_WLAN) {
            spUtils.setBoolean(SharePreferenceUtils.UPLOAD_LOG_WHEN_CONNECT_WIFI, false);
            spUtils.setBoolean(SharePreferenceUtils.UPLOAD_REPORT_WHEN_CONNECT_NETWORK, false);
        } else if (netWorkType == NetworkUtils.NETWORK_MOBILE) {
            spUtils.setBoolean(SharePreferenceUtils.UPLOAD_REPORT_WHEN_CONNECT_NETWORK, false);
        } else {
            WTLogger.e(TAG, "current network is invalid, not to collect log");
            return;
        }

        if (handler == null) {
            WTLogger.e(TAG, "handle time out error: handler is null");
            return;
        }
        handler.sendEmptyMessage(LogCollectService.MSG_UPLOAD_INFO_PREPARE);
    }

    public void resetPeriodTimer(long timeout) {
        long currentTime = System.currentTimeMillis();
        if (timeout == -1) {
            int uploadPeriod = Integer.parseInt(spUtils.getString(LogCollectSettingActivity.KEY_UPLOAD_INTERVAL,
                    LogCollectSettingActivity.DEFAULT_UPLOAD_INTERVAL));
            timeout = uploadPeriod * Utils.MILLIS_PER_HOUR + currentTime;
//            timeout = uploadPeriod * Utils.MILLIS_PER_MINUTE + currentTime;//for debug
            WTLogger.d(TAG, "Upload period is " + uploadPeriod + "h");

            spUtils.setLong(SharePreferenceUtils.UPLOAD_PERIOD_TIMEOUT, timeout);

            Utils.setAlarm(mContext, ACTION_UPLOAD_TIMEOUT, LogCollectService.class, timeout);
        } else if (currentTime >= timeout) {
            handleTimeOut(mHandler);
        } else {
            Utils.setAlarm(mContext, ACTION_UPLOAD_TIMEOUT, LogCollectService.class, timeout);
        }
    }

    public synchronized void cancelTimer() {
        Utils.cancelAlarm(mContext, ACTION_UPLOAD_TIMEOUT, LogCollectService.class);
    }

}
