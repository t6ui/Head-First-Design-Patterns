package com.wingtech.logupload.timetask;

import android.content.Context;
import android.os.Handler;

import com.wingtech.logupload.LogCollectSettingActivity;
import com.wingtech.logupload.service.LogCollectService;
import com.wingtech.logupload.utils.Utils;
import com.wingtech.logupload.utils.WTLogger;

public class LowRamScanTimer extends Timer {
    public static final String ACTION_LOW_RAM_SCAN_TIMEOUT = "com.wingtech.upload.LOW_RAM_SCAN_TIMEOUT";
    private static final String TAG = "LowRamScanTimer";
    private static LowRamScanTimer timer = null;

    private LowRamScanTimer(Context context, LogCollectService.LogCollectHandler handler) {
        super(context, handler);
    }

    public static synchronized LowRamScanTimer getInstance(Context context,
                                                           LogCollectService.LogCollectHandler handler) {
        if (null == timer) {
            timer = new LowRamScanTimer(context, handler);
        }
        return timer;
    }

    public void handleTimeOut(Handler handler) {
        WTLogger.v(TAG, "handleTimeOut");
        if (handler != null) {
            handler.sendEmptyMessage(LogCollectService.MSG_LOW_RAM_SCAN);
        }
    }

    public void resetPeriodTimer(long timeout) {
        long currentTime = System.currentTimeMillis();
        if (timeout == -1) {
            int lowRAMScanTime = Integer.parseInt(spUtils.getString(LogCollectSettingActivity.KEY_LOWMEMORY_SCAN_INTERVAL,
                    LogCollectSettingActivity.DEFAULT_LOWMEMORY_SCAN_INTERVAL));
            WTLogger.v(TAG, "RAM scan period is " + lowRAMScanTime + "min");

            timeout = lowRAMScanTime * Utils.MILLIS_PER_MINUTE + currentTime;
            //timeout = lowRAMScanTime * Utils.MILLIS_PER_SECOND + currentTime;//for test

            Utils.setAlarm(mContext, ACTION_LOW_RAM_SCAN_TIMEOUT, LogCollectService.class, timeout);

        } else if (currentTime >= timeout) {
            handleTimeOut(mHandler);
        } else {
            Utils.setAlarm(mContext, ACTION_LOW_RAM_SCAN_TIMEOUT, LogCollectService.class, timeout);
        }
    }

    public synchronized void cancelTimer() {
        Utils.cancelAlarm(mContext, ACTION_LOW_RAM_SCAN_TIMEOUT, LogCollectService.class);
    }

}
