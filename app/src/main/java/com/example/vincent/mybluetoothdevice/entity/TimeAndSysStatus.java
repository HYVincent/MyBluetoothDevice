package com.example.vincent.mybluetoothdevice.entity;

/**
 * @author Administrator QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.entity
 * @class describe 时间和系统配置 8个字节
 * @date 2018/1/26 14:44
 */

public class TimeAndSysStatus {

    ///系统时间
    public TimeInfo Time;
    public Status status;

    public class Status{
        public int Resv;
        public SystemDataStatus SysSta;
    }

}
