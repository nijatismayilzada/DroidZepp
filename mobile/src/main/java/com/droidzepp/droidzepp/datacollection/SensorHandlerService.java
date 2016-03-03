package com.droidzepp.droidzepp.datacollection;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.droidzepp.droidzepp.HelperFunctions;
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
    private AccelerometerListener mAccEventListener;
    private GyroscopeListener mGyroEventListener;
    private Handler hndlStartRecording;
    private Handler hndlRecording;
    private Handler hndlEndRecording;
    private ExecutorService executorService;
    //private int recordingInterval = 60000;  // 1200000 = 20 minutes
    private int recordingLength = 15000;  //60000 = 1 minute
    private int sensorDelay = 200;

    private static final String LOGTAG = "SensorHandlerService";
    private static final String START_KEY = "droidzepp.start";
    private static final int CLIENT_CONNECTION_TIMEOUT = 10000;
    private GoogleApiClient mGoogleApiClient;

    final Messenger messageReceiver = new Messenger(new IncomingHandler());
    ArrayList<Messenger> messageSender = new ArrayList<>();

    public static boolean flagForAcc = false;
    public static boolean flagForGyro = false;
    public static boolean newDataRecorded = false;
    public static boolean hiddenRecording = false;


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
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                            }
                        });
                    }

                    putDataMapReq.getDataMap().putBoolean(START_KEY, true);
                    putDataReq = putDataMapReq.asPutDataRequest();
                    putDataReq.setUrgent();
                    if (validateConnection()) {
                        Log.d(LOGTAG, "Connection to wearable is okay");
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                                Log.d(LOGTAG, "Status of sending start message: " + dataItemResult.getStatus().isSuccess());
                            }
                        });
                    }

                }
            });
            Log.d(LOGTAG, "Recording started");
            mAccEventListener.getDbNewData().openWritableDB();
            mGyroEventListener.getDbNewData().openWritableDB();
            mAccEventListener.getDbNewData().clearTable();
            mGyroEventListener.getDbNewData().clearTable();
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
            hndlRecording.removeCallbacks(prcsRecording);
            mSensorManager.unregisterListener(mAccEventListener, mAccelerometer);
            mSensorManager.unregisterListener(mGyroEventListener, mGyroscope);
            mAccEventListener.getDbNewData().closeDB();
            mGyroEventListener.getDbNewData().closeDB();
            hndlEndRecording.removeCallbacks(prcsEndRecording);
            newDataRecorded = true;
            if(hiddenRecording) {
                sendMessageToMainActivity(HelperFunctions.MSG_RECORDING_DONE_HIDDEN);
            } else{
                sendMessageToMainActivity(HelperFunctions.MSG_RECORDING_DONE);
            }
            Log.d(LOGTAG, "End of recording");
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOGTAG, "Binding...");
        return messageReceiver.getBinder();
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
        hndlRecording = new Handler();
        hndlEndRecording = new Handler();
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean validateConnection() {
        if (mGoogleApiClient.isConnected())
            return true;
        ConnectionResult result = mGoogleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        return result.isSuccess();
    }

    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HelperFunctions.MSG_REGISTER_CLIENT:
                    messageSender.add(msg.replyTo);
                    break;
                case HelperFunctions.MSG_UNREGISTER_CLIENT:
                    messageSender.remove(msg.replyTo);
                    break;
                case HelperFunctions.MSG_START_RECORDING:
                    hiddenRecording = false;
                    hndlStartRecording.post(prcsStartRecording);
                    break;
                case HelperFunctions.MSG_START_RECORDING_HIDDEN:
                    hiddenRecording = true;
                    hndlStartRecording.post(prcsStartRecording);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMessageToMainActivity(final int message) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    messageSender.get(0).send(Message.obtain(null, message));
                } catch (RemoteException e) {
                    messageSender.remove(0);
                }
            }
        });
    }

    private void sendMessageToMainActivity(final int message, final Object a) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    messageSender.get(0).send(Message.obtain(null, message, a));
                } catch (RemoteException e) {
                    messageSender.remove(0);
                }
            }
        });
    }
}