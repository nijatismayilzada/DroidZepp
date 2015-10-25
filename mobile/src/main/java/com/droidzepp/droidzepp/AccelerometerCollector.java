package com.droidzepp.droidzepp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class AccelerometerCollector extends Service implements SensorEventListener {

    private Sensor mAccelerometer;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;
    private Handler hndlStartRecording;
    private Handler hndlRecording;
    private Handler hndlEndRecording;
    private XYZ data;
    AccelerometerDatabaseHandler dbNewData;
    int recordingInterval = 25000;  // 1200000 = 20 minutes
    int recordingLength = 5000;  //60000 = 1 minute
    int sensorDelay = 500;
    boolean flag = false;

    private final Runnable prcsStartRecording = new Runnable() {
        @Override
        public void run() {
            Log.d("p", "Recording started");
            dbNewData.clearTable();
            mSensorManager.registerListener(mSensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            hndlRecording.post(prcsRecording);
            hndlEndRecording.postDelayed(prcsEndRecording, recordingLength);
            hndlStartRecording.postDelayed(prcsStartRecording, recordingInterval);
        }
    };

    private final Runnable prcsRecording = new Runnable() {
        @Override
        public void run() {
            flag = true;
            hndlRecording.postDelayed(prcsRecording, sensorDelay);
        }
    };

    private final Runnable prcsEndRecording = new Runnable() {
        @Override
        public void run() {
            Log.d("p", "End of recording");
            hndlRecording.removeCallbacks(prcsRecording);
            mSensorManager.unregisterListener(mSensorEventListener, mAccelerometer);
            hndlEndRecording.removeCallbacks(prcsEndRecording);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorEventListener = this;
        hndlStartRecording = new Handler();
        hndlEndRecording = new Handler();
        hndlRecording = new Handler();
        data = new XYZ();
        dbNewData = new AccelerometerDatabaseHandler(this);

        hndlStartRecording.post(prcsStartRecording);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        hndlStartRecording.removeCallbacks(prcsStartRecording);
        super.onDestroy();
    }

    @Override
    public void onLowMemory(){
        hndlStartRecording.removeCallbacks(prcsStartRecording);
        super.onLowMemory();
    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (flag) {

            // Many sensors return 3 values, one for each axis.
            data.setX(event.values[0]);
            data.setY(event.values[1]);
            data.setZ(event.values[2]);
            Log.d("a", String.valueOf(data.getX()) + "  " + String.valueOf(data.getY()) + "  " + String.valueOf(data.getZ()));
            dbNewData.addXYZ(data);
            // Do something with this sensor value.
            flag = false;
        }
    }
}