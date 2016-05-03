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
import com.droidzepp.droidzepp.datacollection.DataCollectionService;
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

public class SendToClassifyService extends Service implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Handler hndlCheckForStart;
    private Handler hndlSendToClassify;
    private ExecutorService executorService;

    private static final String DATA_KEY = "droidzepp.wear.data";
    private static final int CLIENT_CONNECTION_TIMEOUT = 10000;
    private static int RECHECKING_INTERVAL = 1000;
    private GoogleApiClient mGoogleApiClient;
    private static final String LOGTAG = "SendToClassifyService";


    private final Runnable prcsCheckForStart = new Runnable() {
        @Override
        public void run() {
            if (DataCollectionService.newDataRecorded){
                hndlSendToClassify.post(prcsClassify);
                DataCollectionService.newDataRecorded = false;
            }
            hndlCheckForStart.postDelayed(prcsCheckForStart, RECHECKING_INTERVAL);
        }
    };

    private final Runnable prcsClassify = new Runnable() {
        @Override
        public void run() {
            extractFeaturesAndSend();
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

    private void extractFeaturesAndSend(){
        AccelerometerNewDataHandler dbAccNewData = new AccelerometerNewDataHandler(getApplicationContext());
        GyroscopeNewDataHandler dbGyroNewData = new GyroscopeNewDataHandler(getApplicationContext());
        dbAccNewData.openReadableDB();
        dbGyroNewData.openReadableDB();
        List<XYZ> accDataList = dbAccNewData.getAllData();
        List<XYZ> gyroDataList = dbGyroNewData.getAllData();
        dbAccNewData.closeDB();
        dbGyroNewData.closeDB();
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
        Log.d(LOGTAG, "Size of data: " + dataMapList.size());

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/wearData");
                putDataMapReq.getDataMap().putDataMapArrayList(DATA_KEY, dataMapList);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                putDataReq.setUrgent();
                if (validateConnection()) {
                    Log.d(LOGTAG, "Connection is okay");
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Log.d(LOGTAG, "Status of sending recorded data: " + dataItemResult.getStatus().isSuccess());
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
        Log.d(LOGTAG, "Connection failed");
    }

    private boolean validateConnection() {
        if (mGoogleApiClient.isConnected())
            return true;
        ConnectionResult result = mGoogleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        return result.isSuccess();
    }
}