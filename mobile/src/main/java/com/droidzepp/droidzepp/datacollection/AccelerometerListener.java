package com.droidzepp.droidzepp.datacollection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.droidzepp.droidzepp.HelperFunctions;

public class AccelerometerListener implements SensorEventListener {
    private Context mContext;

    public AccelerometerListener(Context context){
        mContext = context;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (SensorHandlerService.flagForAcc) {

            XYZwithTime data = new XYZwithTime();
            AccelerometerNewDataHandler dbNewData = new AccelerometerNewDataHandler(mContext);
            // Many sensors return 3 values, one for each axis.
            data.setTime(HelperFunctions.getDateTime());
            data.setX(event.values[0]);
            data.setY(event.values[1]);
            data.setZ(event.values[2]);
            Log.d("droidzepp.mob.acc", String.valueOf(data.getTime()) + " " + String.valueOf(data.getX()) + "  " + String.valueOf(data.getY()) + "  " + String.valueOf(data.getZ()));
            dbNewData.addXYZ(data);
            SensorHandlerService.flagForAcc = false;
        }
    }
}
