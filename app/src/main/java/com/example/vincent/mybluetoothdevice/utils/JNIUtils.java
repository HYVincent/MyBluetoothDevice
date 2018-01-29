package com.example.vincent.mybluetoothdevice.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.vincent.mybluetoothdevice.bluetooth.BleControl;
import com.example.vincent.mybluetoothdevice.entity.BTDataInfo;
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
        if (datas[0] == (byte) 0x7f) {
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
                parseRealTimeWaveData(datas);
            }else{
                //实时心电数据
//                  [self parseRealTimeWaveData:datas];
            }
        }else{
            //解析实时数据
//        [self parseRealTimeWaveData:characteristic.value];
            parseRealTimeWaveData(datas);
        }

    }
    /**
     * 波形数字
     */
    private List<Byte> allData = new ArrayList<>();
    private List<Byte> currentWaveData = new ArrayList<>();
    private int currentWaveDataLength;

    /**
     * 解析心电图数据
     * @param datas
     */
    private void parseRealTimeWaveData(byte[] datas) {
        //把数据添加进去
        for (int i = 0;i<datas.length;i++){
            allData.add(datas[i]);
        }
        if (currentWaveData == null) {
            currentWaveData = new ArrayList<>();
        }
        if (currentWaveData.size() < currentWaveDataLength+6) {
            for (int i = 0;i<datas.length;i++){
                currentWaveData.add(datas[i]);
            }
        }

        if (currentWaveData != null && currentWaveData.size() > 3) {
            if (currentWaveData.get(0) == (byte)0x7f && (currentWaveData.get(3)== (byte)BLE_CMD_REAL_TIME_SINGLE_ECG ||(currentWaveData.get(3)== (byte)BLE_CMD_HISTORY_SINGLE_ECG))){
                //正确的包头。
                int length = (int) ((currentWaveData.get(1) & 0xFF)| ((currentWaveData.get(2) & 0xFF)<<8));
                currentWaveDataLength = length;
            }else{
                searchDataHeader();
            }
        }

        if (currentWaveData.size() >= currentWaveDataLength+6) {
//         粘包
            Integer leng =  currentWaveDataLength+6;
            Byte[] tempByte = new Byte[leng];
//            Byte  *allByte =(Byte *) [currentWaveData bytes];
            for (int i = 0; i < leng ; i++) {
                tempByte[i] = currentWaveData.get(i);
            }
            //总的长度里是否以标识位结尾
            if (tempByte[leng-1] != (byte) 0xf7) {
                ///监测是否有包头
                searchDataHeader();
                return;
            }
            ///保存本地数据
            currentWaveData.clear();
            for (int i=0;i<tempByte.length;i++){
                currentWaveData.add(tempByte[i]);
            }
            ///截取之后做解析。
            //解析数据包
//        allByte =(Byte *) [currentWaveData bytes];
            Byte[]  contentBytes = new Byte[currentWaveDataLength];
            for (int i = 0; i < currentWaveDataLength; i++) {
                contentBytes[i] = currentWaveData.get(i+4);
            }
            //TODO 解析数据
            byte[]  datas2 = new byte[currentWaveData.size()];
            for (int i =0;i<currentWaveData.size();i++){
                if(currentWaveData != null && currentWaveData.size()>0){
                    datas2[i] = currentWaveData.get(i);
                }
            }
            BTDataInfo info = new BTDataInfo();
            parseECGData(datas2,info);
            Log.d(TAG, "parseRealTimeWaveData: info-->"+info.PacketId);
            Log.d(TAG, "parseRealTimeWaveData: "+JSON.toJSONString(currentWaveData));
            Log.d(TAG, "parseRealTimeWaveData: ....");
     /*     BTDataInfo dataInfo;
            memcpy(&dataInfo,contentBytes,sizeof(BTDataInfo));
//        NSData *pointData = [NSData dataWithBytes:dataInfo.Data length:sizeof(dataInfo.Data)];

            //TODO 发送消息 确认收到
            BleControl.getInstance().writeBuffer();

        [self confirmWaveDataReceiveSuccessWithDataId:dataInfo.PacketId andType:tempByte[3]];
            dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
            dispatch_async(queue, ^{
            ///注意这个是否正确
            ///数据处理放到异步线程
            if (allByte[3] == BLE_CMD_HISTORY_SINGLE_ECG) {
                ///历史数据下标加1
                currentHistoryIndex +=1;
                NSString *numStr =  [NSString stringWithFormat:@"%d",dataInfo.PacketId];
                if (![haveSendHistoryIDArr containsObject:numStr]) {
                 [haveSendHistoryIDArr addObject:numStr];
                }
                //添加到本地数组
            [self addSaveModelArrWithDataInfo:dataInfo];
            [self reGetWaveDateWithArr:needSendHistoryIDArr];
            }else{
                ///解析数据后添加到显示数组
         [self parsingTheRawDataWithDataInfo:dataInfo];
         [self addSaveModelArrWithDataInfo:dataInfo];
            }
        });
*/
            currentWaveData .clear();
            currentWaveData = new ArrayList<>();
            ///粘包中数据拆分进行下一次解析
            ///获取截取位置
            int subIndex = 0;
            for (int i = 0; i < datas.length-1; i ++) {
                if (datas[i] == 0xf7 && datas[i+1] == 0x7f) {
                    subIndex = i+1;
                }
            }
            Byte[] subBytes = new Byte[datas.length-subIndex];
            for (int i = 0; i < datas.length-1; i ++) {
                subBytes[i] = datas[subIndex+i];
            }
            if (subBytes.length < 3) {
                ///直接拼接
                for (int i = 0;i<subBytes.length;i++){
                    currentWaveData.add(subBytes[i]);
                }
                return;
            }
            if (subBytes[0] == 0x7f && (subBytes[3]==BLE_CMD_REAL_TIME_SINGLE_ECG || subBytes[3]==BLE_CMD_HISTORY_SINGLE_ECG)) {
                ///获取长度
                int length = (int) ((subBytes[1] & 0xFF)| ((subBytes[2] & 0xFF)<<8));
                currentWaveDataLength = length;
                currentWaveData.clear();
                for (int i = 0;i<subBytes.length;i++){
                    currentWaveData.add(subBytes[i]);
                }
            }
        }
    }


    /**
     * 搜索包头
     */
    private void searchDataHeader() {
        //包头不正确时
        //循环检索是否有包头 如果有，则表示为包头的下标值
        int subIndex = -1;
        if(currentWaveData == null){
            return;
        }
        for (int i = 0; i < currentWaveData.size()-1; i ++) {
            byte values = currentWaveData.get(i);
            if (values == 0x7f  ) {
                subIndex = i;
            }
        }
        ///检测到了包头
        if (subIndex != -1) {
            Byte[] subBytes = new Byte[currentWaveData.size()-subIndex];
            for (int i = 0; i < subBytes.length-1; i ++) {
                subBytes[i]  = currentWaveData.get(subIndex+i);
            }
            ///重新拼接
            currentWaveData.clear();
            for (int i = 0;i<subBytes.length;i++){
                currentWaveData.add(subBytes[i]);
            }
        }else{
            currentWaveData.clear();
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
