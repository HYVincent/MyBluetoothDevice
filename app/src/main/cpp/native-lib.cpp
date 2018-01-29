#include <jni.h>
#include <string>
#include <string.h>
#include<android/log.h>
#include <stdlib.h>
#include <stdio.h>
#include <inttypes.h>

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

#define   LOG_TAG    "LOG_JNI"
#define   LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define   LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define   LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

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
jarray analysis0x86(jbyte *datas);

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_vincent_mybluetoothdevice_utils_JNIUtils_getSystemFunction0x18(JNIEnv *env,
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

extern "C"
JNIEXPORT char* JNICALL ConvertJByteaArrayToChars(JNIEnv *env, jbyteArray bytearray)
{
    char *chars = NULL;
    jbyte *bytes;
    bytes = env->GetByteArrayElements(bytearray, 0);
    int chars_len = env->GetArrayLength(bytearray);
    chars = new char[chars_len + 1];
    memset(chars,0,chars_len + 1);
    memcpy(chars, bytes, chars_len);
    chars[chars_len] = 0;

    env->ReleaseByteArrayElements(bytearray, bytes, 0);

    return chars;
}


/**
 * 这是解析0x86数据
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vincent_mybluetoothdevice_utils_JNIUtils_analysisFromBleData0x86(JNIEnv *env,
                                                                                  jclass thiz,
                                                                                  jbyteArray datas_,
                                                                                  jobject systemConfigInfo) {
    jbyte *datas = env->GetByteArrayElements(datas_, NULL);
    jbyte mContent[2];
    mContent[0] = datas[4];
    mContent[1] = datas[5];

    SystemConfigInfo info;
    //把jbyte转为结构体
    memcpy(&info,mContent,sizeof(SystemConfigInfo));
    env->ReleaseByteArrayElements(datas_, datas, 0);

//    char str[4];
//    str[0] = info.WaveConfig;
//    str[1] = info.Pacemaker;
//    str[2] = info.ChannelNumber;
//    str[3] = info.BreathMoni;

    jclass clazz;
    jfieldID fid;

    // mapping bar of C to foo
//    clazz = (env)->GetObjectClass(env, systemConfigInfo);
    clazz = (env)->GetObjectClass(systemConfigInfo);
    if (0 == clazz) {
//        LOGD("GetObjectClass returned");
        LOGD("获取对象失败");
    } else{
        //Java 类型     符号
//        boolean    Z
//        byte    B
//        char    C
//        short    S
//        int    I
//        long    L
//        float    F
//        double    D
//        void    V
        //设置属性
        fid = (env)->GetFieldID(clazz, "ChannelNumber", "I");
        (env)->SetIntField(systemConfigInfo,fid,info.ChannelNumber);

        fid = (env)->GetFieldID(clazz,"Pacemaker","I");
        env->SetIntField(systemConfigInfo,fid,info.Pacemaker);

        fid = (env)->GetFieldID(clazz,"BreathMoni","I");
        env->SetIntField(systemConfigInfo,fid,info.BreathMoni);

        fid = (env)->GetFieldID(clazz,"WaveConfig","I");
        env->SetIntField(systemConfigInfo,fid,info.WaveConfig);

    }

}


/**
 * 设置系统配置状态0x17
 */
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_vincent_mybluetoothdevice_utils_JNIUtils_sendSetSystemStatusWithInfo0x17(
        JNIEnv *env, jobject instance,jint BreathMoni,jint ChannelNumber,jint Pacemaker, jint WaveConfig) {

    jbyte  frameBeforeBase64 [8];
    ///命令头
    frameBeforeBase64[0] = BLE_PACKET_HEAD;
    ///命令内容长度 两个字节
    frameBeforeBase64[1] = 0x02;
    frameBeforeBase64[2] = 0x0;
    ///命令字
    frameBeforeBase64[3] =  BLE_CMD_SET_SYSTEM_CONFIG;
    ///命令内容 2个
//    NSString *content = [self toDecimalSystemWithBinarySystem:@"10000001"];
//    int value = [content intValue];
//    ///低位在前 高位在后
//     frameBeforeBase64[4]  =  (Byte) (value & 0xFF);
//     frameBeforeBase64[5] =  (Byte) ((value>>8) & 0xFF);

    SystemConfigInfo info;
    info.BreathMoni = (uint32_t )BreathMoni;
    info.ChannelNumber =(uint32_t )ChannelNumber;
    info.Pacemaker = (uint32_t )Pacemaker;
    info.WaveConfig = (uint32_t )WaveConfig;

    frameBeforeBase64[4]  =  *((uint8_t *)(&info));
    frameBeforeBase64[5] =  *((uint8_t *)(&info)+1);
    ///校验和
    uint8_t sum = frameBeforeBase64[3]+frameBeforeBase64[4]+frameBeforeBase64[5];
    ///校验和
    frameBeforeBase64[6] = (jbyte) (sum & 0xFF);
    frameBeforeBase64[7] = 0xF7;
    jbyteArray jarray = env->NewByteArray(sizeof(frameBeforeBase64));
    (env)->SetByteArrayRegion
            (jarray, 0, sizeof(frameBeforeBase64), frameBeforeBase64);
    return jarray;

}

/**
 * 设置报警开关0x15
 */
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_vincent_mybluetoothdevice_utils_JNIUtils_sendAlertSwitch0x15WithInfo0x15(
        JNIEnv *env, jobject instance, jint LowPowerAlert, jint FlashAlert, jint LeadAlert,
        jint BloothStatusAlert) {

    jbyte  frameBeforeBase64 [8];
    ///命令头
    frameBeforeBase64[0] = BLE_PACKET_HEAD;
    ///命令内容长度 两个字节
    frameBeforeBase64[1] = 0x02;
    frameBeforeBase64[2] = 0x0;
    ///命令字
    frameBeforeBase64[3] =  BLE_CMD_SET_ALARM_ENABLE;
    /*
    NSString *content = [self toDecimalSystemWithBinarySystem:bitStr];
    int value = [content intValue];
    ///低位在前 高位在后
    frameBeforeBase64[4]  =  (Byte) (value & 0xFF);
    frameBeforeBase64[5] =  (Byte) ((value>>8) & 0xFF);
    */
//    SystemAlertInfo alertInfo;

    SystemAlertInfo info;
    info.BloothStatusAlert = BloothStatusAlert;
    info.FlashAlert = FlashAlert;
    info.LeadAlert = LeadAlert;
    info.LowPowerAlert = LowPowerAlert;

    frameBeforeBase64[4]  =  *((uint8_t *)(&info));
    frameBeforeBase64[5] =  *((uint8_t *)(&info)+1);
    ///校验和
    uint8_t sum = frameBeforeBase64[3]+frameBeforeBase64[4]+frameBeforeBase64[5];

    frameBeforeBase64[6] = (jbyte) (sum & 0xFF);
    frameBeforeBase64[7] = 0xF7;
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
Java_com_example_vincent_mybluetoothdevice_utils_JNIUtils_sendSystemTime0x13(JNIEnv *env,
                                                                             jobject instance,
                                                                             jint year, jint month,
                                                                             jint day, jint hour,
                                                                             jint min, jint sec) {

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
 * 解析数据0x83
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vincent_mybluetoothdevice_utils_JNIUtils_parseSystemTime0x83(JNIEnv *env,
                                                                              jobject instance,
                                                                              jbyteArray datas_,
                                                                              jobject infoxx) {
    jbyte *datas = env->GetByteArrayElements(datas_, NULL);
    jbyte mContent[4];
    mContent[0] = datas[4];
    mContent[1] = datas[5];
    mContent[2] = datas[6];
    mContent[3] = datas[7];
    SystemTimeInfo info;
    //把jbyte转为结构体
    memcpy(&info,mContent,sizeof(SystemTimeInfo));
    env->ReleaseByteArrayElements(datas_, datas, 0);

//    char str[4];
//    str[0] = info.WaveConfig;
//    str[1] = info.Pacemaker;
//    str[2] = info.ChannelNumber;
//    str[3] = info.BreathMoni;

    jclass clazz;
    jfieldID fid;

    // mapping bar of C to foo
    //    clazz = (env)->GetObjectClass(env, systemConfigInfo);
    clazz = (env)->GetObjectClass(infoxx);
    if (0 == clazz) {
//        LOGD("GetObjectClass returned");
        LOGD("获取对象失败");
    } else{
        //Java 类型     符号
//        boolean    Z
//        byte    B
//        char    C
//        short    S
//        int    I
//        long    L
//        float    F
//        double    D
//        void    V
        //设置属性
        fid = (env)->GetFieldID(clazz, "Year", "I");
        (env)->SetIntField(infoxx,fid,info.Year);

        fid = (env)->GetFieldID(clazz,"Month","I");
        env->SetIntField(infoxx,fid,info.Month);

        fid = (env)->GetFieldID(clazz,"Day","I");
        env->SetIntField(infoxx,fid,info.Day);

        fid = (env)->GetFieldID(clazz,"Min","I");
        env->SetIntField(infoxx,fid,info.Min);

        fid = (env)->GetFieldID(clazz,"Hour","I");
        env->SetIntField(infoxx,fid,info.Hour);

        fid = (env)->GetFieldID(clazz,"Sec","I");
        env->SetIntField(infoxx,fid,info.Sec);
    }

}

/**
 * 获取报警开关状态
 */
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_vincent_mybluetoothdevice_utils_JNIUtils_getAlertStatus0x14(JNIEnv *env,
                                                                             jobject instance) {

    jbyte  frameBeforeBase64 [6];
    ///命令头
    frameBeforeBase64[0] = BLE_PACKET_HEAD;
    ///命令内容长度 两个字节
    frameBeforeBase64[1] = 0x0;
    frameBeforeBase64[2] = 0x0;
    ///命令字
    frameBeforeBase64[3] =   BLE_CMD_GET_ALARM_ENABLE;
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
 * 解析心电数据
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_vincent_mybluetoothdevice_utils_JNIUtils_parseECGData(JNIEnv *env,
                                                                       jobject instance,
                                                                       jbyteArray datas2_,
                                                                       jobject infoxx) {
    jbyte *datas2 = env->GetByteArrayElements(datas2_, NULL);
    /*jbyte ff[sizeof(datas2)];
    memcpy(&datas2, ff, sizeof(datas2));
    BTDataInfo info;
    //把jbyte转为结构体
    memcpy(&info, datas2, sizeof(SystemTimeInfo));
    env->ReleaseByteArrayElements(datas2_, datas2, 0);*/

    jclass clazz;
    jfieldID fid;
    clazz = (env)->GetObjectClass(infoxx);
    if (0 == clazz) {
        LOGD("获取对象失败");
    } else {
        //Java 类型     符号
//        boolean    Z
//        byte    B
//        char    C
//        short    S
//        int    I
//        long    L
//        float    F
//        double    D
//        void    V
        //设置属性
//        fid = (env)->GetFieldID(clazz, "Year", "I");
//        (env)->SetIntField(infoxx,fid,info.Year);

    }
}