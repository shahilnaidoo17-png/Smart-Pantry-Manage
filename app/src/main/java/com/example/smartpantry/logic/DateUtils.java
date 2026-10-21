package com.example.smartpantry.logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    private static final String FORMAT = "yyyy-MM-dd";

    /** Days from today until the date (negative = already expired); null if no/invalid date. */
    public static Integer daysUntil(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return null;
        try {
            SimpleDateFormat f = new SimpleDateFormat(FORMAT, Locale.US);
            Date expiry = f.parse(isoDate);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long diff = expiry.getTime() - today.getTimeInMillis();
            return (int) TimeUnit.MILLISECONDS.toDays(diff);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String format(int year, int month, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
    }
}
