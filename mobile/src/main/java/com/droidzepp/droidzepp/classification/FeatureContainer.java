package com.droidzepp.droidzepp.classification;

/**
 * Created by nijat on 28/10/15.
 */
public class FeatureContainer {

    private String time;
    private float accMX;
    private float accMY;
    private float accMZ;
    private float gyroMX;
    private float gyroMY;
    private float gyroMZ;
    private float accWX;
    private float accWY;
    private float accWZ;
    private float gyroWX;
    private float gyroWY;
    private float gyroWZ;
    private long lId;


    public FeatureContainer(){
    }

    public FeatureContainer(String time, float accMX, float accMY, float accMZ, float gyroMX, float gyroMY, float gyroMZ, float accWX, float accWY, float accWZ, float gyroWX, float gyroWY, float gyroWZ, long lId) {
        this.time = time;
        this.accMX = accMX;
        this.accMY = accMY;
        this.accMZ = accMZ;
        this.gyroMX = gyroMX;
        this.gyroMY = gyroMY;
        this.gyroMZ = gyroMZ;
        this.accWX = accWX;
        this.accWY = accWY;
        this.accWZ = accWZ;
        this.gyroWX = gyroWX;
        this.gyroWY = gyroWY;
        this.gyroWZ = gyroWZ;
        this.lId = lId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getAccMX() {
        return accMX;
    }

    public void setAccMX(float accMX) {
        this.accMX = accMX;
    }

    public float getAccMY() {
        return accMY;
    }

    public void setAccMY(float accMY) {
        this.accMY = accMY;
    }

    public float getAccMZ() {
        return accMZ;
    }

    public void setAccMZ(float accMZ) {
        this.accMZ = accMZ;
    }

    public float getGyroMX() {
        return gyroMX;
    }

    public void setGyroMX(float gyroMX) {
        this.gyroMX = gyroMX;
    }

    public float getGyroMY() {
        return gyroMY;
    }

    public void setGyroMY(float gyroMY) {
        this.gyroMY = gyroMY;
    }

    public float getGyroMZ() {
        return gyroMZ;
    }

    public void setGyroMZ(float gyroMZ) {
        this.gyroMZ = gyroMZ;
    }

    public float getAccWX() {
        return accWX;
    }

    public void setAccWX(float accWX) {
        this.accWX = accWX;
    }

    public float getAccWY() {
        return accWY;
    }

    public void setAccWY(float accWY) {
        this.accWY = accWY;
    }

    public float getAccWZ() {
        return accWZ;
    }

    public void setAccWZ(float accWZ) {
        this.accWZ = accWZ;
    }

    public float getGyroWX() {
        return gyroWX;
    }

    public void setGyroWX(float gyroWX) {
        this.gyroWX = gyroWX;
    }

    public float getGyroWY() {
        return gyroWY;
    }

    public void setGyroWY(float gyroWY) {
        this.gyroWY = gyroWY;
    }

    public float getGyroWZ() {
        return gyroWZ;
    }

    public void setGyroWZ(float gyroWZ) {
        this.gyroWZ = gyroWZ;
    }

    public long getlId() {
        return lId;
    }

    public void setlId(long lId) {
        this.lId = lId;
    }
}
