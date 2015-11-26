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

import java.util.ArrayList;
import java.util.List;

import Catalano.MachineLearning.Classification.MulticlassSupportVectorMachine;
import Catalano.Statistics.Kernels.Gaussian;

/**
 * Created by nijat on 28/10/15.
 */
public class ClassifyService extends Service {


    private Handler hndlCheckForStart;
    private Handler hndlClassify;
    MulticlassSupportVectorMachine svm = new MulticlassSupportVectorMachine(new Gaussian(100), 1, 3);

    int recheckingInterval = 10000;

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
            //extractfeatures();
            classify();
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        ActionsDatabase actionsDB = new ActionsDatabase(getApplicationContext());
        double[][] input = actionsDB.getDataSet();
        int[] output = actionsDB.getLabels();
        svm.Learn(input, output);

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
    void classify(){
        AccelerometerNewDataHandler dbAccNewData = new AccelerometerNewDataHandler(getApplicationContext());
        GyroscopeNewDataHandler dbGyroNewData = new GyroscopeNewDataHandler(getApplicationContext());


        List<XYZwithTime> accDataList = dbAccNewData.getAllData();
        List<XYZ> gyroDataList = dbGyroNewData.getAllData();
        ArrayList data = new ArrayList();
        double[] testData = new double[870];


        for (int i = 0; i < Math.min(accDataList.size(), gyroDataList.size()); i++){
            data.add((double)accDataList.get(i).getX());
            data.add((double)accDataList.get(i).getY());
            data.add((double)accDataList.get(i).getZ());
            data.add((double)gyroDataList.get(i).getX());
            data.add((double)gyroDataList.get(i).getY());
            data.add((double)gyroDataList.get(i).getZ());
        }
        for (int i=0;i<870;i++)
        {
            testData[i] = (double) data.get(i);
        }

        int p = svm.Predict(testData);
        Log.d("Predicted: ", String.valueOf(p));

    }
}
