package com.wingtech.logupload.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.wingtech.logupload.model.UploadInfo;
import com.wingtech.logupload.utils.FileHelper;
import com.wingtech.logupload.utils.InfoUploader;
import com.wingtech.logupload.utils.InfoUploader.LogUploadListener;
import com.wingtech.logupload.utils.UploadInfoHelper;
import com.wingtech.logupload.utils.WTLogger;

public class LogUploadIntentService extends IntentService {
    public static final String ACTION_UPLOAD_REQUEST_INTENT = "com.wingtech.logupload.UPLOAD_REQUEST";
    private static final String TAG = "LogUploadIntentService";
    private Context mContext;
    private UploadInfoHelper mUploadInfoHelper;
    private FileHelper mFileHelper = null;
    private UploadInfo mUploadInfo;
    LogUploadListener mLogUploadListener = new LogUploadListener() {
        @Override
        public void onFinish(int code, String res) {
            if (code == 200 && res.contains("ok")) {
                if ("auto-status".equals(mUploadInfo.getUploadType())) {
                    mUploadInfo.setElapsedRealTime(0L);
                    mUploadInfo.setLowRAMCount(0);
                    mUploadInfo.setUploadStatus(0);
                    mUploadInfoHelper.updateUploadInfo(mUploadInfo);
                } else if ("auto-log".equals(mUploadInfo.getUploadType())) {
                    mFileHelper.deleteFile(mUploadInfo.getFilePath());
                    mUploadInfoHelper.removeUploadInfo(mUploadInfo);
                } else {
                    mUploadInfoHelper.removeUploadInfo(mUploadInfo);
                }
            } else {
                reUploadInfoByHttp(mContext, mUploadInfo);
            }
        }

        @Override
        public void onError(String error) {
            reUploadInfoByHttp(mContext, mUploadInfo);
        }
    };

    public LogUploadIntentService() {
        super("LogUploadIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mUploadInfoHelper = UploadInfoHelper.getInstance(this);
        mFileHelper = FileHelper.getInstance();
        WTLogger.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WTLogger.d(TAG, "onDestroy");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (ACTION_UPLOAD_REQUEST_INTENT.equals(action)) {
            int infoId = intent.getExtras().getInt("id");
            mUploadInfo = mUploadInfoHelper.getUploadInfo(infoId);
            uploadInfoByHttp(mContext, mUploadInfo);
        }
    }

    private void uploadInfoByHttp(Context context, UploadInfo uploadInfo) {
        if (uploadInfo.getUploadType() != null) {
            long startTime = SystemClock.elapsedRealtime();
            String logPrefix = "id=" + uploadInfo.getId() + " ";
            WTLogger.i(TAG, logPrefix, "start upload");
            InfoUploader.uploadByHttp(context, uploadInfo, mLogUploadListener);
            WTLogger.i(TAG, logPrefix, "end upload, spend time(ms): " + (SystemClock.elapsedRealtime() - startTime));
        }
    }

    private void reUploadInfoByHttp(Context context, UploadInfo uploadInfo) {
        if (uploadInfo.getUploadCount() < 1) {
            uploadInfo.setUploadCount(uploadInfo.getUploadCount() + 1);
            mUploadInfoHelper.updateUploadInfo(uploadInfo);
            uploadInfoByHttp(context, uploadInfo);
        } else {
            uploadInfo.setUploadCount(0);
            mUploadInfoHelper.updateUploadInfo(uploadInfo);
        }
    }
}
