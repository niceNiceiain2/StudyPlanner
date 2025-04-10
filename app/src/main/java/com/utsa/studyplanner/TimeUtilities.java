package com.utsa.studyplanner;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtilities {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String MILITARY_TIME_KEY = "use_military_time";

    public static String formatDateTime(Context context, Date date) {
        boolean useMilitaryTime = getMilitaryTimePreference(context);
        SimpleDateFormat sdf = new SimpleDateFormat(
                useMilitaryTime ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd hh:mm a",
                Locale.getDefault()
        );
        return sdf.format(date);
    }

    public static String formatTime(Context context, String timeString) {
        try {
            // Try parsing with 12-hour format first
            SimpleDateFormat inputFormat12 = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            SimpleDateFormat inputFormat24 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            Date date;
            try {
                date = inputFormat12.parse(timeString);  // Try 12-hour format with AM/PM
            } catch (ParseException e) {
                date = inputFormat24.parse(timeString);  // Fallback to 24-hour format
            }

            if (date != null) {
                boolean useMilitaryTime = getMilitaryTimePreference(context);
                SimpleDateFormat outputFormat = new SimpleDateFormat(
                        useMilitaryTime ? "HH:mm" : "hh:mm a",
                        Locale.getDefault()
                );
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timeString;
    }



    public static boolean getMilitaryTimePreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(MILITARY_TIME_KEY, false);
    }

    public static void setMilitaryTimePreference(Context context, boolean useMilitaryTime) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(MILITARY_TIME_KEY, useMilitaryTime).apply();
    }
}
