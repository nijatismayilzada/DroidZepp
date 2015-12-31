package com.droidzepp.droidzepp;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.droidzepp.droidzepp.datacollection.AccelerometerNewDataHandler;
import com.droidzepp.droidzepp.datacollection.GyroscopeNewDataHandler;
import com.droidzepp.droidzepp.datacollection.XYZ;
import com.droidzepp.droidzepp.datacollection.XYZwithTime;

import java.util.ArrayList;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class TempDatabaseTest extends ApplicationTestCase<Application> {

    private Application application;

    public TempDatabaseTest() {
        super(Application.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        application = getApplication();
    }

    public void testShouldReturnRowNumberWhenNewAccelerometerDataAdded(){
        AccelerometerNewDataHandler db = new AccelerometerNewDataHandler(application.getApplicationContext());
        XYZwithTime newData = new XYZwithTime(3, "time", 10, 10, 10);
        long result = db.addXYZ(newData);
        assertNotNull(result);
        assertTrue(result != 0);
    }

    public void testShouldReturnRowNumberWhenNewGyroscopeDataAdded(){
        GyroscopeNewDataHandler db = new GyroscopeNewDataHandler(application.getApplicationContext());
        XYZ newData = new XYZ(3, 10, 10, 10);
        long result = db.addXYZ(newData);
        assertNotNull(result);
        assertTrue(result != 0);
    }

    public void testShoudReturnAllTemporaryAccelerometerData(){
        AccelerometerNewDataHandler db = new AccelerometerNewDataHandler(application.getApplicationContext());
        List<XYZwithTime> dataList = new ArrayList<>();
        dataList = db.getAllData();
        assertNotNull(dataList);
        assertTrue(dataList.size() != 0);
    }
    public void testShoudReturnAllTemporaryGyroscopeData(){
        GyroscopeNewDataHandler db = new GyroscopeNewDataHandler(application.getApplicationContext());
        List<XYZ> dataList = new ArrayList<>();
        dataList = db.getAllData();
        assertNotNull(dataList);
        assertTrue(dataList.size() != 0);
    }
}