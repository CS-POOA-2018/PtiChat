package fr.centralesupelec.ptichatapp;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static String dateToString(Date date, String format) {
        if (date == null) return "";
        DateFormat messageDateFormat = new SimpleDateFormat(format, Locale.FRANCE);
        return messageDateFormat.format(date);
    }

    public static String dateToString(Date date) {
        return dateToString(date, Constants.DATE_PATTERN);
    }

    public static String twoUserIdsToPrivateChatId(String userId1, String userId2) {
        return (userId2.compareTo(userId1) > 0) ? userId1 + "+" + userId2 : userId2 + "+" + userId1;
    }

    public static Date stringToDate(String dateString) {
        if ("".equals(dateString)) return null;
        DateFormat messageDateFormat = new SimpleDateFormat(Constants.DATE_PATTERN, Locale.FRANCE);
        Date messageDate = null;
        try {
            messageDate = messageDateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e("UTd","Could not parse stored date: " + e.getMessage());
        }
        return messageDate;
    }

    public static void writeHostInfo(final Context context, final String HOSTNAME, final int PORT) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE).edit();
        editor.putString("hostName", HOSTNAME);
        editor.putInt("hostPort", PORT);
        editor.apply();
    }

    public static Pair<String, Integer> getHostInfo(final Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        String hostName = preferences.getString("hostName", null);
        int hostPort = preferences.getInt("hostPort", 0);

        if (hostName == null || hostPort == 0) {
            // Fallback to hardcoded constants
            return new Pair<>(Constants.HOST_NAME, Constants.HOST_PORT);
        }
        return new Pair<>(hostName, hostPort);
    }

    public static void writeCredentials(final Context context, final String USERNAME, final String PASSWORD) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE).edit();
        editor.putString("username", USERNAME);
        editor.putString("password", PASSWORD);
        editor.apply();
    }

    public static Pair<String, String> getCredentials(final Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);
        return new Pair<>(username, password);
    }
}
