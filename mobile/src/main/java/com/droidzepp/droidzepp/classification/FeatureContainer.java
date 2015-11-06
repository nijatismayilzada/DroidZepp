package com.droidzepp.droidzepp.classification;

/**
 * Created by nijat on 28/10/15.
 */
public class FeatureContainer {

    String time;
    float a;
    float b;
    float c;
    float d;
    float e;
    float f;
    long lid;

    public FeatureContainer(){

    }
    public FeatureContainer(String time, float a, float b, float c, float d, float e, float f, long lid){
        this.time = time;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.lid = lid;

    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getC() {
        return c;
    }

    public void setC(float c) {
        this.c = c;
    }

    public float getD() {
        return d;
    }

    public void setD(float d) {
        this.d = d;
    }

    public float getE() {
        return e;
    }

    public void setE(float e) {
        this.e = e;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }

    public long getLid() {
        return lid;
    }

    public void setLid(long lid) {
        this.lid = lid;
    }
}
