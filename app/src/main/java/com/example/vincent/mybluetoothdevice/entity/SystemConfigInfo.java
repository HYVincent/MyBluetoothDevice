package com.example.vincent.mybluetoothdevice.entity;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.entity
 * @class describe
 * @date 2018/1/26 13:45
 */

public class SystemConfigInfo {

    private int ChannelNumber;
    private int Pacemaker;
    private int BreathMoni;
    private int WaveConfig;

    public int getChannelNumber() {
        return ChannelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        ChannelNumber = channelNumber;
    }

    public int getPacemaker() {
        return Pacemaker;
    }

    public void setPacemaker(int pacemaker) {
        Pacemaker = pacemaker;
    }

    public int getBreathMoni() {
        return BreathMoni;
    }

    public void setBreathMoni(int breathMoni) {
        BreathMoni = breathMoni;
    }

    public int getWaveConfig() {
        return WaveConfig;
    }

    public void setWaveConfig(int waveConfig) {
        WaveConfig = waveConfig;
    }
}
