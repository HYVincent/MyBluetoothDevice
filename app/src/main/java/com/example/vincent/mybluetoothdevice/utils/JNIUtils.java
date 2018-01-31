package com.example.vincent.mybluetoothdevice.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.example.vincent.mybluetoothdevice.bluetooth.BleControl;
import com.example.vincent.mybluetoothdevice.entity.BTDataInfo;
import com.example.vincent.mybluetoothdevice.entity.SystemAlertInfo;
import com.example.vincent.mybluetoothdevice.entity.SystemConfigInfo;
import com.example.vincent.mybluetoothdevice.entity.SystemTimeInfo;

import java.util.ArrayList;
import java.util.List;

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

    private static final byte BLE_CMD_GET_SPECIFIC_ID_PACKET   = 0x10;
    private static final byte BLE_CMD_GET_SYSTEM_STATUS    = 0x11;
    private static final byte BLE_CMD_GET_SYSTEM_TIME     = 0x12;
    private static final byte BLE_CMD_SET_SYSTEM_TIME     = 0x13;
    private static final byte  BLE_CMD_GET_ALARM_ENABLE    = 0x14;
    private static final byte BLE_CMD_SET_ALARM_ENABLE    = 0x15;
    private static final byte BLE_CMD_GET_SYSTEM_CONFIG    = 0x16;
    private static final byte  BLE_CMD_SET_SYSTEM_CONFIG    = 0x17;
    private static final byte BLE_CMD_GET_SYSTEM_SUPPORT_FUNCTION  = 0x18;

    private static final byte BLE_CMD_REAL_TIME_SINGLE_ECG   = (byte)0x80;
    private static final byte BLE_CMD_HISTORY_SINGLE_ECG    = (byte)0x81;
    private static final byte  BLE_CMD_SYSTEM_STATUS_REPORT   = (byte)0x82;
    private static final byte BLE_CMD_SYSTEM_TIME_REPORT    = (byte)0x83;
    private static final byte  BLE_CMD_ALARM_ENABLE_REPORT    = (byte)0x84;
    private static final byte  BLE_CMD_SYSTEM_CONFIG_REPORT   = (byte)0x85;
    private static final byte  BLE_CMD_SYSTEM_SURPPORT_FUNCTION_REPORT = (byte)0x86;
    private static final byte  BLE_CMD_PATIENT_TAG_REPORT    = (byte)0x88;

    private static final byte  BLE_CMD_MAX           = (byte)0xFF;

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
    public native byte[] getSystemFunction0x18();

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
    public native byte[] sendSystemTime0x13(int year, int month, int day, int hour, int min, int sec);

    /**
     * 所有数据
     */
    private List<Byte> totalData = new ArrayList();

    /**
     * 清空数据
     */
    public  void clearTotalData(){
        if(totalData != null && totalData.size()>0){
            totalData.clear();
        }
    }

    private int newHeaderIndex = 0;


    /**
     * 对数据判断
     * @param datas
     */
    public void judgeDataType(byte[] datas){
        for(byte b:datas){
            totalData.add(b);
        }
        if(totalData.size() == 0){
            return;
        }

        if (totalData.size() > 0) {

            for (int index = newHeaderIndex; index < totalData.size();index++) {
                if (totalData.get(index) == (byte) 0x7f) {
                    newHeaderIndex = index;
                    int length = 0;
                    if (totalData.size() - index > 3) {
                        length = (int) ((totalData.get(index + 1) & 0xFF) | ((totalData.get(index + 2) & 0xFF) << 8));
                    } else {
                        break;
                    }
                    if (totalData.size() >= index + length + 5) {
                        byte tail = totalData.get(index + length + 5);
                        if (tail == (byte) 0xf7) {
                            //找到了命令尾
                            //找到了命令尾
                            byte[] totalByte = new byte[length + 6];
                            for (int i = index; i < length + 6; i++) {
                                totalByte[i - index] = totalData.get(i);
                            }
                            parseContentData(totalByte);
                            List<Byte> remainingByte = new ArrayList<>();
                            for (int i = index + length + 6; i < totalData.size(); i++) {
                                remainingByte.add(totalData.get(i));
                            }
                            totalData.clear();
                            totalData.addAll(remainingByte);
                            byte[] bytes = new byte[0];
                            newHeaderIndex = 0;
                            judgeDataType(bytes);
                            break;
                        } else {
                            newHeaderIndex += 1;
                            byte[] bytes = new byte[0];
                            judgeDataType(bytes);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }




    /**
     * 解析内容数据
     * @param datas
     */
    private void parseContentData(byte[] datas) {
        ///系统功能信息 0x86
        if (datas[3] == BLE_CMD_SYSTEM_SURPPORT_FUNCTION_REPORT) {
            SystemConfigInfo info = new SystemConfigInfo();
            analysisFromBleData0x86(datas,info);
            Log.d(TAG, "judgeDataType: "+info.getChannelNumber());
        } else if (datas[3] == (byte)BLE_CMD_SYSTEM_CONFIG_REPORT) {
            //0x85
//            [self parseSystemStatus0X85:datas];
        } else if (datas[3] == (byte) BLE_CMD_ALARM_ENABLE_REPORT) {
            //0x84
//            [self parseAlertStatus0X84:datas];
            SystemAlertInfo info = new SystemAlertInfo();
            parseAlertStatus0X84(datas,info);
        } else if (datas[3] == (byte)BLE_CMD_SYSTEM_TIME_REPORT) {
            //0x83
            SystemTimeInfo info = new SystemTimeInfo();
            parseSystemTime0x83(datas,info);
            Log.d(TAG, "judgeDataType 000: "+ JSON.toJSONString(info));
        } else  if (datas[3] == (byte)BLE_CMD_REAL_TIME_SINGLE_ECG ||datas[3]==(byte) BLE_CMD_HISTORY_SINGLE_ECG) {
            //0x80或者0x81 解析实时数据或者是历史数据
            int ecgLength = getContentLength(datas[1],datas[2]);
            byte[] ecgContent = new byte[ecgLength];
            for (int i=0;i<ecgLength;i++){
                ecgContent[i] = datas[i+4];
            }
            HeartPackage heartPackage = new HeartPackage(ecgContent);
            Log.d(TAG, "parseContentData: 心电数据ID:"+JSON.toJSONString(heartPackage.getPackageId()));
        }
    }

    /**
     * 解析报警开关数据
     * @param datas
     * @param info
     */
    public native void parseAlertStatus0X84(byte[] datas, SystemAlertInfo info);

    /**
     * 根据两个字节值获取长度
     */
    public static int getContentLength(byte b1,byte b2)
    {
        return (b2&0xff)<< 8 | (b1&0xff);
    }





    /**
     * 解析数据类型0x83
     * @param datas
     * @param info
     */
    private native void parseSystemTime0x83(byte[] datas, SystemTimeInfo info);

    /**
     * 解析数据0x86类型
     * @param datas
     * @param info
     */
    public native void analysisFromBleData0x86(byte[] datas, SystemConfigInfo info);

    /**
     * 设置系统配置状态0x17
     * @return
     */
    public native byte[] sendSetSystemStatusWithInfo0x17(int BreathMoni,int ChannelNumber,int Pacemaker, int WaveConfig);

    /**
     *  设置报警开关0x15
     * @return
     */
    public native byte[] sendAlertSwitch0x15WithInfo0x15(int LowPowerAlert,int FlashAlert,int LeadAlert,int BloothStatusAlert);

    /**
     * 获取报警开关状态
     * @return
     */
    public native byte[] getAlertStatus0x14();

    /**
     * 解析数据
     * @param datas2
     * @param info
     */
    private native void parseECGData(byte[] datas2, BTDataInfo info);



}
