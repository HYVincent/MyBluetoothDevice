package com.example.vincent.mybluetoothdevice;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice
 * @class describe
 * @date 2018/1/25 11:03
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        CrashReport.initCrashReport(getApplicationContext());
        CrashReport.initCrashReport(getApplicationContext(), "b26ee9496b", false);
    }
}
