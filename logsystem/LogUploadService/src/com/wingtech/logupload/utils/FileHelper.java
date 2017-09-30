package com.wingtech.logupload.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class FileHelper {
    private static final String TAG = Utils.TAG;
    private static FileHelper mInstance;

    public static FileHelper getInstance() {
        if (mInstance == null) {
            mInstance = new FileHelper();
        }
        return mInstance;
    }

    public static long getFileMD5(File file) {
        if (!file.isFile()) {
            return -1;
        }
        MessageDigest digest = null;
        byte buffer[] = null;
        try {
            digest = MessageDigest.getInstance("MD5");
            String fileName = file.getPath();
            if (fileName != null) {
                buffer = fileName.getBytes("UTF-8");
            }
            if (buffer != null) {
                digest.update(buffer, 0, buffer.length);
            } else {
                WTLogger.e(TAG, "get byte array from file path error");
                return -1;
            }
        } catch (NoSuchAlgorithmException e) {
            WTLogger.e(TAG, e.getMessage());
            return -1;
        } catch (UnsupportedEncodingException e) {
            WTLogger.e(TAG, e.getMessage());
            return -1;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.longValue();
    }

    public static void zipFiles(String folderPath, String filePath,
                                ZipOutputStream zipOut) throws Exception {
        if (zipOut == null) {
            return;
        }
        File file = new File(folderPath + filePath);
        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(filePath);
            FileInputStream inputStream = new FileInputStream(file);
            zipOut.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[100000];
            while ((len = inputStream.read(buffer)) != -1) {
                zipOut.write(buffer, 0, len);
            }
            inputStream.close();
            zipOut.closeEntry();
        } else {
            String fileList[] = file.list();
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(filePath + File.separator);
                zipOut.putNextEntry(zipEntry);
                zipOut.closeEntry();
            }
            for (int i = 0; i < fileList.length; i++) {
                zipFiles(folderPath, filePath + File.separator
                        + fileList[i], zipOut);
            }
        }
    }

    public List<File> getFileList(String filePath) {
        List<File> fileList = new LinkedList<File>();

        File dFile = new File(filePath);
        if (!dFile.exists()) {
            Log.d(TAG, "file is not exist!=" + filePath);
            return null;
        }

        if (dFile.isDirectory()) {
            File files[] = dFile.listFiles();
            if (files == null || files.length == 0) {
                return null;
            }
            for (File file : files) {
                if (file.isFile() && file.getAbsolutePath().contains("ErrorType")) {
                    Log.d(TAG, "add file: " + file.getAbsolutePath());
                    fileList.add(file);
                }
            }
        }

        if (fileList.size() == 0) {
            fileList = null;
        }
        return fileList;
    }

    public boolean isExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public boolean isFolderEmpty(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Log.d(TAG, "file is not exist!=" + filePath);
            return true;
        }
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            if (files != null) {
                return (files.length == 0) ? true : false;
            }
        }
        return true;
    }

    public int getFileCount(String filePath) {
        File file = new File(filePath);
        File[] files = file.listFiles();
        if (files != null) {
            return files.length;
        }
        return 0;
    }

    public long getFolderSize(String filePath) {
        File file = new File(filePath);
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i].getPath());

                } else {
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public boolean deleteFolder(String filePath, boolean isDelteRoot) {
        File file = new File(filePath);
        Log.d(TAG, "deleteFolder .... " + filePath);
        if (!file.exists()) {
            Log.d(TAG, "file is not exist!=" + filePath);
            return false;
        }

        if (file.isDirectory()) {
            File files[] = file.listFiles();
            Log.d(TAG, "files size()=" + files.length);
            for (int i = 0; i < files.length; i++) {
                Log.d(TAG, "files[" + i + "]=" + files[i].getPath());
                deleteFolder(files[i].getPath(), true);
            }
        }
        if (!isDelteRoot) {
            Log.d(TAG, "root dir is " + filePath);
        } else {
            file.delete();
        }
        return true;
    }

    public void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Log.d(TAG, "file is not exist!=" + path);
        }
        file.delete();
    }

    public String getDiagPath() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                String path = sdCard.getPath() + "/diag_logs";
                File file = new File(path);
                if (file.exists()) {
                    File[] childFiles = file.listFiles();
                    if (childFiles != null && childFiles.length > 0) {
                        for (int i = 0; i < childFiles.length; i++) {
                            childFiles[i].delete();
                        }
                    }
                } else {
                    file.mkdir();
                }
                String diagPath = path + "/Diag.cfg";
                File diagFile = new File(diagPath);
                if (diagFile.exists()) {
                    diagFile.delete();
                }
                diagFile.createNewFile();
                return "/diag_logs/Diag.cfg";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void saveToSDCard(Context context, int fromPath, String toPath) {
        try {
            InputStream inStream = context.getResources().openRawResource(
                    fromPath);
            File file = new File(Environment.getExternalStorageDirectory(),
                    toPath);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] buffer = new byte[10];
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            byte[] bs = outStream.toByteArray();
            fileOutputStream.write(bs);
            outStream.close();
            inStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void isExist(String filePatch) {
        File file = new File(filePatch);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public void zipFolder(String srcFilePath, String zipFilePath) {
        try {
            ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(
                    zipFilePath));
            File file = new File(srcFilePath);
            zipFiles(file.getParent() + File.separator, file.getName(), outZip);
            outZip.finish();
            outZip.close();
        } catch (Exception e) {
            Log.e(TAG,
                    "zip" + srcFilePath + " Folder fail e = "
                            + e.getStackTrace());
        }
    }

    public void copyFile(String fromFile, String toFile) {
        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();

        } catch (Exception e) {
            Log.e(TAG, " copy error file:" + fromFile + "   e=" + e);
        }
    }

    public void copyDirectory(String fromFile, String toFile) {
        File[] currentFiles;
        File root = new File(fromFile);
        if (!root.exists()) {
            Log.e(TAG, fromFile + " is not exist,return");
            return;
        }
        currentFiles = root.listFiles();

        File targetDir = new File(toFile);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory()) {
                copyDirectory(currentFiles[i].getPath() + "/", toFile
                        + currentFiles[i].getName() + "/");
            } else {
                copyFile(currentFiles[i].getPath(),
                        toFile + currentFiles[i].getName());
            }
        }
    }

    public void copyRecoveryLog(String fromFile, String toFile) {
        File[] currentFiles;
        File root = new File(fromFile);
        if (!root.exists()) {
            Log.e(TAG, fromFile + " is not exist,return");
            return;
        }
        currentFiles = root.listFiles();

        File targetDir = new File(toFile);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        if (currentFiles != null && currentFiles.length > 0) {
            for (int i = 0; i < currentFiles.length; i++) {
                if (currentFiles[i].isDirectory()) {
                    copyRecoveryLog(currentFiles[i].getPath() + File.separator,
                            toFile + currentFiles[i].getName() + File.separator);
                } else {
                    String currentFileName = currentFiles[i].getName();
                    if (currentFileName.contains("log")
                            || currentFileName.contains("kmsg")) {
                        copyFile(currentFiles[i].getPath(), toFile
                                + File.separator + currentFiles[i].getName());
                    }
                }
            }
        }
    }

    public synchronized void captureBugreport(String path) {
        FileOutputStream os = null;
        InputStreamReader inputReader = null;
        OutputStreamWriter osw = null;
        BufferedReader bufferedReader = null;
        Process process = null;
        try {
            File bugreportFile = new File(path);
            if (!bugreportFile.getParentFile().exists()) {
                if (!bugreportFile.getParentFile().mkdirs()) {
                    Log.e(TAG, "could not make dir fail :"
                            + bugreportFile.getParentFile().getName());
                }
            }
            process = Runtime.getRuntime().exec("bugreport");
            os = new FileOutputStream(bugreportFile);
            osw = new OutputStreamWriter(os, "utf-8");
            WriteErrorThread t1 = new WriteErrorThread(osw, new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), "utf-8")));
            t1.start();

            inputReader = new InputStreamReader(process.getInputStream(),
                    "utf-8");
            bufferedReader = new BufferedReader(inputReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                osw.write(line);
                osw.write("\r\n");
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "capture bugreport or occur error");
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    public String getUriPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    class WriteErrorThread extends Thread {
        OutputStreamWriter outputStream = null;
        BufferedReader bufferedReader = null;

        public WriteErrorThread(OutputStreamWriter osw, BufferedReader br) {
            outputStream = osw;
            bufferedReader = br;
        }

        @Override
        public void run() {
            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    outputStream.write(line);
                    outputStream.write("\r\n");
                    bufferedReader.readLine();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

    }
}
