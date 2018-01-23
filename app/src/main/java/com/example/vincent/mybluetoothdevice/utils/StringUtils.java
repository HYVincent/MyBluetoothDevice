package com.example.vincent.mybluetoothdevice.utils;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.utils
 * @class describe
 * @date 2018/1/23 14:57
 */

public class StringUtils {

    public static String byteToString(byte[] data){
        StringBuffer sb = new StringBuffer();
        for (int i = 0;i<data.length;i++){
            sb.append(String.valueOf(data[i]));
        }
        return sb.toString();
    }

}
