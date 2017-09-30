package com.wingtech.logupload;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;

import com.wingtech.logupload.service.LogCollectService;
import com.wingtech.logupload.utils.Utils;
import com.wingtech.logupload.utils.WTLogger;

public class LogCollectSettingActivity extends PreferenceActivity
        implements OnPreferenceChangeListener {
    public static final String KEY_REALTIME_SCAN_INTERVAL = "key_realtime_scan_interval";
    public static final String KEY_LOWMEMORY_SCAN_INTERVAL = "key_lowmemory_scan_interval";
    public static final String KEY_UPLOAD_INTERVAL = "key_upload_interval";
    public static final String KEY_SWITCH_SERVER = "key_switch_server";
    public static final String DEFAULT_REALTIME_SCAN_INTERVAL = "15";
    public static final String DEFAULT_LOWMEMORY_SCAN_INTERVAL = "3";
    public static final String DEFAULT_UPLOAD_INTERVAL = "2";
    public static final String ACTION_STOP_COLLECT = "com.wingtech.logupload.STOP_COLLECT";
    private static final String TAG = "LogCollectSettingActivity";
    SwitchPreference mSwitchServer;
    ListPreference mRealtimeScanInterval;
    ListPreference mLowmemoryScanInterval;
    ListPreference mUploadInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.logcollect_preferences);
        initView();
    }

    private void initView() {
        mSwitchServer = (SwitchPreference) findPreference(KEY_SWITCH_SERVER);
        mSwitchServer.setOnPreferenceChangeListener(this);

        boolean logEnable = SystemProperties.getBoolean("persist.sys.pdm.enable", false);
        mSwitchServer.setChecked(logEnable);

        mRealtimeScanInterval = (ListPreference) findPreference(KEY_REALTIME_SCAN_INTERVAL);
        mRealtimeScanInterval.setSummary(mRealtimeScanInterval.getEntry());
        mRealtimeScanInterval.setOnPreferenceChangeListener(this);

        mLowmemoryScanInterval = (ListPreference) findPreference(KEY_LOWMEMORY_SCAN_INTERVAL);
        mLowmemoryScanInterval.setSummary(mLowmemoryScanInterval.getEntry());
        mLowmemoryScanInterval.setOnPreferenceChangeListener(this);

        mUploadInterval = (ListPreference) findPreference(KEY_UPLOAD_INTERVAL);
        mUploadInterval.setSummary(mUploadInterval.getEntry());
        mUploadInterval.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRealtimeScanInterval) {
            mRealtimeScanInterval.setValue((String) newValue);
            mRealtimeScanInterval.setSummary(mRealtimeScanInterval.getEntry());
            WTLogger.d(TAG, "Change real-time record period to: " + (String) newValue + " min");
        } else if (preference == mLowmemoryScanInterval) {
            mLowmemoryScanInterval.setValue((String) newValue);
            mLowmemoryScanInterval.setSummary(mLowmemoryScanInterval.getEntry());
            WTLogger.d(TAG, "Change ram scan period to: " + (String) newValue + " min");
        } else if (preference == mUploadInterval) {
            mUploadInterval.setValue((String) newValue);
            mUploadInterval.setSummary(mUploadInterval.getEntry());
            WTLogger.d(TAG, "Change upload period to: " + (String) newValue + " h");
        } else if (preference == mSwitchServer) {
            boolean logEnable = (boolean) newValue;
            mSwitchServer.setChecked(logEnable);
            WTLogger.e(TAG, "switch server: " + logEnable);
            Intent intent = new Intent(this, LogCollectService.class);
            if (logEnable) {
                Utils.initDataBase(this);
                SystemProperties.set("persist.sys.pdm.enable", "true");
                startService(intent);
            } else if (Utils.isRunningService(this, "com.wingtech.logupload.service.LogCollectService")) {
                SystemProperties.set("persist.sys.pdm.enable", "false");
                intent.setAction(ACTION_STOP_COLLECT);
                startService(intent);
            } else {
                SystemProperties.set("persist.sys.pdm.enable", "false");
                WTLogger.d(TAG, "LogCollectService is not running. Don't need to stop.");
            }
        } else {
            return true;
        }
        return false;
    }


}
