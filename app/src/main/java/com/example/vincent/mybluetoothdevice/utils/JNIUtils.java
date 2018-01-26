package com.example.vincent.mybluetoothdevice.utils;

import android.util.Log;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.utils
 * @class describe
 * @date 2018/1/24 13:03
 */

public class JNIUtils {

    private static final String TAG = JNIUtils.class.getSimpleName();
    private static JNIUtils instance;

    private static final int BLE_CMD_GET_SPECIFIC_ID_PACKET   = 0x10;
    private static final int BLE_CMD_GET_SYSTEM_STATUS    = 0x11;
    private static final int BLE_CMD_GET_SYSTEM_TIME     = 0x12;
    private static final int BLE_CMD_SET_SYSTEM_TIME     = 0x13;
    private static final int  BLE_CMD_GET_ALARM_ENABLE    = 0x14;
    private static final int BLE_CMD_SET_ALARM_ENABLE    = 0x15;
    private static final int BLE_CMD_GET_SYSTEM_CONFIG    = 0x16;
    private static final int  BLE_CMD_SET_SYSTEM_CONFIG    = 0x17;
    private static final int BLE_CMD_GET_SYSTEM_SUPPORT_FUNCTION  = 0x18;

    private static final int BLE_CMD_REAL_TIME_SINGLE_ECG   = 0x80;
    private static final int BLE_CMD_HISTORY_SINGLE_ECG    = 0x81;
    private static final int  BLE_CMD_SYSTEM_STATUS_REPORT   = 0x82;
    private static final int BLE_CMD_SYSTEM_TIME_REPORT    = 0x83;
    private static final int  BLE_CMD_ALARM_ENABLE_REPORT    = 0x84;
    private static final int  BLE_CMD_SYSTEM_CONFIG_REPORT   = 0x85;
    private static final int  BLE_CMD_SYSTEM_SURPPORT_FUNCTION_REPORT = 0x86;
    private static final int  BLE_CMD_PATIENT_TAG_REPORT    = 0x88;

    private static final int  BLE_CMD_MAX           = 0xFF;


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
     * 对数据判断
     * @param datas
     */
    public void judgeDataType(byte[] datas){

        if (datas[0] == (byte) 0x7f) {
            ///系统功能信息 0x86
            if (datas[3] == (byte)BLE_CMD_SYSTEM_SURPPORT_FUNCTION_REPORT) {
               analysisFromBleData0x86(datas);
            } else if (datas[3] == (byte)BLE_CMD_SYSTEM_CONFIG_REPORT) {
                //0x85
//            [self parseSystemStatus0X85:datas];
            } else if (datas[3] == (byte) BLE_CMD_ALARM_ENABLE_REPORT) {
                //0x84
//            [self parseAlertStatus0X84:datas];
            } else if (datas[3] == (byte)BLE_CMD_SYSTEM_TIME_REPORT) {
                //0x83
//            [self parseSystemTime0x83:datas];
            } else  if (datas[3] == (byte)BLE_CMD_REAL_TIME_SINGLE_ECG ||datas[3]==(byte) BLE_CMD_HISTORY_SINGLE_ECG) {
                //0x80或者0x81
            /*self.totalData = [NSMutableData data];
            //命令内容长度
          /*  int length = (int) ((datas[1] & 0xFF)| ((datas[2] & 0xFF)<<8));
//                       KMyLog(@"数据内容长度:---%d---",length);
            self.currentWaveDataLength = length;
            self.currentWaveData = [NSMutableData data];
            [self parseRealTimeWaveData:characteristic.value];*/
            }else{
                //实时心电数据
//                  [self parseRealTimeWaveData:datas];
            }
        }else{
            //解析实时数据
//        [self parseRealTimeWaveData:characteristic.value];
        }

    }


    public native void analysisFromBleData0x86(byte[] datas);

}
