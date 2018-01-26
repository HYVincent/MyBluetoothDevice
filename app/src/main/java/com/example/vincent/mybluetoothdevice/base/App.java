package com.example.vincent.mybluetoothdevice.base;

import android.app.Application;

import com.example.vincent.mybluetoothdevice.utils.SPUtil;
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

    private static SPUtil spUtil;

    public static SPUtil getSpUtil() {
        return spUtil;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        CrashReport.initCrashReport(getApplicationContext());
        CrashReport.initCrashReport(getApplicationContext(), "b26ee9496b", false);
        spUtil = SPUtil.getInstance(this,"MyBluetoothDevice");
    }
}
