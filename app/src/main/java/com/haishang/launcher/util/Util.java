package com.haishang.launcher.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chenrun on 2016/6/29 0029.
 */
public class Util {
    /**
     * start :开始时间 yyyy--MM--dd
     * end: 结束时间  yyyy--MM--dd
     *
     * @return boolean
     * 当前时间是否在给定的时间之间
     */
    public static boolean nowIsBetween(String start, String end) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy--MM--dd");
        try {
            Date startDate = simpleDateFormat.parse(start);
            Date endDate = simpleDateFormat.parse(end);
            Date cuDate = new Date(System.currentTimeMillis());
            if (cuDate.after(startDate) && cuDate.before(endDate)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }
}
