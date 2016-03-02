package com.droidzepp.droidzepp.alarm;

public class AlarmObject {
    int hour;
    int minute;
    int frequency;
    String actionToAlarm;

    public AlarmObject(){

    }

    public AlarmObject(int hour, int minute, int frequency, String actionToAlarm) {
        this.hour = hour;
        this.minute = minute;
        this.frequency = frequency;
        this.actionToAlarm = actionToAlarm;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getActionToAlarm() {
        return actionToAlarm;
    }

    public void setActionToAlarm(String actionToAlarm) {
        this.actionToAlarm = actionToAlarm;
    }
}