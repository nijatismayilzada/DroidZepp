package com.droidzepp.droidzepp.datacollection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class SensorHandlerService extends Service implements DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private SensorManager mSensorManager;
    private SensorEventListener mAccEventListener;
    private SensorEventListener mGyroEventListener;
    private Handler hndlStartRecording;
    private Handler hndlRecording;
    private Handler hndlEndRecording;
    private AccelerometerNewDataHandler dbNewAccData;
    private GyroscopeNewDataHandler dbNewGyroData;
    private int recordingInterval = 60000;  // 1200000 = 20 minutes
    private int recordingLength = 15000;  //60000 = 1 minute
    private int sensorDelay = 200;

    private static final String START_KEY = "droidzepp.start";
    private GoogleApiClient mGoogleApiClient;

    public static boolean flagForAcc = false;
    public static boolean flagForGyro = false;
    public static boolean newDataRecorded = false;

    private final Runnable prcsStartRecording = new Runnable() {
        @Override
        public void run() {

            PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/prcsStartRecording");
            putDataMapReq.getDataMap().putBoolean(START_KEY, true);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    Log.d("droidzepp.mob.start", "Sending start message: " + dataItemResult.getStatus().isSuccess());
                }
            });

            Log.d("p", "Recording started");
            dbNewAccData.clearTable();
            dbNewGyroData.clearTable();
            mSensorManager.registerListener(mAccEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(mGyroEventListener, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
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
        dbNewAccData = new AccelerometerNewDataHandler(this);
        dbNewGyroData = new GyroscopeNewDataHandler(this);

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
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}