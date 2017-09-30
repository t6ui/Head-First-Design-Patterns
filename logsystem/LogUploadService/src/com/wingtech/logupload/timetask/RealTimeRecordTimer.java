package com.wingtech.logupload.timetask;

import android.content.Context;
import android.os.Handler;

import com.wingtech.logupload.LogCollectSettingActivity;
import com.wingtech.logupload.service.LogCollectService;
import com.wingtech.logupload.utils.Utils;
import com.wingtech.logupload.utils.WTLogger;

public class RealTimeRecordTimer extends Timer {
    public static final String ACTION_REAL_TIME_TIMEOUT = "com.wingtech.upload.REAL_TIME_TIMEOUT";
    private static final String TAG = "RealTimeRecordTimer";
    private static RealTimeRecordTimer timer = null;

    private RealTimeRecordTimer(Context context, LogCollectService.LogCollectHandler handler) {
        super(context, handler);
    }

    public static synchronized RealTimeRecordTimer getInstance(Context context,
                                                               LogCollectService.LogCollectHandler handler) {
        if (null == timer) {
            timer = new RealTimeRecordTimer(context, handler);
        }
        return timer;
    }

    public void handleTimeOut(Handler handler) {
        WTLogger.v(TAG, "handleTimeOut");
        startTimer();
        if (handler != null) {
            handler.sendEmptyMessage(LogCollectService.MSG_REAL_TIME_RECORD);
        }
    }

    public void resetPeriodTimer(long timeout) {
        long currentTime = System.currentTimeMillis();
        if (timeout == -1) {
            int realTimePeriod = Integer.parseInt(spUtils.getString(LogCollectSettingActivity.KEY_REALTIME_SCAN_INTERVAL,
                    LogCollectSettingActivity.DEFAULT_REALTIME_SCAN_INTERVAL));
            WTLogger.v(TAG, "real-time record period is " + realTimePeriod + "min");

            timeout = realTimePeriod * Utils.MILLIS_PER_MINUTE + currentTime;
            //timeout = realTimePeriod * Utils.MILLIS_PER_SECOND + currentTime;//for test

            Utils.setAlarm(mContext, ACTION_REAL_TIME_TIMEOUT, LogCollectService.class, timeout);

        } else if (currentTime >= timeout) {
            handleTimeOut(mHandler);
        } else {
            Utils.setAlarm(mContext, ACTION_REAL_TIME_TIMEOUT, LogCollectService.class, timeout);
        }
    }

    public synchronized void cancelTimer() {
        Utils.cancelAlarm(mContext, ACTION_REAL_TIME_TIMEOUT, LogCollectService.class);
    }
}
