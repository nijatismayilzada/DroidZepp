package com.droidzepp.droidzepp.datacollection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class SensorHandlerService extends Service {

    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private SensorManager mSensorManager;
    private SensorEventListener mAccEventListener;
    private SensorEventListener mGyroEventListener;
    private Handler hndlStartRecording;
    private Handler hndlRecording;
    private Handler hndlEndRecording;
    //private AccelerometerNewDataHandler dbNewAccData;
    //private GyroscopeNewDataHandler dbNewGyroData;
    int recordingInterval = 1200000;  // 1200000 = 20 minutes
    int recordingLength = 60000;  //60000 = 1 minute
    int sensorDelay = 1000;
    public static boolean flagForAcc = false;
    public static boolean flagForGyro = false;
    public static boolean newDataRecorded = false;

    private final Runnable prcsStartRecording = new Runnable() {
        @Override
        public void run() {
            Log.d("p", "Recording started");
            //dbNewAccData.clearTable();
            //dbNewGyroData.clearTable();
            mSensorManager.registerListener(mAccEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mGyroEventListener, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            hndlRecording.post(prcsRecording);
            hndlEndRecording.postDelayed(prcsEndRecording, recordingLength);
            hndlStartRecording.postDelayed(prcsStartRecording, recordingInterval);
        }
    };

    private final Runnable prcsRecording = new Runnable() {
        @Override
        public void run() {
            flagForAcc = true;
            flagForGyro = true;
            hndlRecording.postDelayed(prcsRecording, sensorDelay);
        }
    };

    private final Runnable prcsEndRecording = new Runnable() {
        @Override
        public void run() {
            Log.d("p", "End of recording");
            hndlRecording.removeCallbacks(prcsRecording);
            mSensorManager.unregisterListener(mAccEventListener, mAccelerometer);
            mSensorManager.unregisterListener(mGyroEventListener, mGyroscope);
            hndlEndRecording.removeCallbacks(prcsEndRecording);
            newDataRecorded = true;
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
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccEventListener = new AccelerometerListener(getApplicationContext());
        mGyroEventListener = new GyroscopeListener(getApplicationContext());
        hndlStartRecording = new Handler();
        hndlEndRecording = new Handler();
        hndlRecording = new Handler();
        //dbNewAccData = new AccelerometerNewDataHandler(this);
        //dbNewGyroData = new GyroscopeNewDataHandler(this);

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
}