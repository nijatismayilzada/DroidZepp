package com.droidzepp.droidzepp.sendtoclassify;

import com.google.android.gms.wearable.DataMap;

public class FeatureContainerToSend {

    float accWX;
    float accWY;
    float accWZ;
    float gyroWX;
    float gyroWY;
    float gyroWZ;

    public FeatureContainerToSend(){

    }

    public FeatureContainerToSend(float accWX, float accWY, float accWZ, float gyroWX, float gyroWY, float gyroWZ) {
        this.accWX = accWX;
        this.accWY = accWY;
        this.accWZ = accWZ;
        this.gyroWX = gyroWX;
        this.gyroWY = gyroWY;
        this.gyroWZ = gyroWZ;
    }

    public FeatureContainerToSend(DataMap map){
        this(map.getFloat("accWX"),
                map.getFloat("accWY"),
                map.getFloat("accWZ"),
                map.getFloat("gyroWX"),
                map.getFloat("gyroWY"),
                map.getFloat("gyroWZ"));
    }

    public DataMap putToDataMap(DataMap map){
        map.putFloat("accWX", accWX);
        map.putFloat("accWY", accWY);
        map.putFloat("accWZ", accWZ);
        map.putFloat("gyroWX", gyroWX);
        map.putFloat("gyroWY", gyroWY);
        map.putFloat("gyroWZ", gyroWZ);
        return map;
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
}
