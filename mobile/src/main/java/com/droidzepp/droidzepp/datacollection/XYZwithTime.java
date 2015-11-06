package com.droidzepp.droidzepp.datacollection;

/**
 * Created by nijat on 05/11/15.
 */
public class XYZwithTime {
    int id;
    String time;
    float x;
    float y;
    float z;

    public XYZwithTime(){

    }
    public XYZwithTime(int id, String time, float x, float y, float z){
        this.id = id;
        this.time = time;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }
}
