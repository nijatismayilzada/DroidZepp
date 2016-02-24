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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SensorHandlerService extends Service implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private SensorManager mSensorManager;
    private SensorEventListener mAccEventListener;
    private SensorEventListener mGyroEventListener;
    private Handler hndlStartRecording;
    private Handler hndlRecording;
    private Handler hndlEndRecording;
    private ExecutorService executorService;
    private AccelerometerNewDataHandler dbNewAccData;
    private GyroscopeNewDataHandler dbNewGyroData;
    private int recordingInterval = 60000;  // 1200000 = 20 minutes
    private int recordingLength = 15000;  //60000 = 1 minute
    private int sensorDelay = 200;


    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_START_RECORDING = 3;
    public static final int MSG_RECORDING_DONE = 4;

    private static final String START_KEY = "droidzepp.start";
    private static final int CLIENT_CONNECTION_TIMEOUT = 10000;
    private GoogleApiClient mGoogleApiClient;

    final Messenger messageReceiver = new Messenger(new IncomingHandler());
    ArrayList<Messenger> messageSender = new ArrayList<>();

    public static boolean flagForAcc = false;
    public static boolean flagForGyro = false;
    public static boolean newDataRecorded = false;


    private final Runnable prcsStartRecording = new Runnable() {
        @Override
        public void run() {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/prcsStartRecording");
                    putDataMapReq.getDataMap().putBoolean(START_KEY, false);
                    PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                    putDataReq.setUrgent();
                    if (validateConnection()) {
                        Log.d("droidzepp.mob", "connection is okay");
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(DataApi.DataItemResult dataItemResult) {
                                Log.d("droidzepp.mob.start", "Sending start message: " + dataItemResult.getStatus().isSuccess());
                            }
                        });
                    }

                    putDataMapReq.getDataMap().putBoolean(START_KEY, true);
                    putDataReq = putDataMapReq.asPutDataRequest();
                    putDataReq.setUrgent();
                    if (validateConnection()) {
                        Log.d("droidzepp.mob", "connection is okay");
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(DataApi.DataItemResult dataItemResult) {
                                Log.d("droidzepp.mob.start", "Sending start message: " + dataItemResult.getStatus().isSuccess());
                            }
                        });
                    }

                }
            });
            Log.d("p", "Recording started");
            dbNewAccData.clearTable();
            dbNewGyroData.clearTable();
            mSensorManager.registerListener(mAccEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(mGyroEventListener, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
            hndlRecording.post(prcsRecording);
            hndlEndRecording.postDelayed(prcsEndRecording, recordingLength);
            //hndlStartRecording.postDelayed(prcsStartRecording, recordingInterval);
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

            for (int i=messageSender.size()-1; i>=0; i--) {
                try {
                    // Send data as an Integer
                    messageSender.get(i).send(Message.obtain(null, MSG_RECORDING_DONE));
                }
                catch (RemoteException e) {
                    messageSender.remove(i);
                }
            }

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("droidzepp.mob", "Binding...");
        return messageReceiver.getBinder();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    messageSender.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    messageSender.remove(msg.replyTo);
                    break;
                case MSG_START_RECORDING:
                    Log.d("droidzepp.mob.class", "Recording..");
                    hndlStartRecording.post(prcsStartRecording);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    @Override
    public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        executorService = Executors.newCachedThreadPool();
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

        //hndlStartRecording.post(prcsStartRecording);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        hndlStartRecording.removeCallbacks(prcsStartRecording);
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
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

    private boolean validateConnection() {
        if (mGoogleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = mGoogleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }
}