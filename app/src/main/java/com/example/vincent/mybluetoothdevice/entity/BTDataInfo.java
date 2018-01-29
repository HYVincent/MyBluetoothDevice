package com.example.vincent.mybluetoothdevice.entity;

/**
 * @author Administrator QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.entity
 * @class describe
 * @date 2018/1/27 17:35
 */

public class BTDataInfo {

    ///数据包 ID
    public int PacketId;
    ///系统时间和状态
    public TimeAndSysStatus TimeSysStatus;
    ///pace标记
    public int PaceFlag;
    ///原始数据位置标记
    public int RawDataFlag;

    public int Data;
    public int DataLen;
    
}
