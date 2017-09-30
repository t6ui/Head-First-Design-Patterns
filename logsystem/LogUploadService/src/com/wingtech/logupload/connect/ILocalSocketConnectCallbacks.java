package com.wingtech.logupload.connect;

public interface ILocalSocketConnectCallbacks {
    public static final int EVENT_LOG_INFO = 0;

    void onEvent(int event, Object obj);
}