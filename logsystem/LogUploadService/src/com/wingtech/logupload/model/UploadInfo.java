package com.wingtech.logupload.model;

import android.content.ContentValues;

import com.wingtech.logupload.db.ProviderConstant;
import com.wingtech.logupload.utils.Utils;

import java.io.File;

public class UploadInfo {
    private static final String TAG = "UploadInfo";
    private int id;
    private long fileMD5;
    private String fileName;
    private String filePath;
    private String packageName;
    private String errorType;
    private String zipTime;
    private int exceptionCount;
    private long elapsedRealTime;
    private int lowRAMCount;
    private int uploadCount;
    private int uploadStatus; //0 not uploading, 1 uploading
    private String uploadType;
    private String softVersion;
    private String stackInfo1;
    private String stackInfo2;
    private String appVersion;
    private String uuid;
    private int firstUpload;

    public UploadInfo() {
        super();
    }

    public UploadInfo(File file, String uploadType, String uuid) {
        this.uploadType = uploadType;
        this.uuid = uuid;
        fileName = file.getName();
        filePath = file.getAbsolutePath();
        //fileMD5 = FileHelper.getFileMD5(file);
        softVersion = Utils.getSoftVersion();
        uploadCount = 0;
        firstUpload = 1;
        String name = file.getName();
        String field[] = fileName.substring(0, name.indexOf(".")).split("_");
        errorType = field[1];
        zipTime = field[2];
        if ("auto-report".equals(uploadType)) {
            exceptionCount = 1;
            elapsedRealTime = 0;
            lowRAMCount = 0;
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ContentValues getDbContentValue() {
        ContentValues values = new ContentValues();
        if (fileMD5 != 0) {
            values.put(ProviderConstant.UploadInfoColumns.FILE_MD5,
                    (long) fileMD5);
        }
        if (fileName != null) {
            values.put(ProviderConstant.UploadInfoColumns.FILE_NAME,
                    (String) fileName);
        }
        if (filePath != null) {
            values.put(ProviderConstant.UploadInfoColumns.FILE_PATH,
                    (String) filePath);
        }
        if (packageName != null) {
            values.put(ProviderConstant.UploadInfoColumns.PACKAGENAME,
                    (String) packageName);
        }
        if (errorType != null) {
            values.put(ProviderConstant.UploadInfoColumns.ERRORTYPE,
                    (String) errorType);
        }
        if (zipTime != null) {
            values.put(ProviderConstant.UploadInfoColumns.ZIPTIME,
                    (String) zipTime);
        }
        if (exceptionCount >= 0) {
            values.put(ProviderConstant.UploadInfoColumns.EXCEPTIONCOUNT,
                    (int) exceptionCount);
        }
        if (elapsedRealTime >= 0) {
            values.put(ProviderConstant.UploadInfoColumns.ELAPSEDREALTIME,
                    (long) elapsedRealTime);
        }
        if (lowRAMCount >= 0) {
            values.put(ProviderConstant.UploadInfoColumns.LOWRAMCOUNT,
                    (int) lowRAMCount);
        }
        if (uploadCount >= 0) {
            values.put(ProviderConstant.UploadInfoColumns.UPLOADCOUNT,
                    (int) uploadCount);
        }
        if (uploadStatus >= 0) {
            values.put(ProviderConstant.UploadInfoColumns.UPLOAD_STATUS,
                    (int) uploadStatus);
        }
        if (uploadType != null) {
            values.put(ProviderConstant.UploadInfoColumns.UPLOAD_TYPE,
                    (String) uploadType);
        }
        if (softVersion != null) {
            values.put(ProviderConstant.UploadInfoColumns.SOFT_VERSION,
                    (String) softVersion);
        }
        if (stackInfo1 != null) {
            values.put(ProviderConstant.UploadInfoColumns.STACK_INFO1,
                    (String) stackInfo1);
        }
        if (stackInfo2 != null) {
            values.put(ProviderConstant.UploadInfoColumns.STACK_INFO2,
                    (String) stackInfo2);
        }
        if (appVersion != null) {
            values.put(ProviderConstant.UploadInfoColumns.APP_VERSION,
                    (String) appVersion);
        }
        if (uuid != null) {
            values.put(ProviderConstant.UploadInfoColumns.UUID,
                    (String) uuid);
        }
        values.put(ProviderConstant.UploadInfoColumns.FIRST_UPLOAD,
                (int) firstUpload);

        return values;
    }

    public String getStackInfo1() {
        return stackInfo1;
    }

    public void setStackInfo1(String stackInfo1) {
        this.stackInfo1 = stackInfo1;
    }

    public String getStackInfo2() {
        return stackInfo2;
    }

    public void setStackInfo2(String stackInfo2) {
        this.stackInfo2 = stackInfo2;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getFileMD5() {
        return fileMD5;
    }

    public void setFileMD5(long fileMD5) {
        this.fileMD5 = fileMD5;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getZipTime() {
        return zipTime;
    }

    public void setZipTime(String zipTime) {
        this.zipTime = zipTime;
    }

    public int getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(int exceptionCount) {
        this.exceptionCount = exceptionCount;
    }

    public long getElapsedRealTime() {
        return elapsedRealTime;
    }

    public void setElapsedRealTime(long elapsedRealTime) {
        this.elapsedRealTime = elapsedRealTime;
    }

    public int getLowRAMCount() {
        return lowRAMCount;
    }

    public void setLowRAMCount(int lowRAMCount) {
        this.lowRAMCount = lowRAMCount;
    }

    public int getUploadCount() {
        return uploadCount;
    }

    public void setUploadCount(int uploadCount) {
        this.uploadCount = uploadCount;
    }

    public int getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(int uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public String getUploadType() {
        return uploadType;
    }

    public void setUploadType(String uploadType) {
        this.uploadType = uploadType;
    }

    public String getSoftVersion() {
        return softVersion;
    }

    public void setSoftVersion(String softVersion) {
        this.softVersion = softVersion;
    }

    public int getFirstUpload() {
        return firstUpload;
    }

    public void setFirstUpload(int firstUpload) {
        this.firstUpload = firstUpload;
    }

    @Override
    public String toString() {
        return "UploadInfo{" +
                "id=" + id +
                ", fileMD5=" + fileMD5 +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", packageName='" + packageName + '\'' +
                ", errorType='" + errorType + '\'' +
                ", zipTime='" + zipTime + '\'' +
                ", exceptionCount=" + exceptionCount +
                ", elapsedRealTime=" + elapsedRealTime +
                ", lowRAMCount=" + lowRAMCount +
                ", uploadCount=" + uploadCount +
                ", uploadStatus=" + uploadStatus +
                ", uploadType='" + uploadType + '\'' +
                ", softVersion='" + softVersion + '\'' +
                ", stackInfo1='" + stackInfo1 + '\'' +
                ", stackInfo2='" + stackInfo2 + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", uuid='" + uuid + '\'' +
                ", firstUpload=" + firstUpload +
                '}';
    }
}
