/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

        return timeString;

    }

    private long getAgeInDays(Date from) {

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(from);
        
        long diff = System.currentTimeMillis() - calendar.getTime().getTime();

        return (diff / (1000 * 60 * 60 * 24));
        
    }
    
}
