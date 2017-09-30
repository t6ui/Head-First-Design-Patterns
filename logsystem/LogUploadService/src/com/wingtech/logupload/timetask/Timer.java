package com.wingtech.logupload.timetask;

import android.content.Context;
import android.os.Handler;

import com.wingtech.logupload.utils.SharePreferenceUtils;

public abstract class Timer {
    protected SharePreferenceUtils spUtils = null;
    protected Handler mHandler = null;
    protected Context mContext = null;

    protected Timer(Context context, Handler handler) {
        spUtils = SharePreferenceUtils.newInstance(context);
        mContext = context;
        mHandler = handler;
    }

    public abstract void handleTimeOut(Handler handler);

    public abstract void resetPeriodTimer(long timeout);

    public void startTimer() {
        resetPeriodTimer(-1);
    }

    public abstract void cancelTimer();

}
