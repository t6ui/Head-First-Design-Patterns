package com.wingtech.logupload.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

public class UploadInfoProvider extends ContentProvider {
    private static final String TAG = "UploadInfoProvider";
    private static final int URI_UPLOAD_INFO = 1;
    private static final int URI_UPLOAD_INFO_ID = 2;
    private static final UriMatcher mMatcher;

    static {
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(ProviderConstant.AUTHORITY, "upload_info", URI_UPLOAD_INFO);
        mMatcher.addURI(ProviderConstant.AUTHORITY, "upload_info/#", URI_UPLOAD_INFO_ID);
    }

    private UploadInfoDbHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = UploadInfoDbHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor c = null;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        String id = null;
        switch (mMatcher.match(uri)) {
            case URI_UPLOAD_INFO:
                c = db.query(UploadInfoDbHelper.TABLE.UPLOAD_INFO, projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            case URI_UPLOAD_INFO_ID:
                id = uri.getPathSegments().get(1);
                c = db.query(UploadInfoDbHelper.TABLE.UPLOAD_INFO, projection, ProviderConstant.UploadInfoColumns.ID + "=" + id
                        + parseSelection(selection), selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        long insertedId = -1;
        switch (mMatcher.match(uri)) {
            case URI_UPLOAD_INFO:
                insertedId = db.insert(UploadInfoDbHelper.TABLE.UPLOAD_INFO, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (insertedId > 0) {
            getContext().getContentResolver().notifyChange(ProviderConstant.CONTENT_UPLOAD_INFO_URI, null);
        }
        return ContentUris.withAppendedId(uri, insertedId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        String id = null;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        switch (mMatcher.match(uri)) {
            case URI_UPLOAD_INFO:
                count = db.delete(UploadInfoDbHelper.TABLE.UPLOAD_INFO, selection, selectionArgs);
                break;
            case URI_UPLOAD_INFO_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(UploadInfoDbHelper.TABLE.UPLOAD_INFO,
                        ProviderConstant.UploadInfoColumns.ID + " = " + id + parseSelection(selection), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(ProviderConstant.CONTENT_UPLOAD_INFO_URI, null);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        String id = null;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        switch (mMatcher.match(uri)) {
            case URI_UPLOAD_INFO:
                count = db.update(UploadInfoDbHelper.TABLE.UPLOAD_INFO, values, selection, selectionArgs);
                break;
            case URI_UPLOAD_INFO_ID:
                id = uri.getPathSegments().get(1);
                count = db.update(UploadInfoDbHelper.TABLE.UPLOAD_INFO, values, ProviderConstant.UploadInfoColumns.ID + "=" + id
                        + parseSelection(selection), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(ProviderConstant.CONTENT_UPLOAD_INFO_URI, null);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    private String parseSelection(String selection) {
        return (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
