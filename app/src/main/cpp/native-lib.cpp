#include <jni.h>
#include <string>
#include <string.h>

///宏定义
///一次数据的长度
#define  BT_DATA_LEN      (125) //
///最大原始数据个数
#define  RAW_DATA_MAX_COUNT    (30) //125
///pace 标记数据
#define  PACE_FLAG_LEN     (16)
///原始数据位置标记
#define  RAWDATA_FLAG_LEN    (16)
///存储了多少 pace 数据
#define  PAGE_SAVE_NUM     (5)
///没有数据时候的长度
#define  BLE_PACKET_LEN_WITHOUT_DATA  (44)

///命令字宏定义
typedef enum{
    BLE_CMD_GET_SPECIFIC_ID_PACKET   = 0x10,
    BLE_CMD_GET_SYSTEM_STATUS    = 0x11,
    BLE_CMD_GET_SYSTEM_TIME     = 0x12,
    BLE_CMD_SET_SYSTEM_TIME     = 0x13,
    BLE_CMD_GET_ALARM_ENABLE    = 0x14,
    BLE_CMD_SET_ALARM_ENABLE    = 0x15,
    BLE_CMD_GET_SYSTEM_CONFIG    = 0x16,
    BLE_CMD_SET_SYSTEM_CONFIG    = 0x17,
    BLE_CMD_GET_SYSTEM_SUPPORT_FUNCTION  = 0x18,

    BLE_CMD_REAL_TIME_SINGLE_ECG   = 0x80,
    BLE_CMD_HISTORY_SINGLE_ECG    = 0x81,
    BLE_CMD_SYSTEM_STATUS_REPORT   = 0x82,
    BLE_CMD_SYSTEM_TIME_REPORT    = 0x83,
    BLE_CMD_ALARM_ENABLE_REPORT    = 0x84,
    BLE_CMD_SYSTEM_CONFIG_REPORT   = 0x85,
    BLE_CMD_SYSTEM_SURPPORT_FUNCTION_REPORT = 0x86,
    BLE_CMD_PATIENT_TAG_REPORT    = 0x88,

    BLE_CMD_MAX           = 0xFF,
}BlePacketCmd;
///协议头长
#define BLE_PACKET_HEAD_SIZE  (1)
///尾长
#define BLE_PACKET_TAIL_SIZE  (1)
///校验字长
#define BLE_PACKET_CRC_SIZE   (1)
///命令字长
#define BLE_PACKET_CMD_SIZE   (1)
///内容长度
#define BLE_PACKET_LEN_SIZE   (2)
//命令最小长度
#define BLE_PACKET_MIN_LEN   (6)
//最大长度
#define BLE_PACKET_MAX_LEN   (250)
//命令头
#define BLE_PACKET_HEAD    (0x7F)
//命令尾
#define BLE_PACKET_TAIL    (0xF7)

///系统报警设置
typedef  struct SystemAlertInfo{
    uint32_t LowPowerAlert : 2;
    uint32_t FlashAlert : 2;
    uint32_t LeadAlert : 2;
    uint32_t BloothStatusAlert: 1;
}SystemAlertInfo;

///系统配置
typedef  struct SystemConfigInfo{
    ///通道数
    uint32_t ChannelNumber : 4;
    ///起搏
    uint32_t Pacemaker : 2;
    ///呼吸监测
    uint32_t BreathMoni : 2;
    ///工频陷波
    uint32_t WaveConfig: 2;
}SystemConfigInfo;
///系统时间
typedef  struct SystemTimeInfo{
    uint32_t Year : 6;
    uint32_t Month : 4;
    uint32_t Day : 5;
    uint32_t Hour: 5;
    uint32_t Min : 6;
    uint32_t Sec : 6;
}SystemTimeInfo;

/// 系统状态
typedef struct{

    uint32_t ResvTime  : 8;
    ///导联状态
    uint32_t LeadOff   : 2;
    ///不适标记
    uint32_t PatientFlag : 1; //1是有标记 0是没有标记。
    ///PACE 标记
    uint32_t PaceEnableSta : 1;
    //工频陷波状态
    uint32_t PowerFreqSta : 2;
    ///预留
    uint32_t Resv    : 18;
}SystemDataStatus;

///波形时间
typedef struct{
    uint32_t Year : 6;
    uint32_t Month : 4;
    uint32_t Day : 5;
    uint32_t Hour: 5;
    uint32_t Min : 6;
    uint32_t Sec : 6;
    uint32_t Msec : 8;
    uint32_t Resv : 24;
}TimeInfo;

//时间和系统配置 8个字节
typedef union
{
    ///系统时间
    TimeInfo Time;
    struct{
        uint32_t Resv;
        SystemDataStatus SysSta;
    }Status;
} TimeAndSysStatus;

typedef struct{
    ///数据包 ID
    uint32_t PacketId;
    ///系统时间和状态
    TimeAndSysStatus TimeSysStatus;
    ///pace标记
    uint8_t PaceFlag[PACE_FLAG_LEN];
    ///原始数据位置标记
    uint8_t RawDataFlag[RAWDATA_FLAG_LEN];

    int8_t Data[1][BT_DATA_LEN+RAW_DATA_MAX_COUNT];
    uint8_t DataLen;
}BTDataInfo;



/**
 * 发送获取系统功能
 */
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_vincent_mybluetoothdevice_utils_JNIUtils_getSystemFunction(JNIEnv *env,
                                                                            jobject instance) {

    jbyte  frameBeforeBase64 [6];
    ///命令头
    frameBeforeBase64[0] = BLE_PACKET_HEAD;
    ///命令内容长度 两个字节
    frameBeforeBase64[1] = 0x0;
    frameBeforeBase64[2] = 0x0;
    ///命令字
    frameBeforeBase64[3] =  BLE_CMD_GET_SYSTEM_SUPPORT_FUNCTION;
    ///命令内容 无
    ///校验和
    frameBeforeBase64[4] = frameBeforeBase64[3];
    frameBeforeBase64[5] = 0xF7;
    jbyteArray jarray = env->NewByteArray(sizeof(frameBeforeBase64));
    (env)->SetByteArrayRegion
            (jarray, 0, sizeof(frameBeforeBase64), frameBeforeBase64);
    return jarray;
}

/**
 * 调用此方法设置时间
 * @param env
 * @param year  年
 * @param month  月
 * @param day 日
 * @param hour 时
 * @param min 分
 * @param sec 秒
 * @return
 */
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_vincent_mybluetoothdevice_utils_JNIUtils_setSystemTime(JNIEnv *env,
                                                                        jobject instance, jint year,
                                                                        jint month, jint day,
                                                                        jint hour, jint min,
                                                                        jint sec) {
    jbyte  frameBeforeBase64 [10];
    ///命令头
    frameBeforeBase64[0] = BLE_PACKET_HEAD;
    ///命令内容长度 两个字节
    frameBeforeBase64[1] = 0x04;
    frameBeforeBase64[2] = 0x0;
    ///命令字
    frameBeforeBase64[3] =  BLE_CMD_SET_SYSTEM_TIME;

    SystemTimeInfo timeInfo;
    timeInfo.Year = year;
    timeInfo.Month = month;
    timeInfo.Day = day;
    timeInfo.Hour = hour;
    timeInfo.Min = min;
    timeInfo.Sec = sec;

    frameBeforeBase64[4] =  *((uint8_t *)(&timeInfo));
    frameBeforeBase64[5] =  *((uint8_t *)(&timeInfo)+1);
    frameBeforeBase64[6] = *((uint8_t *)(&timeInfo)+2);
    frameBeforeBase64[7] = *((uint8_t *)(&timeInfo)+3);
    ///校验和
    uint8_t sum = frameBeforeBase64[3]+frameBeforeBase64[4]+frameBeforeBase64[5]+frameBeforeBase64[6]+frameBeforeBase64[7];
    frameBeforeBase64[8] = (jbyte) ((sum) & 0xFF);
    frameBeforeBase64[9] = 0xF7;
    jbyteArray jarray = env->NewByteArray(sizeof(frameBeforeBase64));
    (env)->SetByteArrayRegion
            (jarray, 0, sizeof(frameBeforeBase64), frameBeforeBase64);
    return jarray;
}
/**
 * 数据解析
 */
extern "C"
JNIEXPORT jbyteArray* JNICALL
Java_com_example_vincent_mybluetoothdevice_utils_JNIUtils_analysisFromBleData(JNIEnv *env,
                                                                              jobject instance,
                                                                              jbyteArray datas_) {
    jbyte *datas = env->GetByteArrayElements(datas_, NULL);

    // 所有协议均以0x7f 开头
    if (datas[0] == 0x7f) {
        ///系统功能信息
        if (datas[3] == BLE_CMD_SYSTEM_SURPPORT_FUNCTION_REPORT) {
            jbyte  infoByte [2];
            infoByte[0] = datas[4];
            infoByte[1] = datas[5];
            SystemConfigInfo info;
        //    info.ChannelNumber = 1;
        //    info.Pacemaker = 1;
        //    info.BreathMoni =2;
        //    info.WaveConfig = 1;
//            memcpy(&info,infoByte,sizeof(SystemConfigInfo));
//            printf("ffffffff");
        } else if (datas[3] == BLE_CMD_SYSTEM_CONFIG_REPORT) {
//            [self parseSystemStatus0X85:datas];
        }
        else if (datas[3] ==  BLE_CMD_ALARM_ENABLE_REPORT) {
//            [self parseAlertStatus0X84:datas];
        }
        else if (datas[3] == BLE_CMD_SYSTEM_TIME_REPORT) {
//            [self parseSystemTime0x83:datas];
        }
        else  if (datas[3] == BLE_CMD_REAL_TIME_SINGLE_ECG ||datas[3]==BLE_CMD_HISTORY_SINGLE_ECG) {
            /*self.totalData = [NSMutableData data];
            //命令内容长度
            int length = (int) ((datas[1] & 0xFF)| ((datas[2] & 0xFF)<<8));
//                       KMyLog(@"数据内容长度:---%d---",length);
            self.currentWaveDataLength = length;
            self.currentWaveData = [NSMutableData data];
            [self parseRealTimeWaveData:characteristic.value];*/
        }else{
//                  [self parseRealTimeWaveData:datas];
        }
    }else{
//        [self parseRealTimeWaveData:characteristic.value];
    }
    env->ReleaseByteArrayElements(datas_, datas, 0);
}