package fr.centralesupelec.ptichatapp;
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

    public static Date stringToDate(String dateString) {
        if ("".equals(dateString)) return null;
        DateFormat messageDateFormat = new SimpleDateFormat(Constants.DATE_PATTERN, Locale.FRANCE);
        Date messageDate = null;
        try {
            messageDate = messageDateFormat.parse(dateString);
        } catch (ParseException e) {
            System.out.println("Utils.stringToDate: Could not parse stored date: " + e.getMessage());
        }
        return messageDate;
    }
}
