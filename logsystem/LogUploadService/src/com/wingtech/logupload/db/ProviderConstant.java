package com.wingtech.logupload.db;

import android.net.Uri;

public class ProviderConstant {
    public static final String AUTHORITY = "uploadInfo_provider";
    public static final String TAG = "ProviderConstant";

    public static final Uri CONTENT_UPLOAD_INFO_URI = Uri.parse("content://" + AUTHORITY + "/upload_info");

    public interface UploadInfoColumns {
        public static final String ID = "_id";
        public static final String FILE_MD5 = "_fileMD5";
        public static final String FILE_NAME = "_fileName";
        public static final String FILE_PATH = "_filePath";
        public static final String PACKAGENAME = "_packageName";
        public static final String ERRORTYPE = "_errorType";
        public static final String ZIPTIME = "_zipTime";
        public static final String EXCEPTIONCOUNT = "_exceptionCount";
        public static final String ELAPSEDREALTIME = "_elapsedRealTime";
        public static final String LOWRAMCOUNT = "_lowRAMCount";
        public static final String UPLOADCOUNT = "_uploadCount";
        public static final String UPLOAD_STATUS = "_uploadStatus";
        public static final String UPLOAD_TYPE = "_uploadType";
        public static final String SOFT_VERSION = "_soft_version";
        public static final String STACK_INFO1 = "_stack_info1";
        public static final String STACK_INFO2 = "_stack_info2";
        public static final String APP_VERSION = "_app_version";
        public static final String UUID = "_uuid";
        public static final String FIRST_UPLOAD = "_first_upload";
    }
}
