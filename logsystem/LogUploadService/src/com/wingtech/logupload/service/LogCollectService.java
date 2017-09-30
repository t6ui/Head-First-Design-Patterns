package com.wingtech.logupload.service;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.util.Log;

import com.wingtech.logupload.LogCollectSettingActivity;
import com.wingtech.logupload.connect.ILocalSocketConnectCallbacks;
import com.wingtech.logupload.connect.LocalSocketConnect;
import com.wingtech.logupload.model.UploadInfo;
import com.wingtech.logupload.timetask.LowRamScanTimer;
import com.wingtech.logupload.timetask.RealTimeRecordTimer;
import com.wingtech.logupload.timetask.Timer;
import com.wingtech.logupload.timetask.UploadTimer;
import com.wingtech.logupload.utils.NetworkUtils;
import com.wingtech.logupload.utils.SharePreferenceUtils;
import com.wingtech.logupload.utils.UploadInfoHelper;
import com.wingtech.logupload.utils.Utils;
import com.wingtech.logupload.utils.WTLogger;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class LogCollectService extends Service implements ILocalSocketConnectCallbacks {
    public static final int MSG_EXIT = 0;
    public static final int MSG_TIMER_TIMEOUT = 1;
    public static final int MSG_UPLOAD_INFO_PREPARE = 2;
    public static final int MSG_READY_TO_UPLOAD = 3;
    public static final int MSG_SAVE_UPLOAD_INFO = 4;
    public static final int MSG_LOW_RAM_SCAN = 5;
    public static final int MSG_REAL_TIME_RECORD = 6;
    private static final String TAG = "LogCollectService";
    private static SharePreferenceUtils spUtils = null;
    private LogCollectHandler mHandler = null;
    private UploadTimer mUploadTimer = null;
    private LowRamScanTimer mLowRamScanTimer = null;
    private RealTimeRecordTimer mRealTimeRecordTimer = null;
    private LocalSocketConnect mConnect = null;
    private NetworkConnectChangedReceiver mNetworkConnectChangedReceiver = null;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        WTLogger.i(TAG, "onCreate");
        spUtils = SharePreferenceUtils.newInstance(this);
        initConnectThread();
        initLogCollectThread();
        initTimers();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mNetworkConnectChangedReceiver = new NetworkConnectChangedReceiver();
        registerReceiver(mNetworkConnectChangedReceiver, filter);
    }

    private void initLogCollectThread() {
        HandlerThread thread = new HandlerThread(LogCollectService.class.getName());
        thread.start();
        if (mHandler == null) {
            mHandler = new LogCollectHandler(thread.getLooper(), this);
        }
    }

    private void initConnectThread() {
        WTLogger.v(TAG, "initConnectThread");
        HandlerThread connectThread = new HandlerThread(LocalSocketConnect.class.getName());
        connectThread.start();
        if (mConnect == null) {
            mConnect = new LocalSocketConnect(connectThread.getLooper());
            mConnect.registerCallBacks(this);
            mConnect.sendEmptyMessageDelayed(LocalSocketConnect.EVENT_SOCKET_CONNECT, 2000);
            //Message.obtain(mConnect, LocalSocketConnect.EVENT_SOCKET_CONNECT).sendToTarget();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            WTLogger.v(TAG, "onStartCommand " + intent.getAction());
            String action = intent.getAction();
            if (UploadTimer.ACTION_UPLOAD_TIMEOUT.equals(action)) {
                if (mHandler != null) {
                    Message.obtain(mHandler, MSG_TIMER_TIMEOUT, mUploadTimer).sendToTarget();
                }
            } else if (LowRamScanTimer.ACTION_LOW_RAM_SCAN_TIMEOUT.equals(action)) {
                if (mHandler != null) {
                    Message.obtain(mHandler, MSG_TIMER_TIMEOUT, mLowRamScanTimer).sendToTarget();
                }
            } else if (RealTimeRecordTimer.ACTION_REAL_TIME_TIMEOUT.equals(action)) {
                if (mHandler != null) {
                    Message.obtain(mHandler, MSG_TIMER_TIMEOUT, mRealTimeRecordTimer).sendToTarget();
                }
            } else if (LogCollectSettingActivity.ACTION_STOP_COLLECT.equals(action)) {
                cancelTimers();
                stopSelf();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_EXIT);
        }
        cancelTimers();
        unregisterReceiver(mNetworkConnectChangedReceiver);
        super.onDestroy();
    }

    @Override
    public void onEvent(int event, Object obj) {
        WTLogger.v(TAG, "Socket connect event : " + event);
        switch (event) {
            case ILocalSocketConnectCallbacks.EVENT_LOG_INFO:
                CreateUploadInfoFromMessage((String) obj);
                break;
            default:
                break;
        }
    }

    private void CreateUploadInfoFromMessage(String message) {
        if (mHandler == null) {
            WTLogger.e(TAG, "mHandler == null");
            return;
        }
        UploadInfo reportInfo = null;
        UploadInfo logInfo = null;
        String fields[] = message.split(",");
        for (String field : fields) {
            if (field.startsWith("FILE_PATH=")) {
                String path = field.substring("FILE_PATH=".length());
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    reportInfo = new UploadInfo(file, "auto-report", UUID.randomUUID().toString());
                    //关闭log上传
                    if (SystemProperties.getBoolean("persist.sys.pdm.logu.enable", false)) {
                        logInfo = new UploadInfo(file, "auto-log", UUID.randomUUID().toString());
                    }
                } else {
                    WTLogger.d(TAG, "Log file does not exist: " + path);
                    return;
                }
            } else if (field.startsWith("STACK_INFO1=")) {
                String stackInfo1 = field.substring("STACK_INFO1=".length());
                if (reportInfo != null) {
                    reportInfo.setStackInfo1(stackInfo1);
                }
                if(logInfo != null) {
                    logInfo.setStackInfo1(stackInfo1);
                }
            } else if (field.startsWith("STACK_INFO2=")) {
                String stackInfo2 = field.substring("STACK_INFO2=".length());
                if (reportInfo != null) {
                    reportInfo.setStackInfo2(stackInfo2);
                }
                if(logInfo != null) {
                    logInfo.setStackInfo2(stackInfo2);
                }
            } else if (field.startsWith("PROC_NAME=")) {
                String packgeName = field.substring("PROC_NAME=".length());
                if (reportInfo != null) {
                    reportInfo.setPackageName(packgeName);
                }
                if(logInfo != null) {
                    logInfo.setPackageName(packgeName);
                }
            } else if (field.startsWith("APP_VERSION=")) {
                String appVersion = field.substring("APP_VERSION=".length());
                if (reportInfo != null) {
                    reportInfo.setAppVersion(appVersion);
                }
                if(logInfo != null) {
                    logInfo.setAppVersion(appVersion);
                }
            }
        }
        if (reportInfo != null && mHandler != null) { //report info upload former
            fillStatusInfo(reportInfo);
            Message.obtain(mHandler, MSG_SAVE_UPLOAD_INFO, reportInfo).sendToTarget();
        }
        if (logInfo != null && mHandler != null) {
            Message.obtain(mHandler, MSG_SAVE_UPLOAD_INFO, logInfo).sendToTarget();
        }
    }

    private void fillStatusInfo(UploadInfo uploadInfo) {
        long lastUploadRealTime = spUtils.getLong(SharePreferenceUtils.LAST_UPLOAD_REALTIME, 0);
        long currentUploadRealTime = spUtils.getLong(SharePreferenceUtils.LAST_REALTIME, 0) + SystemClock.elapsedRealtime();
        long uploadRealTime = currentUploadRealTime - lastUploadRealTime;
        uploadRealTime = Math.max(uploadRealTime, 0L);

        WTLogger.v(TAG, "last upload time: " + DateUtils.formatElapsedTime(lastUploadRealTime / 1000L));
        WTLogger.v(TAG, "current upload time: " + DateUtils.formatElapsedTime(currentUploadRealTime / 1000L));
        WTLogger.v(TAG, "upload real time: " + DateUtils.formatElapsedTime(uploadRealTime / 1000L));

        uploadInfo.setElapsedRealTime(uploadRealTime + uploadInfo.getElapsedRealTime());
        spUtils.setLong(SharePreferenceUtils.LAST_UPLOAD_REALTIME, currentUploadRealTime);

        int lowRAMCount = spUtils.getInt(SharePreferenceUtils.LOWRAM_COUNT, 0) + uploadInfo.getLowRAMCount();
        uploadInfo.setLowRAMCount(lowRAMCount);
        spUtils.setInt(SharePreferenceUtils.LOWRAM_COUNT, 0);
    }

    private void initTimers() {
        long uploadTimeout = spUtils.getLong(SharePreferenceUtils.UPLOAD_PERIOD_TIMEOUT, -1);
        WTLogger.d(TAG, "Upload timeout(sp) is " + Utils.longTimeToString(uploadTimeout));
        mUploadTimer = UploadTimer.getInstance(this, mHandler);
        mUploadTimer.resetPeriodTimer(uploadTimeout);
        mLowRamScanTimer = LowRamScanTimer.getInstance(this, mHandler);
        mLowRamScanTimer.startTimer();
        mRealTimeRecordTimer = RealTimeRecordTimer.getInstance(this, mHandler);
        mRealTimeRecordTimer.startTimer();
    }

    private void cancelTimers() {
        if (mUploadTimer != null) {
            mUploadTimer.cancelTimer();
            mUploadTimer = null;
        }
        if (mLowRamScanTimer != null) {
            mLowRamScanTimer.cancelTimer();
            mLowRamScanTimer = null;
        }
        if (mRealTimeRecordTimer != null) {
            mRealTimeRecordTimer.cancelTimer();
            mRealTimeRecordTimer = null;
        }
    }

    public class LogCollectHandler extends Handler {
        //        private final String monitorPath =
//                "/data" + File.separator + "log" + File.separator + "monitor_logs" + File.separator;
        private Context mContext;
        private UploadInfoHelper mUploadInfoHelper;

        LogCollectHandler(Looper looper, Context context) {
            super(looper);
            mContext = context;
            mUploadInfoHelper = UploadInfoHelper.getInstance(context);
        }

        public void handleMessage(Message msg) {
            WTLogger.v(TAG, "msg = " + msg.what + " received");
            switch (msg.what) {
                case MSG_EXIT:
                    getLooper().quit();
                    if (mConnect != null) {
                        mConnect.getLooper().quit();
                        mConnect = null;
                    }
                    break;
                case MSG_SAVE_UPLOAD_INFO:
                    handleSaveUploadInfo((UploadInfo) msg.obj);
                    break;
                case MSG_TIMER_TIMEOUT:
                    Timer timer = (Timer) msg.obj;
                    handleTimerTimeout(timer);
                    break;
                case MSG_UPLOAD_INFO_PREPARE:
                    handleUploadInfoPrepare();
                    break;
                case MSG_READY_TO_UPLOAD:
                    handleReadyToUpload((UploadInfo) msg.obj);
                    break;
                case MSG_LOW_RAM_SCAN:
                    handleLowRamScan();
                    break;
                case MSG_REAL_TIME_RECORD:
                    handleRealTimeRecord();
                default:
                    break;
            }
        }

        private void handleSaveUploadInfo(UploadInfo uploadInfo) {
            if (uploadInfo == null) {
                WTLogger.e(TAG, "save upload info error: upload info is null");
                return;
            }
            Uri uri = mUploadInfoHelper.addUploadInfo(uploadInfo);
            uploadInfo.setId(Integer.parseInt(uri.getLastPathSegment()));
            uploadInfo.setUploadStatus(1);
            mUploadInfoHelper.updateUploadInfo(uploadInfo);
            Message.obtain(mHandler, MSG_READY_TO_UPLOAD, uploadInfo).sendToTarget();
        }

        private void handleTimerTimeout(Timer timer) {
            if (timer != null) {
                timer.startTimer();
                timer.handleTimeOut(mHandler);
            }
        }

        private void handleUploadInfoPrepare() {
            if (mHandler == null) {
                WTLogger.e(TAG, "LogCollectHandler have no instance, return.");
                return;
            }
            List<UploadInfo> uploadInfoList = mUploadInfoHelper.getUploadInfoList();
            for (UploadInfo uploadInfo : uploadInfoList) {
                if (uploadInfo.getUploadStatus() == 1) { //uploading
                    WTLogger.d(TAG, "info is uploading, ignore. uploading info: " + uploadInfo);
                    continue;
                }
                uploadInfo.setUploadStatus(1);
                uploadInfo.setUploadCount(0);
                String uploadType = uploadInfo.getUploadType();
                if ("auto-report".equals(uploadType)) {
                    Message.obtain(mHandler, MSG_READY_TO_UPLOAD, uploadInfo).sendToTarget();
                } else if ("auto-log".equals(uploadType)) {
                    String path = uploadInfo.getFilePath();
                    File file = new File(path);
                    if (file.exists() && file.isFile()) {
                        Message.obtain(mHandler, MSG_READY_TO_UPLOAD, uploadInfo).sendToTarget();
                    } else {
                        WTLogger.d(TAG, path + "does not exist, remove info in database.");
                        mUploadInfoHelper.removeUploadInfo(uploadInfo);
                    }
                } else if ("auto-status".equals(uploadType)) {
                    fillStatusInfo(uploadInfo);
                    Message.obtain(mHandler, MSG_READY_TO_UPLOAD, uploadInfo).sendToTarget();
                }
                mUploadInfoHelper.updateUploadInfo(uploadInfo);
            }
        }

        private void handleReadyToUpload(UploadInfo uploadInfo) {
            if (uploadInfo == null) {
                WTLogger.e(TAG, "ready to upload error: upload info is null");
                return;
            }
            WTLogger.v(TAG, "ReadyToUploadInfo: " + uploadInfo);

            Intent intent = new Intent(LogUploadIntentService.ACTION_UPLOAD_REQUEST_INTENT);
            intent.setComponent(new ComponentName("com.wingtech.logupload",
                    "com.wingtech.logupload.service.LogUploadIntentService"));
            intent.putExtra("id", uploadInfo.getId());

            int netWorkTpye = NetworkUtils.getNetWorkType(mContext);
            int firstUpload = uploadInfo.getFirstUpload();
            String uploadType = uploadInfo.getUploadType();

            if (netWorkTpye == NetworkUtils.NETWORK_WLAN) {
                mContext.startService(intent);
            } else if (netWorkTpye == NetworkUtils.NETWORK_MOBILE
                    && ("auto-report".equals(uploadType) || "auto-status".equals(uploadType))) {
                mContext.startService(intent);
            } else if ("auto-log".equals(uploadType) && (firstUpload == 1)) {
                uploadInfo.setUploadStatus(0);
                WTLogger.d(TAG, "current network is invalid, not upload log");
                if (!Utils.isRunningService(mContext, "com.wingtech.logupload.service.LogUploadIntentService")) {
                    WTLogger.v(TAG, "set upload log when connect wifi: true");
                    spUtils.setBoolean(SharePreferenceUtils.UPLOAD_LOG_WHEN_CONNECT_WIFI, true);
                }
            } else if ("auto-report".equals(uploadType) && (firstUpload == 1)) {
                uploadInfo.setUploadStatus(0);
                WTLogger.d(TAG, "current network is invalid, not upload report");
                if (!Utils.isRunningService(mContext, "com.wingtech.logupload.service.LogUploadIntentService")) {
                    WTLogger.v(TAG, "set upload report when connect network: true");
                    spUtils.setBoolean(SharePreferenceUtils.UPLOAD_REPORT_WHEN_CONNECT_NETWORK, true);
                }
            } else {
                uploadInfo.setUploadStatus(0);
                WTLogger.d(TAG, "current upload type is invalid, not upload.");
            }

            uploadInfo.setFirstUpload(0);
            mUploadInfoHelper.updateUploadInfo(uploadInfo);
        }

        private void handleLowRamScan() {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            MemoryInfo mi = new MemoryInfo();
            am.getMemoryInfo(mi);
            long availMemory = mi.availMem;
            long totalMemory = mi.totalMem;

            if (spUtils == null) {
                spUtils = SharePreferenceUtils.newInstance(mContext);
            }

            long recordMemory = totalMemory / 100 * 20;

            int recordCount = spUtils.getInt(SharePreferenceUtils.LOWRAM_COUNT, 0);
            if (availMemory < recordMemory) {
                recordCount++;
                spUtils.setInt(SharePreferenceUtils.LOWRAM_COUNT, recordCount);
            }

            String availMem = Formatter.formatFileSize(getBaseContext(), mi.availMem);
            String totalMem = Formatter.formatFileSize(getBaseContext(), mi.totalMem);
            String recordMem = Formatter.formatFileSize(getBaseContext(), recordMemory);
            WTLogger.v(TAG, "totalMem=" + totalMem + " availMem= " + availMem + " recordMem=" + recordMem);

            WTLogger.v(TAG, "recordCount=" + recordCount);
        }

        private void handleRealTimeRecord() {
            long currentRealtime = SystemClock.elapsedRealtime();
            WTLogger.v(TAG, "currentRealtime=" + currentRealtime + " format is : "
                    + DateUtils.formatElapsedTime(currentRealtime / 1000));
            if (spUtils == null) {
                spUtils = SharePreferenceUtils.newInstance(getApplicationContext());
            }
            spUtils.setLong(SharePreferenceUtils.CURRENT_REALTIME, currentRealtime);
        }
    }


    public class NetworkConnectChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (mHandler == null) {
                    WTLogger.w(TAG, "Warning: mHandler is null when network change");
                    return;
                }
                if (spUtils == null) {
                    spUtils = SharePreferenceUtils.newInstance(context);
                }

                int netWorkType = NetworkUtils.getNetWorkType(context);
                WTLogger.v(TAG, "network change, current state is " + netWorkType);
                boolean uploadWhenWIFI = spUtils.getBoolean(SharePreferenceUtils.UPLOAD_LOG_WHEN_CONNECT_WIFI, false);
                boolean uploadWhenNetwork = spUtils.getBoolean(SharePreferenceUtils.UPLOAD_REPORT_WHEN_CONNECT_NETWORK, false);
                WTLogger.v(TAG, "uploadWhenWIFI is " + uploadWhenWIFI + "， uploadWhenNetwork is " + uploadWhenNetwork);
                if ((netWorkType == NetworkUtils.NETWORK_WLAN && (uploadWhenWIFI || uploadWhenNetwork))
                        || (netWorkType == NetworkUtils.NETWORK_MOBILE && uploadWhenNetwork)) {
                    Message.obtain(mHandler, MSG_TIMER_TIMEOUT, mUploadTimer).sendToTarget();
                } else {
                    WTLogger.v(TAG, "network change, nothing need to do.");
                }
            }
        }
    }
}
