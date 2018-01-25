package com.example.vincent.mybluetoothdevice.utils;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.utils
 * @class describe
 * @date 2018/1/24 13:03
 */

public class JNIUtils {

    private static JNIUtils instance;

    public static JNIUtils getInstance() {
        if(instance == null){
            instance = new JNIUtils();
        }
        return instance;
    }

    static {
        System.loadLibrary("native-lib");
    }

    /**
     * 发送数据，获取系统功能
     * @return
     */
    public native byte[] getSystemFunction();

    /**
     * 设置系统时间
     * @param year 年
     * @param month 月
     * @param day 日
     * @param hour 时
     * @param min 分
     * @param sec 秒
     * @return
     */
    public native byte[] setSystemTime(int year, int month, int day, int hour, int min, int sec);

    /**
     * 解析数据
     * @param datas
     * @return
     */
    public native byte[] analysisFromBleData(byte[] datas);





}
