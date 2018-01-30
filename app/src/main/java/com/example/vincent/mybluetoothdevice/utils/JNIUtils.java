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
     * 对数据判断
     * @param datas
     */
    public void judgeDataType(byte[] datas){
        getOneFullData(datas);

        /*if (datas[0] == (byte) 0x7f) {
            ///系统功能信息 0x86
            if (datas[3] == (byte)BLE_CMD_SYSTEM_SURPPORT_FUNCTION_REPORT) {
                SystemConfigInfo info = new SystemConfigInfo();
                analysisFromBleData0x86(datas,info);
                Log.d(TAG, "judgeDataType: "+info.getChannelNumber());
            } else if (datas[3] == (byte)BLE_CMD_SYSTEM_CONFIG_REPORT) {
                //0x85
//            [self parseSystemStatus0X85:datas];
            } else if (datas[3] == (byte) BLE_CMD_ALARM_ENABLE_REPORT) {
                //0x84
//            [self parseAlertStatus0X84:datas];
            } else if (datas[3] == (byte)BLE_CMD_SYSTEM_TIME_REPORT) {
                //0x83
                SystemTimeInfo info = new SystemTimeInfo();
                parseSystemTime0x83(datas,info);
                Log.d(TAG, "judgeDataType 000: "+ JSON.toJSONString(info));
            } else  if (datas[3] == (byte)BLE_CMD_REAL_TIME_SINGLE_ECG ||datas[3]==(byte) BLE_CMD_HISTORY_SINGLE_ECG) {
                //0x80或者0x81 解析实时数据或者是历史数据
//                parseRealTimeWaveData(datas);

            }else{
                //实时心电数据
//                  [self parseRealTimeWaveData:datas];
                jiexiData(datas);
            }
        }else{
            //解析实时数据
//        [self parseRealTimeWaveData:characteristic.value];
//            parseRealTimeWaveData(datas);
            jiexiData(datas);
        }*/

    }
    //存放接收到的所有数据
    private  List<Byte> allData = new ArrayList<>();
    //存放一个完整的命令内容
    private  List<Byte> contentDatas = new ArrayList<>();

    //在allData中找到第一个0x7f的位置
    private int index0x7f = -1;
    //表示命令内容的长度
    private int contentLength = -1;
    //命令字
    private byte commandWord;
    //结束下标
    private int index0xf7 = -1;

    /**
     * 解析数据
     * @param datas
     */
    private void getOneFullData(byte[] datas) {
        //把datas添加到allData中
        addAllDatas(datas);
        if(contentDatas.size() == 0){
            find0x7fInAllData();
        }
        //找到0x7f了
        if(index0x7f != -1){
            //已找到0x7f的位置，在0x7f之前的数据丢弃
            removeIndexBeforeItem(index0x7f);
            //判断数据长度大于3并且f7的下标没找到才去找，找到了就不找了
            if(allData.size()>3){
                //得到数据内容的长度 因为是高位在前 低位在后 长度占两个字节，0为0x7f标志位，1、2两个
                contentLength = getContentLength(allData.get(index0x7f+1),allData.get(index0x7f+2));
                Log.d(TAG, "jiexiData:contentLength="+contentLength);
                //命令字的内容
                commandWord = allData.get(index0x7f + 3);
                index0xf7 = index0x7f + 4+ contentLength + 2 -1;
            }
            if(allData.size()>index0xf7){
                Log.d(TAG, "jiexiData: 所有数据："+JSONArray.toJSONString(allData));
                //表示已经接收完了一个完整的数据了，
                Log.d(TAG, "jiexiData: index0xf7 = "+allData.get(index0xf7-1));
                //判断一下命令尾部是否正确 247表示为16进制的 0xf7
                if(allData.get(0) == (byte)0x7f && allData.get(index0xf7) == (byte)0xf7  ){
                    for (int i = index0x7f;i<index0xf7+1;i++){
                        contentDatas.add(allData.get(i));
                    }
                    Log.d(TAG, "jiexiData: 解析数据..");
                    //解析命令内容
                    parseContentData(contentDatas);
                    removeIndexBeforeItem(index0xf7+1);
                    //解析完了要清空
                    Log.d(TAG, "jiexiData: 满了，清除contentDatas");
                    contentDatas.clear();
                    index0xf7 = -1;
                }
            }
        }
    }

    /**
     * 解析内容数据
     * @param datas
     */
    private void parseContentData(List<Byte> datas) {
        byte[] bytes = new byte[datas.size()];
        for (int i = 0;i<datas.size();i++){
            bytes[i] = datas.get(i);
        }
        ///系统功能信息 0x86
        if (datas.get(3) == BLE_CMD_SYSTEM_SURPPORT_FUNCTION_REPORT) {
            SystemConfigInfo info = new SystemConfigInfo();
            analysisFromBleData0x86(bytes,info);
            Log.d(TAG, "judgeDataType: "+info.getChannelNumber());
        } else if (datas.get(3) == (byte)BLE_CMD_SYSTEM_CONFIG_REPORT) {
            //0x85
//            [self parseSystemStatus0X85:datas];
        } else if (datas.get(3) == (byte) BLE_CMD_ALARM_ENABLE_REPORT) {
            //0x84
//            [self parseAlertStatus0X84:datas];
            SystemAlertInfo info = new SystemAlertInfo();
            parseAlertStatus0X84(bytes,info);
        } else if (datas.get(3) == (byte)BLE_CMD_SYSTEM_TIME_REPORT) {
            //0x83
            SystemTimeInfo info = new SystemTimeInfo();
            parseSystemTime0x83(bytes,info);
            Log.d(TAG, "judgeDataType 000: "+ JSON.toJSONString(info));
        } else  if (datas.get(3) == (byte)BLE_CMD_REAL_TIME_SINGLE_ECG ||datas.get(3)==(byte) BLE_CMD_HISTORY_SINGLE_ECG) {
            //0x80或者0x81 解析实时数据或者是历史数据
            BTDataInfo info = new BTDataInfo();
            parseECGData(bytes,info);

        }else{
            //实时心电数据

        }
        Log.d(TAG, "parseContentData: fffff");
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
     * 移除0x7f之前的所有数据
     * @param index
     * @return
     */
    private void removeIndexBeforeItem(int index){
        for (int i = 0;i<index;i++){
            allData.remove(i);
        }
        //现在0x7f排在第一位了
        index0x7f = 0;
    }

    /**
     * 在allData中寻找0x7f(数据头) 找到就不找了
     * @return
     */
    private void find0x7fInAllData(){
        index0x7f = -1;
        for (int i = 0;i<allData.size();i++){
            if(allData.get(i) == (byte)0x7f){//127 为16进制0x7f
                index0x7f = i;
                Log.d(TAG, "find0x7fInAllData: index0x7f = "+i);
                break;
            }
        }
    }

    /**
     * 判断数据是否包含头尾
     * @return
     */
    private boolean hasHeadOrEnd(){
        for (int i = 0;i<allData.size();i++){
            if(allData.get(i) == 0x7f||allData.get(i) == 0xf7){
                return true;
            }
        }
        return false;
    }

    /**
     * 把接受到的数据放到allData中
     * @param datas
     */
    private void addAllDatas(byte[] datas) {
        for (byte b:datas){
            allData.add(b);
        }
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
