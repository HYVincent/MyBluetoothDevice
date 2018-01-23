package com.example.vincent.mybluetoothdevice.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name StartKangMedical_Android
 * @page com.vincent.mylibrary.util
 * @class describe
 * @date 2018/1/9 11:52
 */

public class DateUtils {


    private final static long minute = 60 * 1000;// 1分钟
    private final static long hour = 60 * minute;// 1小时
    private final static long day = 24 * hour;// 1天
    private final static long month = 31 * day;// 月
    private final static long year = 12 * month;// 年

    //时间格式
    public final static String DATE_FORMAT_ALL = "yyyy-MM-dd HH:mm:ss";
    public final static String DATE_FORMAT_ALL2 = "yyyy/MM/dd HH:mm:ss";
    public final static String DATE_FORMAT_YEAR_MONTH_DAY = "yyyy-MM-dd";
    public final static String DATA_FORMAT_YEAR_MONTH_DAY2 = "yyyy年MM月dd日";
    public final static String DATA_FORMAT_YEAR = "yyyy";
    public final static String DATA_FORMAT_MONTH = "MM月";

    /**
     * 格式化时间
     * @param format 传入的格式
     * @param time 时间戳
     * @return
     */
    public static String getDateString(String format, long time)
    {
        SimpleDateFormat sf = new SimpleDateFormat(format);
        Date d = new Date(time);
        return sf.format(d);
    }



    /**
     * 返回文字描述的日期
     *
     * @param date
     * @return
     */
    public static String getTimeFormatText(Date date) {
        if (date == null) {
            return null;
        }
        long diff = System.currentTimeMillis() - date.getTime();
        long r = 0;
        if (diff > year) {
            r = (diff / year);
            return r + "年前";
        }
        if (diff > month) {
            r = (diff / month);
            return r + "个月前";
        }
        if (diff > day) {
            r = (diff / day);
            return r + "天前";
        }
        if (diff > hour) {
            r = (diff / hour);
            return r + "个小时前";
        }
        if (diff > minute) {
            r = (diff / minute);
            return r + "分钟前";
        }
        return "刚刚";
    }

    /**
     * 返回文字描述的日期
     *
     * @param date
     * @return
     */
    public static String getTimeFormatText2(Date date) {
        if (date == null) {
            return null;
        }
        long diff = System.currentTimeMillis() - date.getTime();
        long r = 0;
        if (diff > month) {
            r = (diff / month);
            return getDateString(DATE_FORMAT_ALL,date.getTime());
        }
        if (diff > day) {
            r = (diff / day);
            return r + "天前";
        }
        if (diff > hour) {
            r = (diff / hour);
            return r + "个小时前";
        }
        if (diff > minute) {
            r = (diff / minute);
            return r + "分钟前";
        }
        return "刚刚";
    }

}
