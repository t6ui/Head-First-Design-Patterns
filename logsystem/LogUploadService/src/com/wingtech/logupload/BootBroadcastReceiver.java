package com.wingtech.logupload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;

import com.wingtech.logupload.model.UploadInfo;
import com.wingtech.logupload.service.LogCollectService;
import com.wingtech.logupload.utils.SharePreferenceUtils;
import com.wingtech.logupload.utils.UploadInfoHelper;
import com.wingtech.logupload.utils.Utils;
import com.wingtech.logupload.utils.WTLogger;

import java.util.List;


public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BootBroadcastReceiver";
    private static SharePreferenceUtils spUtils = null;

    public BootBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            WTLogger.i(TAG, "onReceive " + intent.getAction() + " Receiver: " + this);
            String action = intent.getAction();

            if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                spUtils = SharePreferenceUtils.newInstance(context);
                initData(context);

                boolean logEnable = SystemProperties.getBoolean("persist.sys.pdm.enable", false);
                if (logEnable) {
                    Intent bootIntent = new Intent(context, LogCollectService.class);
                    context.startService(bootIntent);
                }
            } else {
                Log.w(TAG, "onReceive: could not handle: " + intent);
            }
        }
    }

    private void initData(Context context) {
        if (spUtils == null) {
            spUtils = SharePreferenceUtils.newInstance(context);
        }

        UploadInfoHelper uploadInfoHelper = UploadInfoHelper.getInstance(context);
        List<UploadInfo> uploadInfoList = uploadInfoHelper.getUploadInfoList();
        for (UploadInfo info : uploadInfoList) {
            if (info.getUploadStatus() != 0) {
                info.setUploadStatus(0);
                uploadInfoHelper.updateUploadInfo(info);
            }
        }

        Utils.initDataBase(context);

        boolean hasRecordRealtime = spUtils.getBoolean(
                SharePreferenceUtils.RECORD_LAST_REALTIME, false);
        if (!hasRecordRealtime) {
            long lastRealtime = spUtils.getLong(
                    SharePreferenceUtils.LAST_REALTIME, 0);
            long currentRealtime = spUtils.getLong(
                    SharePreferenceUtils.CURRENT_REALTIME, 0);
            long recordRealtime = lastRealtime + currentRealtime;
            WTLogger.d(TAG, "lastRealtime=" + lastRealtime + "    currentRealtime="
                    + currentRealtime + "   recordRealtime = " + recordRealtime);
            spUtils.setLong(SharePreferenceUtils.LAST_REALTIME, recordRealtime);
        }
        spUtils.setBoolean(SharePreferenceUtils.RECORD_LAST_REALTIME, false);
    }
}
