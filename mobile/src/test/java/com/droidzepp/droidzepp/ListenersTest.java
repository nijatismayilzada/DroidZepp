package com.droidzepp.droidzepp;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.droidzepp.droidzepp.datacollection.AccelerometerNewDataHandler;
import com.droidzepp.droidzepp.datacollection.XYZwithTime;

import org.junit.Test;

/**
 * Created by nijat on 16/12/15.
 */
public class ListenersTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public ListenersTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Test
    public void shouldReturnRowNumberWhenAddNewData(){
        Activity mTestActivity = getActivity();
        XYZwithTime newCurrentDateTime = new XYZwithTime(1, "time", 10, 10, 10);
        AccelerometerNewDataHandler db = new AccelerometerNewDataHandler(mTestActivity.getApplicationContext());
        long rowNumber = db.addXYZ(newCurrentDateTime);
        assert rowNumber != 0;
    }
}
