package com.example.vincent.mybluetoothdevice.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @Description 设备数据解析工具类
 * @Author:xiaoxiong
 * @date: 2018年1月30日 下午2:34:23
 */
public class DeviceDataUtils {
	
	/**
	 * 
	 * @Description 解析数值
	 * @Author: xiaoxiong                                                                
	 * @date: 2018年1月30日 下午5:14:14 
	 * @param data 数据包
	 * @param start 开始
	 * @param end 结束
	 * @return
	 * @exception:
	 *
	 */
	public static long parseToNumber(byte[] data,int start,int end) {
		long num = 0L;
		for(int i = start ; i < end ; i++) {
			num += (data[i] &0xFF)<<((i-start)*8);
		}
		return num;
	}
	
	/**
	 * 
	 * @Description 解析数据得到日期
	 * @Author: xiaoxiong                                                                
	 * @date: 2018年1月30日 下午2:38:22 
	 * @param data 5个字节 最后一个字节忽略
	 * @return
	 * @exception:
	 *
	 */
	public static Date parseToDate(byte[] data,int start){
		long num = 0L;
		for(int i = start ; i < start + 4 ; i++) {
			num += (data[i] &0xFF)<<((i-start)*8);
		}
		long year = num & 0x3F;
		long month = (num >>6) &0x0F;
		long day = (num >> 10) &0x1F;
		long hour = (num >> 15) &0x1F;
		long min = (num >> 20) &0x3F;
		long sec = (num >> 26) &0x1F;
		Calendar cal = Calendar.getInstance();
		cal.set((int)(year+2000), (int)(month-1), (int)day, (int)hour, (int)min, (int)sec);
		return cal.getTime();
	}
	
	/**
	 * 
	 * @Description 获取125个点的pace标记
	 * @Author: xiaoxiong                                                                
	 * @date: 2018年1月31日 下午4:37:28 
	 * @param data
	 * @param start
	 * @return
	 * @exception:
	 *
	 */
	public static int[] getPaces(byte[] data,int start) {
		int[] result = new int[125];
		int[] flags = getFlags(data, start);
		for(int i = 0;i<125;i++) {
			result[i] = (flags[i>>3])&(1<<(i&0x07));
		}
		return result;
	}
	
	/**
	 * 
	 * @Description 解析原始数据标志位 16个字节
	 * @Author: xiaoxiong                                                                
	 * @date: 2018年1月31日 上午11:17:50 
	 * @param data
	 * @param start
	 * @return 返回16个int8
	 * @exception:
	 *
	 */
	public static int[] getFlags(byte[] data,int start) {
		int[] result = new int[16];
		int j = 0;
		for(int i = start ;  i < start + 16 ;i ++ ) {
			result[j] = data[i] & 0xFF;
			j++;
		}
		return result;
	}
	
	/**
	 * 
	 * @Description 解析得到一个数据包的125个点
	 * @Author: xiaoxiong                                                                
	 * @date: 2018年1月31日 上午11:55:28 
	 * @param flags 标志位16个字节,128个标志位,0表示原始数据,1表示差分数据,原始数据占两个字节,差分数据占一个字节,差分数据的原始点等于上一个点加上差分数据
	 * @param data 数据
	 * @param start 原始数据开始位
	 * @return
	 * @exception:
	 *
	 */
	public static int[] getPoints(int[] flags,byte[] data,int start) {
		int[] result = new int[125]; 
		for(int i = 0; i < 125;i ++) {
			if(((flags[i>>3])&(1<<(i&0x07))) ==0) {//差分数据
				if(i == 0){
					result[i] = data[0];
				}else {
					result[i] = data[start] + result[i - 1];
				}
				start ++;
			}else {//原始数据
				result[i] = toInt16(data[start],data[start+1]);
				start += 2;
			}
				
		}
		return result;
	}
	
	/**
	 * 
	 * @Description 解析得到int16数据
	 * @Author: xiaoxiong                                                                
	 * @date: 2018年1月31日 上午11:58:34 
	 * @param b1
	 * @param b2
	 * @return
	 * @exception:
	 *
	 */
	public static short toInt16(byte b1, byte b2) {
		return (short) Integer.parseInt(byteToHex(b2)  + byteToHex(b1),16);
	}
	
	/**
	 * 
	 * @Description byte转为16进制字符
	 * @Author: xiaoxiong                                                                
	 * @date: 2018年1月31日 下午3:56:52 
	 * @param b
	 * @return
	 * @exception:
	 *
	 */
	public static String byteToHex(byte b) {
       return String.format("%02x", new Integer(b & 0xff));
    }
	
	/**
	 * 
	 * @Description 16进制字符串转byte
	 * @Author: xiaoxiong                                                                
	 * @date: 2018年1月31日 下午4:27:33 
	 * @param data
	 * @return
	 * @exception:
	 *
	 */
	public static byte[] stringToHex(String data) {
		byte[] result = new byte[data.length() / 2];
		int j = 0;
		for(int i = 0;i<data.length()-1;i=i+2) {
			byte b = (byte) Integer.valueOf(data.substring(i, i+2), 16).intValue();
			result[j] = b;
			j++;
			
		}
		return result;
	} 
	
	public static void main(String[] args) {
		String dataStr = "de0c000052f8a5b2001100000000000000000000000000000000000001000000000000000000000000000000ecff020201fd00f8f2245a6323c597aef112060101fd0201fe0204010202020003020305040207080806fefdfefaf3eef5fcfdfe02fe0002fefd020201010100ff010000ff00ff010201ff0000fffd0000050a08060501fdfcf7f7fbfd0102fdfe01ff01fff0fd39645606af9ac5010f00fb02010100000204040202010117c0010000007faa0080de0c000052f8a5b2001100000000000000000000";
		byte[] data = stringToHex(dataStr);
		
		long packageId = parseToNumber(data, 0, 4);//数据包id
		Date date = parseToDate(data, 4);//日期
		int[] paces = getPaces(data, 12);
		int[] flags = getFlags(data, 28);//标志位
		int[] points = getPoints(flags, data, 44);//点 
		
	}
	
	
	
}
