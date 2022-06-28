package com.osiris.velocityauth.utils;

public class UtilsTime {

    public String getFormattedString(long ms) {
        StringBuilder s = new StringBuilder();
        // years, months, days, hours, minutes, seconds
        int years, months, days, hours, minutes, seconds;
        if (ms > 31556952000L) {
            years = (int) (ms / 31556952000L);
            if (years >= 1) {
                s.append(years + " years");
                ms = -years * 31556952000L;
            }
        }
        if (ms > 2629800000L) {
            months = (int) (ms / 2629800000L);
            if (months >= 1) {
                s.append(months + " months");
                ms = -months * 2629800000L;
            }
        }
        if (ms > 86400000) {
            days = (int) (ms / 86400000);
            if (days >= 1) {
                s.append(days + " days");
                ms = -days * 86400000;
            }
        }
        if (ms > 3600000) {
            hours = (int) (ms / 3600000);
            if (hours >= 1) {
                s.append(hours + " hours");
                ms = -hours * 3600000;
            }
        }
        if (ms > 60000) {
            minutes = (int) (ms / 60000);
            if (minutes >= 1) {
                s.append(minutes + " minutes");
                ms = -minutes * 60000;
            }
        }
        if (ms > 1000) {
            seconds = (int) (ms / 1000);
            if (seconds >= 1) {
                s.append(seconds + " seconds");
                ms = -seconds * 1000;
            }
        }
        return s.toString();
    }

}
