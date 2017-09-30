package com.wingtech.logupload.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    public static final int NETWORK_UNKNOWN = -1;
    public static final int NETWORK_MOBILE = 0;
    public static final int NETWORK_WLAN = 1;
    public static final int NO_NETWORK = 2;

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getNetWorkType(Context context) {
        ConnectivityManager connectManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        int netWorkType = NETWORK_UNKNOWN;
        NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.isConnected()) {
                int type = networkInfo.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {
                    netWorkType = NETWORK_WLAN;
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    netWorkType = NETWORK_MOBILE;
                }
            } else {
                netWorkType = NO_NETWORK;
            }
        }
        return netWorkType;
    }

}
