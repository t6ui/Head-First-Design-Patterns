package com.wingtech.logupload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UploadInfoDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "UploadInfoDbHelper";
    private static final String DB_NAME = "upload_info.db";
    private static final int DB_VERSION = 1;
    private static final String CREATE_UPLOAD_INFO_TABLE_SQL =
            "CREATE TABLE " + TABLE.UPLOAD_INFO + "(" +
                    ProviderConstant.UploadInfoColumns.ID + " INTEGER PRIMARY KEY," +
                    ProviderConstant.UploadInfoColumns.FILE_MD5 + " LONG NOT NULL DEFAULT 0 ," +
                    ProviderConstant.UploadInfoColumns.FILE_NAME + " TEXT NOT NULL DEFAULT '' ," +
                    ProviderConstant.UploadInfoColumns.FILE_PATH + " TEXT NOT NULL DEFAULT '' ," +
                    ProviderConstant.UploadInfoColumns.PACKAGENAME + " TEXT NOT NULL DEFAULT '' ," +
                    ProviderConstant.UploadInfoColumns.ERRORTYPE + " TEXT NOT NULL DEFAULT '' ," +
                    ProviderConstant.UploadInfoColumns.ZIPTIME + " TEXT NOT NULL DEFAULT '' ," +
                    ProviderConstant.UploadInfoColumns.EXCEPTIONCOUNT + " INTEGER NOT NULL DEFAULT 0 ," +
                    ProviderConstant.UploadInfoColumns.ELAPSEDREALTIME + " LONG NOT NULL DEFAULT 0 ," +
                    ProviderConstant.UploadInfoColumns.LOWRAMCOUNT + " INTEGER NOT NULL DEFAULT 0 ," +
                    ProviderConstant.UploadInfoColumns.UPLOADCOUNT + " INTEGER NOT NULL DEFAULT 0 ," +
                    ProviderConstant.UploadInfoColumns.UPLOAD_STATUS + " INTEGER NOT NULL DEFAULT 0 ," +
                    ProviderConstant.UploadInfoColumns.UPLOAD_TYPE + " TEXT NOT NULL DEFAULT '' ," +
                    ProviderConstant.UploadInfoColumns.SOFT_VERSION + " TEXT NOT NULL DEFAULT '' ," +
                    ProviderConstant.UploadInfoColumns.STACK_INFO1 + " TEXT NOT NULL DEFAULT '' ," +
                    ProviderConstant.UploadInfoColumns.STACK_INFO2 + " TEXT NOT NULL DEFAULT '' ," +
                    ProviderConstant.UploadInfoColumns.APP_VERSION + " TEXT NOT NULL DEFAULT '' ," +
                    ProviderConstant.UploadInfoColumns.UUID + " TEXT NOT NULL DEFAULT '' ," +
                    ProviderConstant.UploadInfoColumns.FIRST_UPLOAD + " INTEGER NOT NULL DEFAULT 0 " +
                    ")";
    private static UploadInfoDbHelper mInstance;

    public UploadInfoDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    static synchronized UploadInfoDbHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new UploadInfoDbHelper(context);
        }
        return mInstance;
    }

    public void createLogTable(SQLiteDatabase db) {
        db.execSQL(CREATE_UPLOAD_INFO_TABLE_SQL);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createLogTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public interface TABLE {
        public static final String UPLOAD_INFO = "upload_info";
    }
}
