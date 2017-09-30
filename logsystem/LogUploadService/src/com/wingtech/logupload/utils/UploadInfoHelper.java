package com.wingtech.logupload.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.wingtech.logupload.db.ProviderConstant;
import com.wingtech.logupload.model.UploadInfo;

import java.util.ArrayList;
import java.util.List;

public class UploadInfoHelper {
    private static final String TAG = "UploadInfoHelper";
    private static UploadInfoHelper mInstance;
    private Context mContext;

    public UploadInfoHelper(Context context) {
        super();
        mContext = context;
    }

    public static UploadInfoHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new UploadInfoHelper(context);
        }
        return mInstance;
    }

    public UploadInfo getUploadInfo(int currentId) {
        UploadInfo info = new UploadInfo();
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(
                    ProviderConstant.CONTENT_UPLOAD_INFO_URI,
                    new String[]{ProviderConstant.UploadInfoColumns.ID,
                            ProviderConstant.UploadInfoColumns.FILE_NAME,
                            ProviderConstant.UploadInfoColumns.FILE_PATH,
                            ProviderConstant.UploadInfoColumns.PACKAGENAME,
                            ProviderConstant.UploadInfoColumns.ERRORTYPE,
                            ProviderConstant.UploadInfoColumns.ZIPTIME,
                            ProviderConstant.UploadInfoColumns.EXCEPTIONCOUNT,
                            ProviderConstant.UploadInfoColumns.ELAPSEDREALTIME,
                            ProviderConstant.UploadInfoColumns.LOWRAMCOUNT,
                            ProviderConstant.UploadInfoColumns.UPLOADCOUNT,
                            ProviderConstant.UploadInfoColumns.UPLOAD_STATUS,
                            ProviderConstant.UploadInfoColumns.UPLOAD_TYPE,
                            ProviderConstant.UploadInfoColumns.SOFT_VERSION,
                            ProviderConstant.UploadInfoColumns.STACK_INFO1,
                            ProviderConstant.UploadInfoColumns.STACK_INFO2,
                            ProviderConstant.UploadInfoColumns.APP_VERSION,
                            ProviderConstant.UploadInfoColumns.UUID,
                            ProviderConstant.UploadInfoColumns.FIRST_UPLOAD
                    },
                    ProviderConstant.UploadInfoColumns.ID + "=" + currentId, null, null);

            if (c != null && c.moveToFirst()) {
                int id = c.getInt(
                        c.getColumnIndex(ProviderConstant.UploadInfoColumns.ID));
                String fileName = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.FILE_NAME));
                String filePath = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.FILE_PATH));
                String packageName = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.PACKAGENAME));
                String errorType = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.ERRORTYPE));

                String zipTime = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.ZIPTIME));

                int exceptionCount = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.EXCEPTIONCOUNT));
                long elapsedRealTime = c
                        .getLong(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.ELAPSEDREALTIME));
                int lowRAMCount = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.LOWRAMCOUNT));
                int uploadCount = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOADCOUNT));
                int uploadStatus = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOAD_STATUS));
                String uploadType = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOAD_TYPE));
                String softVersion = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.SOFT_VERSION));
                String stackInfo1 = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.STACK_INFO1));
                String stackInfo2 = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.STACK_INFO2));
                String appVersion = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.APP_VERSION));
                String uuid = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UUID));
                int firstUpload = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.FIRST_UPLOAD));

                info.setId(id);
                info.setFileName(fileName);
                info.setFilePath(filePath);
                info.setPackageName(packageName);
                info.setErrorType(errorType);
                info.setZipTime(zipTime);
                info.setExceptionCount(exceptionCount);
                info.setElapsedRealTime(elapsedRealTime);
                info.setLowRAMCount(lowRAMCount);
                info.setUploadCount(uploadCount);
                info.setUploadStatus(uploadStatus);
                info.setUploadType(uploadType);
                info.setSoftVersion(softVersion);
                info.setStackInfo1(stackInfo1);
                info.setStackInfo2(stackInfo2);
                info.setAppVersion(appVersion);
                info.setUuid(uuid);
                info.setFirstUpload(firstUpload);
            }
        } catch (Exception e) {
            WTLogger.e(TAG, "getUploadInfo happen Exception = " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return info;
    }

    public UploadInfo getUploadInfoWithMD5(long currentFileMD5) {
        UploadInfo info = null;
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(
                    ProviderConstant.CONTENT_UPLOAD_INFO_URI,
                    new String[]{ProviderConstant.UploadInfoColumns.ID,
                            ProviderConstant.UploadInfoColumns.FILE_MD5,
                            ProviderConstant.UploadInfoColumns.FILE_NAME,
                            ProviderConstant.UploadInfoColumns.FILE_PATH,
                            ProviderConstant.UploadInfoColumns.PACKAGENAME,
                            ProviderConstant.UploadInfoColumns.ERRORTYPE,
                            ProviderConstant.UploadInfoColumns.ZIPTIME,
                            ProviderConstant.UploadInfoColumns.EXCEPTIONCOUNT,
                            ProviderConstant.UploadInfoColumns.ELAPSEDREALTIME,
                            ProviderConstant.UploadInfoColumns.LOWRAMCOUNT,
                            ProviderConstant.UploadInfoColumns.UPLOADCOUNT,
                            ProviderConstant.UploadInfoColumns.UPLOAD_STATUS,
                            ProviderConstant.UploadInfoColumns.UPLOAD_TYPE,
                            ProviderConstant.UploadInfoColumns.SOFT_VERSION,
                            ProviderConstant.UploadInfoColumns.STACK_INFO1,
                            ProviderConstant.UploadInfoColumns.STACK_INFO2,
                            ProviderConstant.UploadInfoColumns.APP_VERSION,
                            ProviderConstant.UploadInfoColumns.UUID,
                            ProviderConstant.UploadInfoColumns.FIRST_UPLOAD,
                    },
                    ProviderConstant.UploadInfoColumns.FILE_MD5 + "=" + currentFileMD5, null, null);

            if (c != null && c.moveToFirst()) {
                int id = c.getInt(
                        c.getColumnIndex(ProviderConstant.UploadInfoColumns.ID));
                long fileMD5 = c.getLong(
                        c.getColumnIndex(ProviderConstant.UploadInfoColumns.FILE_MD5));
                String fileName = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.FILE_NAME));
                String filePath = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.FILE_PATH));
                String packageName = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.PACKAGENAME));
                String errorType = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.ERRORTYPE));

                String zipTime = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.ZIPTIME));

                int exceptionCount = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.EXCEPTIONCOUNT));
                long elapsedRealTime = c
                        .getLong(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.ELAPSEDREALTIME));
                int lowRAMCount = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.LOWRAMCOUNT));
                int uploadCount = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOADCOUNT));
                int uploadStatus = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOAD_STATUS));
                String uploadType = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOAD_TYPE));
                String softVersion = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.SOFT_VERSION));
                String stackInfo1 = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.STACK_INFO1));
                String stackInfo2 = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.STACK_INFO2));
                String appVersion = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.APP_VERSION));
                String uuid = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UUID));
                int firstUpload = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.FIRST_UPLOAD));

                info = new UploadInfo();
                info.setId(id);
                info.setFileMD5(fileMD5);
                info.setFileName(fileName);
                info.setFilePath(filePath);
                info.setPackageName(packageName);
                info.setErrorType(errorType);
                info.setZipTime(zipTime);
                info.setExceptionCount(exceptionCount);
                info.setElapsedRealTime(elapsedRealTime);
                info.setLowRAMCount(lowRAMCount);
                info.setUploadCount(uploadCount);
                info.setUploadStatus(uploadStatus);
                info.setUploadType(uploadType);
                info.setSoftVersion(softVersion);
                info.setStackInfo1(stackInfo1);
                info.setStackInfo2(stackInfo2);
                info.setAppVersion(appVersion);
                info.setUuid(uuid);
                info.setFirstUpload(firstUpload);
            }
        } catch (Exception e) {
            WTLogger.e(TAG, "getUploadInfoWithMD5 happen Exception = " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return info;
    }

    public UploadInfo getStatusInfo() {
        UploadInfo info = null;
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(
                    ProviderConstant.CONTENT_UPLOAD_INFO_URI,
                    new String[]{ProviderConstant.UploadInfoColumns.ID,
                            ProviderConstant.UploadInfoColumns.FILE_MD5,
                            ProviderConstant.UploadInfoColumns.FILE_NAME,
                            ProviderConstant.UploadInfoColumns.FILE_PATH,
                            ProviderConstant.UploadInfoColumns.PACKAGENAME,
                            ProviderConstant.UploadInfoColumns.ERRORTYPE,
                            ProviderConstant.UploadInfoColumns.ZIPTIME,
                            ProviderConstant.UploadInfoColumns.EXCEPTIONCOUNT,
                            ProviderConstant.UploadInfoColumns.ELAPSEDREALTIME,
                            ProviderConstant.UploadInfoColumns.LOWRAMCOUNT,
                            ProviderConstant.UploadInfoColumns.UPLOADCOUNT,
                            ProviderConstant.UploadInfoColumns.UPLOAD_STATUS,
                            ProviderConstant.UploadInfoColumns.UPLOAD_TYPE,
                            ProviderConstant.UploadInfoColumns.SOFT_VERSION,
                            ProviderConstant.UploadInfoColumns.STACK_INFO1,
                            ProviderConstant.UploadInfoColumns.STACK_INFO2,
                            ProviderConstant.UploadInfoColumns.APP_VERSION,
                            ProviderConstant.UploadInfoColumns.UUID,
                            ProviderConstant.UploadInfoColumns.FIRST_UPLOAD
                    },
                    ProviderConstant.UploadInfoColumns.UPLOAD_TYPE + "=" + "'" + "auto-status" + "'", null, null); //Note

            if (c != null && c.moveToFirst()) {
                int id = c.getInt(
                        c.getColumnIndex(ProviderConstant.UploadInfoColumns.ID));
                long fileMD5 = c.getLong(
                        c.getColumnIndex(ProviderConstant.UploadInfoColumns.FILE_MD5));
                String fileName = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.FILE_NAME));
                String filePath = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.FILE_PATH));
                String packageName = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.PACKAGENAME));
                String errorType = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.ERRORTYPE));

                String zipTime = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.ZIPTIME));

                int exceptionCount = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.EXCEPTIONCOUNT));
                long elapsedRealTime = c
                        .getLong(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.ELAPSEDREALTIME));
                int lowRAMCount = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.LOWRAMCOUNT));
                int uploadCount = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOADCOUNT));
                int uploadStatus = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOAD_STATUS));
                String uploadType = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOAD_TYPE));
                String softVersion = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.SOFT_VERSION));
                String stackInfo1 = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.STACK_INFO1));
                String stackInfo2 = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.STACK_INFO2));
                String appVersion = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.APP_VERSION));
                String uuid = c
                        .getString(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.UUID));
                int firstUpload = c
                        .getInt(c
                                .getColumnIndex(ProviderConstant.UploadInfoColumns.FIRST_UPLOAD));

                info = new UploadInfo();
                info.setId(id);
                info.setFileMD5(fileMD5);
                info.setFileName(fileName);
                info.setFilePath(filePath);
                info.setPackageName(packageName);
                info.setErrorType(errorType);
                info.setZipTime(zipTime);
                info.setExceptionCount(exceptionCount);
                info.setElapsedRealTime(elapsedRealTime);
                info.setLowRAMCount(lowRAMCount);
                info.setUploadCount(uploadCount);
                info.setUploadStatus(uploadStatus);
                info.setUploadType(uploadType);
                info.setSoftVersion(softVersion);
                info.setStackInfo1(stackInfo1);
                info.setStackInfo2(stackInfo2);
                info.setAppVersion(appVersion);
                info.setUuid(uuid);
                info.setFirstUpload(firstUpload);
            }
        } catch (Exception e) {
            WTLogger.e(TAG, "getStatusInfo happen Exception = " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return info;
    }

    public List<UploadInfo> getUploadInfoList() {
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(
                    ProviderConstant.CONTENT_UPLOAD_INFO_URI,
                    new String[]{ProviderConstant.UploadInfoColumns.ID,
                            ProviderConstant.UploadInfoColumns.FILE_MD5,
                            ProviderConstant.UploadInfoColumns.FILE_NAME,
                            ProviderConstant.UploadInfoColumns.FILE_PATH,
                            ProviderConstant.UploadInfoColumns.PACKAGENAME,
                            ProviderConstant.UploadInfoColumns.ERRORTYPE,
                            ProviderConstant.UploadInfoColumns.ZIPTIME,
                            ProviderConstant.UploadInfoColumns.EXCEPTIONCOUNT,
                            ProviderConstant.UploadInfoColumns.ELAPSEDREALTIME,
                            ProviderConstant.UploadInfoColumns.LOWRAMCOUNT,
                            ProviderConstant.UploadInfoColumns.UPLOADCOUNT,
                            ProviderConstant.UploadInfoColumns.UPLOAD_STATUS,
                            ProviderConstant.UploadInfoColumns.UPLOAD_TYPE,
                            ProviderConstant.UploadInfoColumns.SOFT_VERSION,
                            ProviderConstant.UploadInfoColumns.STACK_INFO1,
                            ProviderConstant.UploadInfoColumns.STACK_INFO2,
                            ProviderConstant.UploadInfoColumns.APP_VERSION,
                            ProviderConstant.UploadInfoColumns.UUID,
                            ProviderConstant.UploadInfoColumns.FIRST_UPLOAD
                    },
                    null, null, null);

            List<UploadInfo> list = new ArrayList<UploadInfo>();
            if (c != null && c.moveToFirst()) {
                do {
                    int id = c.getInt(
                            c.getColumnIndex(ProviderConstant.UploadInfoColumns.ID));
                    long fileMD5 = c.getLong(
                            c.getColumnIndex(ProviderConstant.UploadInfoColumns.FILE_MD5));
                    String fileName = c
                            .getString(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.FILE_NAME));
                    String filePath = c
                            .getString(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.FILE_PATH));
                    String packageName = c
                            .getString(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.PACKAGENAME));
                    String errorType = c
                            .getString(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.ERRORTYPE));

                    String zipTime = c
                            .getString(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.ZIPTIME));

                    int exceptionCount = c
                            .getInt(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.EXCEPTIONCOUNT));
                    long elapsedRealTime = c
                            .getLong(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.ELAPSEDREALTIME));
                    int lowRAMCount = c
                            .getInt(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.LOWRAMCOUNT));
                    int uploadCount = c
                            .getInt(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOADCOUNT));
                    int uploadStatus = c
                            .getInt(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOAD_STATUS));
                    String uploadType = c
                            .getString(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.UPLOAD_TYPE));
                    String softVersion = c
                            .getString(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.SOFT_VERSION));
                    String stackInfo1 = c
                            .getString(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.STACK_INFO1));
                    String stackInfo2 = c
                            .getString(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.STACK_INFO2));
                    String appVersion = c
                            .getString(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.APP_VERSION));
                    String uuid = c
                            .getString(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.UUID));
                    int firstUpload = c
                            .getInt(c
                                    .getColumnIndex(ProviderConstant.UploadInfoColumns.FIRST_UPLOAD));

                    UploadInfo info = new UploadInfo();
                    info.setId(id);
                    info.setFileMD5(fileMD5);
                    info.setFileName(fileName);
                    info.setFilePath(filePath);
                    info.setPackageName(packageName);
                    info.setErrorType(errorType);
                    info.setZipTime(zipTime);
                    info.setExceptionCount(exceptionCount);
                    info.setElapsedRealTime(elapsedRealTime);
                    info.setLowRAMCount(lowRAMCount);
                    info.setUploadCount(uploadCount);
                    info.setUploadStatus(uploadStatus);
                    info.setUploadType(uploadType);
                    info.setSoftVersion(softVersion);
                    info.setStackInfo1(stackInfo1);
                    info.setStackInfo2(stackInfo2);
                    info.setAppVersion(appVersion);
                    info.setUuid(uuid);
                    info.setFirstUpload(firstUpload);
                    list.add(info);
                } while (c.moveToNext());
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }

    public Uri addUploadInfo(UploadInfo info) {
        ContentValues value = info.getDbContentValue();
        final ContentResolver cr = mContext.getContentResolver();
        return cr.insert(ProviderConstant.CONTENT_UPLOAD_INFO_URI, value);
    }

    public void removeUploadInfo(int infoId) {
        final ContentResolver cr = mContext.getContentResolver();
        cr.delete(
                ProviderConstant.CONTENT_UPLOAD_INFO_URI,
                ProviderConstant.UploadInfoColumns.ID + " = '"
                        + infoId + "'", null);
    }

    public void removeUploadInfo(UploadInfo info) {
        final ContentResolver cr = mContext.getContentResolver();
        cr.delete(
                ProviderConstant.CONTENT_UPLOAD_INFO_URI,
                ProviderConstant.UploadInfoColumns.ID + " = '"
                        + info.getId() + "'", null);
    }

    public void removeAllUploadInfo(List<UploadInfo> list) {
        final ContentResolver cr = mContext.getContentResolver();
        for (UploadInfo info : list) {
            cr.delete(ProviderConstant.CONTENT_UPLOAD_INFO_URI,
                    ProviderConstant.UploadInfoColumns.ID + " = '"
                            + info.getId() + "'", null);
        }
    }

    public void updateUploadInfo(UploadInfo info) {
        ContentValues value = info.getDbContentValue();
        final ContentResolver cr = mContext.getContentResolver();
        cr.update(ProviderConstant.CONTENT_UPLOAD_INFO_URI, value,
                ProviderConstant.UploadInfoColumns.ID + " = '"
                        + info.getId() + "'", null);
    }
}
