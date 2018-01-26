package com.example.vincent.mybluetoothdevice.entity;

/**
 * @author Administrator QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.entity
 * @class describe 系统状态
 * @date 2018/1/26 14:39
 */

public class SystemDataStatus {

    private int ResvTime;
    ///导联状态
    private int LeadOff ;
    ///不适标记
    private int PatientFlag; //1是有标记 0是没有标记。
    ///PACE 标记
    private int PaceEnableSta;
    //工频陷波状态
    private int PowerFreqSta;
    ///预留
    private int Resv;

    public int getResvTime() {
        return ResvTime;
    }

    public void setResvTime(int resvTime) {
        ResvTime = resvTime;
    }

    public int getLeadOff() {
        return LeadOff;
    }

    public void setLeadOff(int leadOff) {
        LeadOff = leadOff;
    }

    public int getPatientFlag() {
        return PatientFlag;
    }

    public void setPatientFlag(int patientFlag) {
        PatientFlag = patientFlag;
    }

    public int getPaceEnableSta() {
        return PaceEnableSta;
    }

    public void setPaceEnableSta(int paceEnableSta) {
        PaceEnableSta = paceEnableSta;
    }

    public int getPowerFreqSta() {
        return PowerFreqSta;
    }

    public void setPowerFreqSta(int powerFreqSta) {
        PowerFreqSta = powerFreqSta;
    }

    public int getResv() {
        return Resv;
    }

    public void setResv(int resv) {
        Resv = resv;
    }
}
