package com.droidzepp.droidzepp;

import com.droidzepp.droidzepp.datacollection.XYZ;
import com.droidzepp.droidzepp.datacollection.XYZwithTime;

import org.junit.Test;

public class XYZobjectTest {
    @Test
    public void shouldRegisterValidXYZobject() {
        XYZ featureObject = new XYZ();
        int id = 5;
        featureObject.setId(id);
        assert featureObject != null;
        assert featureObject.getId() == id;
    }

    @Test
    public void shouldReturnXfromXYZobject() {
        XYZ featureObject = new XYZ();
        float x = 10;
        featureObject.setX(x);
        assert featureObject.getX() == x;
    }

    @Test
    public void shouldReturnYfromXYZobject() {
        XYZ featureObject = new XYZ();
        float y = 20;
        featureObject.setY(y);
        assert featureObject.getY() == y;
    }

    @Test
    public void shouldReturnZfromXYZobject() {
        XYZ featureObject = new XYZ();
        float z = 30;
        featureObject.setZ(z);
        assert featureObject.getZ() == z;
    }

    @Test
    public void shouldRegisterValidXYZobjectWithTime() {
        XYZwithTime featureObject = new XYZwithTime();
        int id = 5;
        featureObject.setId(id);
        assert featureObject != null;
        assert featureObject.getId() == id;
    }

    @Test
    public void shouldReturnXfromXYZTimeobject() {
        XYZwithTime featureObject = new XYZwithTime();
        float x = 10;
        featureObject.setX(x);
        assert featureObject.getX() == x;
    }

    @Test
    public void shouldReturnYfromXYZTimeobject() {
        XYZwithTime featureObject = new XYZwithTime();
        float y = 20;
        featureObject.setY(y);
        assert featureObject.getY() == y;
    }

    @Test
    public void shouldReturnZfromXYZTimeobject() {
        XYZwithTime featureObject = new XYZwithTime();
        float z = 30;
        featureObject.setZ(z);
        assert featureObject.getZ() == z;
    }

    @Test
    public void shouldReturnTimefromXYZTimeobject() {
        XYZwithTime featureObject = new XYZwithTime();
        String dateTime = HelperFunctions.getDateTime();
        featureObject.setTime(dateTime);
        assert featureObject.getTime() == dateTime;
    }
}
