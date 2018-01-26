package com.example.vincent.mybluetoothdevice.entity;

/**
 * @author Administrator QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.entity
 * @class describe 系统报警设置
 * @date 2018/1/26 14:35
 */

public class SystemAlertInfo {

    private int LowPowerAlert;
    private int FlashAlert;
    private int LeadAlert;
    private int BloothStatusAlert;

    public int getLowPowerAlert() {
        return LowPowerAlert;
    }

    public void setLowPowerAlert(int lowPowerAlert) {
        LowPowerAlert = lowPowerAlert;
    }

    public int getFlashAlert() {
        return FlashAlert;
    }

    public void setFlashAlert(int flashAlert) {
        FlashAlert = flashAlert;
    }

    public int getLeadAlert() {
        return LeadAlert;
    }

    public void setLeadAlert(int leadAlert) {
        LeadAlert = leadAlert;
    }

    public int getBloothStatusAlert() {
        return BloothStatusAlert;
    }

    public void setBloothStatusAlert(int bloothStatusAlert) {
        BloothStatusAlert = bloothStatusAlert;
    }
}
