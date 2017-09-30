package com.wingtech.logupload.utils;

import android.content.Context;

import com.wingtech.logupload.model.UploadInfo;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class InfoUploader {
    public static final String TAG = "InfoUploader";
    //测试服务器地址
    public static final String UPLOAD_URL = "http://122.225.54.25/uplog/uplog";
    //正式服务器地址
    //public static final String UPLOAD_URL = "http://122.225.24.51/uplog/uplog";
    private static final String CHARSET = "utf-8";
    private static final String PREFIX = "--";
    private static final String LINE_END = "\r\n";
    private static final String BOUNDARY = "bc2f8441-ad5a-4a62-8343-331e8f11dfac"; // 边界标识
    private static final int TIME_OUT = 300000; //5 min
    private static final int TANSF_CHUNK = 1024000;

    public static void uploadByHttp(Context context,
                                    UploadInfo info, LogUploadListener listener) {
        String logPrefix = "id=" + info.getId() + " ";

        File file = new File(info.getFilePath());
        if (!file.exists() && "auto-log".equals(info.getUploadType())) {
            WTLogger.e(TAG, logPrefix, info.getFilePath() + " not exists, return.");
            return;
        }

        String response = null;
        int responseCode = 0;
        DataOutputStream dos = null;
        InputStream is = null;
        try {
            //1.set connect
            //System.setProperty("http.keepAlive", "false");
            URL url = new URL(UPLOAD_URL + "?t=" + System.currentTimeMillis());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST"); // 请求方式
            conn.setRequestProperty("Charset", CHARSET);// 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + BOUNDARY);

            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);

            conn.setDoInput(true); // 允许输入流
            conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            //conn.setChunkedStreamingMode(TANSF_CHUNK);

            WTLogger.v(TAG, logPrefix, "connecting...");
            conn.connect();
            WTLogger.v(TAG, logPrefix, "end connecting");

            //2.send data
            dos = new DataOutputStream(conn.getOutputStream());
            WTLogger.v(TAG, logPrefix, "send info data");
            if ("auto-log".equals(info.getUploadType())) {
                long totalbytes = file.length();
                WTLogger.v(TAG, logPrefix, "upload log size(bytes):" + totalbytes);
                sendInfoDataWithLog(dos, info, context);
            } else {
                sendInfoData(dos, info, context);
            }
            WTLogger.v(TAG, logPrefix, "flushing...");
            dos.flush();

            //3.get Response
            WTLogger.v(TAG, logPrefix, "acquire response");
            responseCode = conn.getResponseCode();
            WTLogger.v(TAG, logPrefix, "responseCode=" + responseCode);

            is = conn.getInputStream();
            StringBuffer sb = new StringBuffer();
            byte[] buffer = new byte[32];
            int readCount = 0;
            while (true) {
                readCount = is.read(buffer);
                if (-1 == readCount) {
                    break;
                } else {
                    sb.append(new String(buffer, 0, readCount, "utf-8"));
                }
            }
            response = sb.toString();
            listener.onFinish(responseCode, response);

//            StringBuilder sb = new StringBuilder();
//            InputStream is = conn.getInputStream();
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//            String line;
//            while ((line = br.readLine()) != null) {
//                sb.append(line);
//            }
//            br.close();
//            WTLogger.v(TAG, logPrefix, "response content: " + sb.toString());
//            InputStream es = conn.getErrorStream();
//            sb.setLength(0);
//            br = new BufferedReader(new InputStreamReader(es));
//            while ((line = br.readLine()) != null) {
//                sb.append(line);
//            }
//            br.close();
//            WTLogger.v(TAG, logPrefix, "response error: " + sb.toString());
        } catch (Exception e) {
            WTLogger.e(TAG, logPrefix, "Exception " + e.getMessage());
            listener.onError(e.getMessage());
        } finally {
            WTLogger.v(TAG, logPrefix, "finally close ");
            try {
                if (null != is) {
                    is.close();
                }
                if (null != dos) {
                    dos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (null != response) {
            WTLogger.v(TAG, logPrefix, "response content: " + response);
        }
    }

    private static int sendInfoDataWithLog(DataOutputStream dos, UploadInfo info, Context context) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(info.getFilePath());
            if (fis.available() <= 0) {
                WTLogger.d(TAG, "warning: fis.available() <= 0");
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                WTLogger.e(TAG, "fis close error: " + e.getMessage());
            }
            return 0;
        }

        StringBuilder sb = new StringBuilder();
        //sb.append(LINE_END);
        HashMap<String, String> params = getInfoParamsWithLog(context, info);
        WTLogger.v(TAG, "upload params: " + params);

        //build params
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);// 分界符
                sb.append("Content-Disposition: form-data; name=\""
                        + entry.getKey() + "\"" + LINE_END);
                sb.append("Content-Type: text/plain; charset="
                        + CHARSET + LINE_END);
                sb.append("Content-Transfer-Encoding: 8bit" + LINE_END);
                sb.append(LINE_END);
                sb.append(entry.getValue()).append(LINE_END);
            }
        }

        //build start field for file.
        sb.append(PREFIX).append(BOUNDARY).append(LINE_END);  // 分界符
        sb.append("Content-Disposition: form-data; name=\"logFile\"; filename=\""
                + info.getFileName() + "\"" + LINE_END);
        sb.append("Content-Type: application/octet-stream; charset="
                + CHARSET + LINE_END);     //Content-Type: application/zip
        sb.append(LINE_END);
        //WTLogger.v(TAG, "sb: " + sb.toString());

        byte[] buffer = new byte[1024];
        int len = -1;
        try {
            dos.write(sb.toString().getBytes("utf-8"));
            while ((len = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, len); //write file
            }
            dos.write(LINE_END.getBytes("utf-8"));

            //build end field for all data.
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes("utf-8");
            dos.write(end_data);
        } catch (IOException e) {
            return -1;
        } finally {
            try {
                if (null != fis) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private static int sendInfoData(DataOutputStream dos, UploadInfo info, Context context) {
        StringBuilder sb = new StringBuilder();
        //sb.append(LINE_END);
        HashMap<String, String> params = getInfoParams(context, info);
        WTLogger.v(TAG, "upload params: " + params);

        //build params
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);// 分界符
                sb.append("Content-Disposition: form-data; name=\""
                        + entry.getKey() + "\"" + LINE_END);
                sb.append("Content-Type: text/plain; charset="
                        + CHARSET + LINE_END);
                sb.append("Content-Transfer-Encoding: 8bit" + LINE_END);
                sb.append(LINE_END);
                sb.append(entry.getValue()).append(LINE_END);
            }
        }
        //WTLogger.v(TAG, "sb: " + sb.toString());

        try {
            dos.write(sb.toString().getBytes("utf-8"));
            dos.write(LINE_END.getBytes("utf-8"));
            //build end field for all data.
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes("utf-8");
            dos.write(end_data);
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }


    private static HashMap<String, String> getInfoParamsWithLog(Context context, UploadInfo info) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("uploadType", info.getUploadType());
        map.put("product", Utils.getProductName());
        map.put("swv", Utils.getSoftVersion());
        map.put("hwv", Utils.getHardwareVersion());
        map.put("sn", Utils.getSerialno());
        map.put("uuid", info.getUuid());
        map.put("packageName", info.getPackageName());
        map.put("model", info.getErrorType());
        map.put("stackInfo", info.getStackInfo1());
//        map.put("compressTime", info.getZipTime());
//        map.put("exceptionCount", String.valueOf(info.getExceptionCount()));
        int netWorkTpye = NetworkUtils.getNetWorkType(context);
        if (netWorkTpye == NetworkUtils.NETWORK_MOBILE) {
            map.put("netType", "data");
        } else if (netWorkTpye == NetworkUtils.NETWORK_WLAN) {
            map.put("netType", "wlan");
        }
        return map;
    }

    private static HashMap<String, String> getInfoParams(Context context, UploadInfo info) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("uploadType", info.getUploadType());
        map.put("product", Utils.getProductName());
        map.put("swv", info.getSoftVersion());
        map.put("hwv", Utils.getHardwareVersion());
        map.put("sn", Utils.getSerialno());
        map.put("elapsedRealtime", String.valueOf(info.getElapsedRealTime()));
        map.put("lowRAMCount", String.valueOf(info.getLowRAMCount()));

        if ("auto-report".equals(info.getUploadType())) {
            map.put("uuid", info.getUuid());
            map.put("packageName", info.getPackageName());
            map.put("model", info.getErrorType());
            map.put("stackInfo", info.getStackInfo1());
            map.put("compressTime", info.getZipTime());
            map.put("exceptionCount", String.valueOf(info.getExceptionCount()));
        }

        int netWorkTpye = NetworkUtils.getNetWorkType(context);
        if (netWorkTpye == NetworkUtils.NETWORK_MOBILE) {
            map.put("netType", "data");
        } else if (netWorkTpye == NetworkUtils.NETWORK_WLAN) {
            map.put("netType", "wlan");
        }
        return map;
    }

    public interface LogUploadListener {
        public void onFinish(int code, String res);

        public void onError(String error);
    }
}
