/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author henrik
 */
public class DateUtility {

    private HashMap<Locale, SimpleDateFormat> yearFormatter = new HashMap<Locale, SimpleDateFormat>();
    private HashMap<Locale, SimpleDateFormat> dayOfYearFormatter = new HashMap<Locale, SimpleDateFormat>();
    private HashMap<Locale, SimpleDateFormat> shortTimeFormatter = new HashMap<Locale, SimpleDateFormat>();
    private HashMap<Locale, SimpleDateFormat> timeFormatter = new HashMap<Locale, SimpleDateFormat>();
    private HashMap<Locale, SimpleDateFormat> dateFormatter = new HashMap<Locale, SimpleDateFormat>();
    private HashMap<Locale, SimpleDateFormat> dateTimeFormatter = new HashMap<Locale, SimpleDateFormat>();
    private HashMap<Locale, HashMap<String, String>> relativeTimeLabels = new HashMap<Locale, HashMap<String, String>>();

    private DateUtility() {

        yearFormatter.put(new Locale("sv"), new SimpleDateFormat("yyyy", new Locale("sv")));
        dayOfYearFormatter.put(new Locale("sv"), new SimpleDateFormat("d MMM", new Locale("sv")));
        shortTimeFormatter.put(new Locale("sv"), new SimpleDateFormat("HH.mm", new Locale("sv")));
        timeFormatter.put(new Locale("sv"), new SimpleDateFormat("HH.mm.ss", new Locale("sv")));
        dateFormatter.put(new Locale("sv"), new SimpleDateFormat("dd MMM yyyy", new Locale("sv")));
        dateTimeFormatter.put(new Locale("sv"), new SimpleDateFormat("d MMMM HH.mm", new Locale("sv")));

        HashMap<String, String> sv = new HashMap<String, String>();

        sv.put("yesterday", "igår");
        sv.put("today", "");

        relativeTimeLabels.put(new Locale("sv"), sv);

    }

    public static DateUtility getInstance() {
        return DateUtilityHolder.INSTANCE;
    }

    private static class DateUtilityHolder {

        private static final DateUtility INSTANCE = new DateUtility();
    }

    public String getTimeString(Date time, Locale locale) {

        String retval = DateFormat.getDateInstance(DateFormat.SHORT).format(time);

        if (timeFormatter.containsKey(locale)) {

            retval = timeFormatter.get(locale).format(time);

        }

        return retval;

    }

    public String getDateString(Date time, Locale locale) {

        String retval = DateFormat.getDateInstance(DateFormat.MEDIUM).format(time);

        if (dateFormatter.containsKey(locale)) {

            retval = dateFormatter.get(locale).format(time);

        }

        return retval;

    }

    public String getDateTimeString(Date time, Locale locale) {

        String retval = DateFormat.getDateInstance(DateFormat.LONG).format(time);

        if (dateTimeFormatter.containsKey(locale)) {

            retval = dateTimeFormatter.get(locale).format(time);

        }

        return retval;

    }

    public String getRelativeDateTimeString(Date time, Locale locale) {

        String timeString = dayOfYearFormatter.get(locale).format(time);
        String year = "";

        if (getAgeInDays(time) == 0) {

            timeString = relativeTimeLabels.get(locale).get("today");

        } else if (getAgeInDays(time) == 1) {

            timeString = relativeTimeLabels.get(locale).get("yesterday");

        }

        if (!yearFormatter.get(locale).format(new Date()).equals(yearFormatter.get(locale).format(time))) {

            year = " " + yearFormatter.get(locale).format(time);

        }

        if (dateTimeFormatter.containsKey(locale)) {

            timeString += year + " " + shortTimeFormatter.get(locale).format(time);

        }

        return timeString.trim();

    }

    private long getAgeInDays(Date from) {

        long age = 0l;
        
        GregorianCalendar startDate = new GregorianCalendar();        
        GregorianCalendar endDate = new GregorianCalendar();
        
        GregorianCalendar now = new GregorianCalendar();
        now.setTime(new Date());
        
        GregorianCalendar then = new GregorianCalendar();
        then.setTime(from);
        
        endDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));
        startDate.set(then.get(Calendar.YEAR), then.get(Calendar.MONTH), then.get(Calendar.DATE));
        
        now = null;
        then = null;
        
        if (System.currentTimeMillis() - startDate.getTime().getTime() > 0) {
         
            age = daysBetween(startDate, endDate);
            
        }

        return age;

    }

    public static long daysBetween(final Calendar startDate, final Calendar endDate) {
        
        Calendar sDate = (Calendar) startDate.clone();
        long daysBetween = 0;

        int y1 = sDate.get(Calendar.YEAR);
        int y2 = endDate.get(Calendar.YEAR);
        int m1 = sDate.get(Calendar.MONTH);
        int m2 = endDate.get(Calendar.MONTH);

        //**year optimization**
        while (((y2 - y1) * 12 + (m2 - m1)) > 12) {
            //move to Jan 01
            if (sDate.get(Calendar.MONTH) == Calendar.JANUARY
                    && sDate.get(Calendar.DAY_OF_MONTH) == sDate.getActualMinimum(Calendar.DAY_OF_MONTH)) {

                daysBetween += sDate.getActualMaximum(Calendar.DAY_OF_YEAR);
                sDate.add(Calendar.YEAR, 1);
            } else {
                int diff = 1 + sDate.getActualMaximum(Calendar.DAY_OF_YEAR) - sDate.get(Calendar.DAY_OF_YEAR);
                sDate.add(Calendar.DAY_OF_YEAR, diff);
                daysBetween += diff;
            }
            y1 = sDate.get(Calendar.YEAR);
        }

        //** optimize for month **
        //while the difference is more than a month, add a month to start month
        while ((m2 - m1) % 12 > 1) {
            daysBetween += sDate.getActualMaximum(Calendar.DAY_OF_MONTH);
            sDate.add(Calendar.MONTH, 1);
            m1 = sDate.get(Calendar.MONTH);
        }

        // process remainder date
        while (sDate.before(endDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }

        return daysBetween;
        
    }
    
}
