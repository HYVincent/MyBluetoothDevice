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
//    public TimeAndSysStatus TimeSysStatus;
    ///pace标记
    public String PaceFlag;
    ///原始数据位置标记
    public int RawDataFlag;

    public int Data;
    public int DataLen;


    public class TimeAndSysStatus{

        ///系统时间
        public TimeInfo Time;
        public Status status;

        public class Status{

            public int Resv;
            public SystemDataStatus SysSta;


            public class SystemDataStatus{
                public int ResvTime;
                ///导联状态
                public int LeadOff ;
                ///不适标记
                public int PatientFlag; //1是有标记 0是没有标记。
                ///PACE 标记
                public int PaceEnableSta;
                //工频陷波状态
                public int PowerFreqSta;
                ///预留
                public int Resv;
            }

        }


        public class TimeInfo{
            public int Year;
            public int Month;
            public int Day;
            public int Hour;
            public int Min;
            public int Sec;
        }

    }

}
