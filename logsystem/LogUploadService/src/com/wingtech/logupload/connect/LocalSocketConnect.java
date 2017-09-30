package com.wingtech.logupload.connect;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.wingtech.logupload.utils.WTLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

public class LocalSocketConnect extends Handler {
    public static final int EVENT_SOCKET_CONNECT = 0;
    private final static String TAG = "LocalSocketConnect";
    private final static String SOCKET_NAME = "logcontrol";
    private LocalSocket mSocket;

    private ILocalSocketConnectCallbacks mCallbacks = null;

    public LocalSocketConnect(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case EVENT_SOCKET_CONNECT:
                handleStartListenSocket();
                break;
            default:
                WTLogger.e(TAG, "no message to handle");
        }
    }

    public void registerCallBacks(ILocalSocketConnectCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    private void processBuffer(byte[] buffer, int count) {
        if (count == 0) {
            return;
        }
        try {
            String input = new String(buffer, 0, count, "UTF-8");
            WTLogger.v(TAG, "Socket client receive message:" + input);
            if (null != input) {
                mCallbacks.onEvent(ILocalSocketConnectCallbacks.EVENT_LOG_INFO, input);
            }
        } catch (UnsupportedEncodingException e) {
            WTLogger.e(TAG, "processBuffer " + e.getMessage());
        }
    }

    private void handleStartListenSocket() {
        if (mSocket != null) {
            WTLogger.i(TAG, "Socket has connected");
            return;
        }

        try {
            mSocket = new LocalSocket();
            LocalSocketAddress address = new LocalSocketAddress(SOCKET_NAME,
                    LocalSocketAddress.Namespace.RESERVED);
            WTLogger.v(TAG, "Connect to socket server...");
            mSocket.connect(address);
            WTLogger.i(TAG, "Connect to socket server success.");

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    mSocket.getInputStream()));

            InputStream inputStream = mSocket.getInputStream();

            byte[] buffer = new byte[1024];
            int in = -1;
            int count = 0;

            while ((in = inputStream.read()) != -1) {
                if (in != '\n') {
                    buffer[count] = (byte) in;
                    count++;
                } else {
                    processBuffer(buffer, count);
                    count = 0;
                }

                if (count >= 1024) {
                    WTLogger.e(TAG, "Command length > " + 1024);
                    break;
                }
            }

//            String line;
//            while (true) {
//                line = br.readLine();
//                WTLogger.i(TAG, "Socket client receive message: " + line);
//                if (null != line && line.contains("ErrorType")) {
//                    mCallbacks.onEvent(ILocalSocketConnectCallbacks.EVENT_LOG_INFO,null);
//                    WTLogger.i(TAG, line);
//                }
//            }
        } catch (UnknownHostException e) {
            WTLogger.e(TAG, "Trying to connect to unknown host: " + e.getMessage());
        } catch (IOException e) {
            WTLogger.e(TAG, "IOException: " + e.getMessage());
//            e.printStackTrace();
        } finally {
            if (mSocket != null) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    mSocket = null;
                    WTLogger.e(TAG, "Finally, Client error: " + e.getMessage());
                }
            }
        }
    }

}
