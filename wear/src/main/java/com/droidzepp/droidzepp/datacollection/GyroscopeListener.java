package com.droidzepp.droidzepp.datacollection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class GyroscopeListener implements SensorEventListener {
    private static final String LOGTAG = "GyroscopeListener";
    private Context mContext;
    private GyroscopeNewDataHandler dbNewData;
    private XYZ data = new XYZ();

    public GyroscopeListener(Context context){
        mContext = context;
        dbNewData = new GyroscopeNewDataHandler(mContext);
    }

    public GyroscopeNewDataHandler getDbNewData() {
        return dbNewData;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (SensorHandlerService.flagForGyro) {
            // Many sensors return 3 values, one for each axis
            data.setX(event.values[0]);
            data.setY(event.values[1]);
            data.setZ(event.values[2]);
            Log.d(LOGTAG,
                    String.valueOf(data.getX()) + "  " +
                    String.valueOf(data.getY()) + "  " +
                    String.valueOf(data.getZ()));
            dbNewData.addXYZ(data);
            SensorHandlerService.flagForGyro = false;
        }
    }
}
