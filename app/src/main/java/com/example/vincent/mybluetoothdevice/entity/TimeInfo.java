package com.example.vincent.mybluetoothdevice.entity;

/**
 * @author Administrator QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.entity
 * @class describe 波形时间
 * @date 2018/1/26 14:41
 */

public class TimeInfo {

    private int Year;
    private int Month;
    private int Day;
    private int Hour;
    private int Min;
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
