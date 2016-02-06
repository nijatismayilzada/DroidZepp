package com.droidzepp.droidzepp.sendtoclassify;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.droidzepp.droidzepp.datacollection.AccelerometerNewDataHandler;
import com.droidzepp.droidzepp.datacollection.GyroscopeNewDataHandler;
import com.droidzepp.droidzepp.datacollection.SensorHandlerService;
import com.droidzepp.droidzepp.datacollection.XYZ;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by nijat on 03/02/16.
 */
public class SendToClassifyService extends Service implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Handler hndlCheckForStart;
    private Handler hndlSendToClassify;
    private ExecutorService executorService;

    private static final String DATA_KEY = "droidzepp.wear.data";
    private static final int CLIENT_CONNECTION_TIMEOUT = 10000;
    private static int RECHECKING_INTERVAL = 1000;
    private GoogleApiClient mGoogleApiClient;


    private final Runnable prcsCheckForStart = new Runnable() {
        @Override
        public void run() {
            if (SensorHandlerService.newDataRecorded){
                Log.d("droidzepp.wear", "Data checking started");
                hndlSendToClassify.post(prcsClassify);
                SensorHandlerService.newDataRecorded = false;
            }
            hndlCheckForStart.postDelayed(prcsCheckForStart, RECHECKING_INTERVAL);
        }
    };

    private final Runnable prcsClassify = new Runnable() {
        @Override
        public void run() {
            extractfeaturesAndSend();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        hndlCheckForStart = new Handler();
        hndlSendToClassify = new Handler();
        hndlCheckForStart.post(prcsCheckForStart);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        hndlCheckForStart.removeCallbacks(prcsCheckForStart);
        super.onDestroy();
    }

    @Override
    public void onLowMemory(){
        hndlCheckForStart.removeCallbacks(prcsCheckForStart);
        super.onLowMemory();
    }

    private void extractfeaturesAndSend(){
        AccelerometerNewDataHandler dbAccNewData = new AccelerometerNewDataHandler(getApplicationContext());
        GyroscopeNewDataHandler dbGyroNewData = new GyroscopeNewDataHandler(getApplicationContext());

        List<XYZ> accDataList = dbAccNewData.getAllData();
        List<XYZ> gyroDataList = dbGyroNewData.getAllData();
        FeatureContainerToSend newFeatures = new FeatureContainerToSend();
        final ArrayList<DataMap> dataMapList = new ArrayList<>();

        for (int i = 0; i < Math.min(accDataList.size(), gyroDataList.size()); i++) {
            newFeatures.setAccWX(accDataList.get(i).getX());
            newFeatures.setAccWY(accDataList.get(i).getY());
            newFeatures.setAccWZ(accDataList.get(i).getZ());
            newFeatures.setGyroWX(gyroDataList.get(i).getX());
            newFeatures.setGyroWY(gyroDataList.get(i).getY());
            newFeatures.setGyroWZ(gyroDataList.get(i).getZ());
            dataMapList.add(newFeatures.putToDataMap(new DataMap()));
        }
        Log.d("droidzepp.wear.data", "Size of data: " + dataMapList.size());

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/wearData");
//                putDataMapReq.getDataMap().putDataMapArrayList(DATA_KEY, new ArrayList<DataMap>());
//                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
//                if (validateConnection()) {
//                    Log.d("droidzepp.wear", "connection is okay");
//                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
//                        @Override
//                        public void onResult(DataApi.DataItemResult dataItemResult) {
//                            Log.d("droidzepp.wear.send", "Sending 0 recorded data: " + dataItemResult.getStatus().isSuccess());
//                        }
//                    });
//                }

                putDataMapReq.getDataMap().putDataMapArrayList(DATA_KEY, dataMapList);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                if (validateConnection()) {
                    Log.d("droidzepp.wear", "connection is okay");
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Log.d("droidzepp.wear.send", "Sending recorded data: " + dataItemResult.getStatus().isSuccess());
                        }
                    });
                }

            }
        });

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
        Log.d("droidzepp.wear", "Connection failed");
    }

    private boolean validateConnection() {
        if (mGoogleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = mGoogleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }
}