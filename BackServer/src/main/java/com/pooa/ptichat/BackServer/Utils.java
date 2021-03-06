package com.pooa.ptichat.BackServer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static String dateToString(Date date) {
        if (date == null) return "";
        DateFormat messageDateFormat = new SimpleDateFormat(Constants.DATE_PATTERN);
        return messageDateFormat.format(date);
    }

    public static Date stringToDate(String dateString) {
        if ("".equals(dateString)) return null;
        DateFormat messageDateFormat = new SimpleDateFormat(Constants.DATE_PATTERN);
        Date messageDate = null;
        try {
            messageDate = messageDateFormat.parse(dateString);
        } catch (ParseException e) {
            System.out.println("Utils.stringToDate: Could not parse stored date: " + e.getMessage());
        }
        return messageDate;
    }

    public static String twoUserIdsToPrivateChatId(String userId1, String userId2) {
        return (userId2.compareTo(userId1) > 0) ? userId1 + "+" + userId2 : userId2 + "+" + userId1;
    }
}
