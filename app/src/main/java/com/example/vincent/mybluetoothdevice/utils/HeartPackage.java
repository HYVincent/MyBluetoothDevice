package com.example.vincent.mybluetoothdevice.utils;

import java.util.Date;


/**
 * 
 * @Description 解析波形数据vo
 * @Author:xiaoxiong
 * @date: 2018年1月31日 下午5:20:08
 */
public class HeartPackage {
	/**
	 * 整包数据
	 */
	private byte[] data;
	
	/**
	 * 数据包id
	 */
	private long packageId = 0l;
	
	/**
	 * 导联状态 0:脱落   1:正常
	 */
	private int linkStatus = 0;
	
	/**
	 * 病人不适标记 0:正常  1:不适
	 */
	private int symptom = 0;
	
	/**
	 * pace状态 0检测关闭 1:检测打开
	 */
	private int paceStatus = 0;
	
	/**
	 * 工频陷波状态 0:关闭   1:50hz  2:60hz
	 */
	private int rateStatus = 0;
	
	/**
	 * 时间戳
	 */
	private Date date ;
	
	/**
	 * pace标志
	 */
	private int[] paces = new int[125];
	
	/**
	 * 原始数据位置标记
	 */
	private int[] flags = new int[16];
	
	/**
	 * 点
	 */
	private int[] points = new int[125];
	
	
	public HeartPackage(byte[] data) {
		this.data = data;
		this.packageId = DeviceDataUtils.parseToNumber(data, 0, 4);//数据包id
		this.date = DeviceDataUtils.parseToDate(data, 4);//日期
		long status = DeviceDataUtils.parseToNumber(data, 0, 4);//系统状态
		this.linkStatus = (int) (status&0x03);
		this.symptom = (int) ((status>>2)&0x01);
		this.paceStatus = (int) ((status>>3)&0x01);
		this.rateStatus = (int) ((status>>4)&0x03);
		this.paces = DeviceDataUtils.getPaces(data, 12);
		this.flags = DeviceDataUtils.getFlags(data, 28);//标志位
		this.points = DeviceDataUtils.getPoints(flags, data, 44);//点 
	} 
	
	public HeartPackage(String waveData) {
		this(DeviceDataUtils.stringToHex(waveData));
	}

	public byte[] getData() {
		return data;
	}



	public void setData(byte[] data) {
		this.data = data;
	}



	public long getPackageId() {
		return packageId;
	}



	public void setPackageId(long packageId) {
		this.packageId = packageId;
	}



	public Date getDate() {
		return date;
	}



	public void setDate(Date date) {
		this.date = date;
	}



	public int[] getPaces() {
		return paces;
	}



	public void setPaces(int[] paces) {
		this.paces = paces;
	}



	public int[] getFlags() {
		return flags;
	}



	public void setFlags(int[] flags) {
		this.flags = flags;
	}



	public int[] getPoints() {
		return points;
	}



	public void setPoints(int[] points) {
		this.points = points;
	}



	public int getLinkStatus() {
		return linkStatus;
	}



	public void setLinkStatus(int linkStatus) {
		this.linkStatus = linkStatus;
	}



	public int getSymptom() {
		return symptom;
	}



	public void setSymptom(int symptom) {
		this.symptom = symptom;
	}



	public int getPaceStatus() {
		return paceStatus;
	}



	public void setPaceStatus(int paceStatus) {
		this.paceStatus = paceStatus;
	}



	public int getRateStatus() {
		return rateStatus;
	}



	public void setRateStatus(int rateStatus) {
		this.rateStatus = rateStatus;
	}
	
	
}
