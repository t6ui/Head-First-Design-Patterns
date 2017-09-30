package com.wingtech.logupload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.wingtech.logupload.service.LogCollectService;
import com.wingtech.logupload.utils.SharePreferenceUtils;
import com.wingtech.logupload.utils.Utils;
import com.wingtech.logupload.utils.WTLogger;

public class ShutdownReceiver extends BroadcastReceiver {
    public static final String TAG = "ShutdownReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        WTLogger.e(TAG, "--ShutdownReceiver--action=" + action);
        if (action.equals(Intent.ACTION_SHUTDOWN)) {
            saveRealTime(context);
            if (Utils.isRunningService(context, "com.wingtech.logupload.service.LogCollectService")) {
                Intent i = new Intent();
                i.setClass(context, LogCollectService.class);
                context.stopService(i);
            }
        }
    }

    private void saveRealTime(Context context) {
        SharePreferenceUtils spUtils = SharePreferenceUtils.newInstance(context);
        long lastRealtime = spUtils.getLong(spUtils.LAST_REALTIME, 0);
        long currentRealtime = SystemClock.elapsedRealtime();
        long recordRealtime = lastRealtime + currentRealtime;
        WTLogger.e(TAG, "lastRealtime=" + lastRealtime + "    currentRealtime=" + currentRealtime
                + "   recordRealtime = " + recordRealtime);
        spUtils.setLong(spUtils.LAST_REALTIME, recordRealtime);
        spUtils.setBoolean(spUtils.RECORD_LAST_REALTIME, true);
    }

}
