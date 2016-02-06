package com.droidzepp.droidzepp.datacollection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

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

            XYZ data = new XYZ();
            AccelerometerNewDataHandler dbNewData = new AccelerometerNewDataHandler(mContext);
            // Many sensors return 3 values, one for each axis
            data.setX(event.values[0]);
            data.setY(event.values[1]);
            data.setZ(event.values[2]);
            Log.d("droidzepp.wear.acc", String.valueOf(data.getX()) + "  " + String.valueOf(data.getY()) + "  " + String.valueOf(data.getZ()));
            dbNewData.addXYZ(data);
            SensorHandlerService.flagForAcc = false;
        }
    }
}
