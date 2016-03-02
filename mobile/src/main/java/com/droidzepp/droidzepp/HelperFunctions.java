package com.droidzepp.droidzepp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HelperFunctions {


    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_START_RECORDING = 3;
    public static final int MSG_RECORDING_DONE = 4;
    public static final int MSG_COMBINING_DONE = 5;
    public static final int MSG_CLASSIFIER_RESULT = 6;
    public static final int MSG_REGISTER_NEW_ALARM = 7;
    public static final int MSG_START_CLASSIFICATION = 8;

    public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
