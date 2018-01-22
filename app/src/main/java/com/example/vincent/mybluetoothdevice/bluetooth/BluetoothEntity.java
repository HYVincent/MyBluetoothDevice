package com.example.vincent.mybluetoothdevice.bluetooth;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.bluetooth
 * @class describe
 * @date 2018/1/22 14:02
 */

public class BluetoothEntity {

    //蓝牙地址
    private String address;
    //蓝牙状态 0 正常状态 1 正在连接 2 连接成功 3 连接失败
    private int status;
    //蓝牙名称
    private String name;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
