package com.droidzepp.droidzepp.classification;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.droidzepp.droidzepp.datacollection.AccelerometerNewDataHandler;
import com.droidzepp.droidzepp.datacollection.GyroscopeNewDataHandler;
import com.droidzepp.droidzepp.datacollection.SensorHandlerService;
import com.droidzepp.droidzepp.datacollection.XYZ;
import com.droidzepp.droidzepp.datacollection.XYZwithTime;

import java.util.List;

/**
 * Created by nijat on 28/10/15.
 */
public class ClassifyService extends Service {


    private Handler hndlCheckForStart;
    private Handler hndlClassify;

    int recheckingInterval = 2000;

    private final Runnable prcsCheckForStart = new Runnable() {
        @Override
        public void run() {
            if (SensorHandlerService.newDataRecorded){
                Log.d("p", "Data checking started");
                hndlClassify.post(prcsClassify);
                SensorHandlerService.newDataRecorded = false;
                hndlCheckForStart.postDelayed(prcsCheckForStart, recheckingInterval);
            } else{
                hndlCheckForStart.postDelayed(prcsCheckForStart, recheckingInterval);
            }
        }
    };

    private final Runnable prcsClassify = new Runnable() {
        @Override
        public void run() {
            extractfeatures();
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        hndlCheckForStart = new Handler();
        hndlClassify = new Handler();
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

    void extractfeatures(){
        AccelerometerNewDataHandler dbAccNewData = new AccelerometerNewDataHandler(getApplicationContext());
        GyroscopeNewDataHandler dbGyroNewData = new GyroscopeNewDataHandler(getApplicationContext());
        ActionsDatabase actionsDB = new ActionsDatabase(getApplicationContext());

        List<XYZwithTime> accDataList = dbAccNewData.getAllData();
        List<XYZ> gyroDataList = dbGyroNewData.getAllData();
        FeatureContainer newFeatures = new FeatureContainer();

        long actionLabelID = actionsDB.addNewLabel("unknown action");

        for (int i = 0; i < Math.min(accDataList.size(), gyroDataList.size()); i++){
            newFeatures.setTime(accDataList.get(i).getTime());
            newFeatures.setA(accDataList.get(i).getX());
            newFeatures.setB(accDataList.get(i).getY());
            newFeatures.setC(accDataList.get(i).getZ());
            newFeatures.setD(gyroDataList.get(i).getX());
            newFeatures.setE(gyroDataList.get(i).getY());
            newFeatures.setF(gyroDataList.get(i).getZ());
            newFeatures.setLid(actionLabelID);
            actionsDB.addFeatures(newFeatures);
        }
        actionsDB.addFeatures(newFeatures);

    }
}
