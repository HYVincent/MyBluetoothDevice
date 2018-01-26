package com.example.vincent.mybluetoothdevice.entity;

/**
 * @author Administrator QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.entity
 * @class describe 系统时间
 * @date 2018/1/26 14:36
 */

public class SystemTimeInfo {

    //当前年-2000
    private int Year;
    //1-12
    private int Month;
    //1-31
    private int Day;
    //0-23
    private int Hour;
    //0-59
    private int Min;
    //0-59
    private int Sec;

    public int getYear() {
        return Year;
    }

    public void setYear(int year) {
        Year = year;
    }

    public int getMonth() {
        return Month;
    }

    public void setMonth(int month) {
        Month = month;
    }

    public int getDay() {
        return Day;
    }

    public void setDay(int day) {
        Day = day;
    }

    public int getHour() {
        return Hour;
    }

    public void setHour(int hour) {
        Hour = hour;
    }

    public int getMin() {
        return Min;
    }

    public void setMin(int min) {
        Min = min;
    }

    public int getSec() {
        return Sec;
    }

    public void setSec(int sec) {
        Sec = sec;
    }
}
