package com.example.vincent.mybluetoothdevice.utils;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.utils
 * @class describe
 * @date 2018/1/25 16:03
 */

public class AnalysisDataUtils {

    /**
     * 解析系统功能信息
     * @param datas 蓝牙设备传递过来的数据
     * @return 二进制的字符串信息
     */
    public static String analysis0x86(byte[] datas){

        byte[] datas2 = new byte[2];
        datas2[0] = datas[1];
        datas2[1] = datas[2];
        int lenth = byteArrayToInt(datas2);
        System.out.println("lenth = "+lenth);

        byte[] content = new byte[2];
        content[0] = datas[4];
        content[1] = datas[5];

        int contents = byteArrayToInt(content);
        return Integer.toBinaryString(contents);
    }

    /**
     * 字节数组转为int 地位在前
     * @param b
     * @return
     */
    public static int byteArrayToInt(byte[] b) {
        return   (b[0] & 0xFF) |
                ( (b[1] & 0xFF) << 8);
    }




}
