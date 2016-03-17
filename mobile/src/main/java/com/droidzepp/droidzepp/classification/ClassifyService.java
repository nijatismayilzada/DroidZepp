package com.droidzepp.droidzepp.classification;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.droidzepp.droidzepp.HelperFunctions;
import com.droidzepp.droidzepp.datacollection.AccelerometerNewDataHandler;
import com.droidzepp.droidzepp.datacollection.GyroscopeNewDataHandler;
import com.droidzepp.droidzepp.datacollection.XYZ;
import com.droidzepp.droidzepp.datacollection.XYZwithTime;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClassifyService extends Service implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    //    private Handler hndlCheckForStart;
//    private Handler hndlClassify;
    private static final String DATA_KEY = "droidzepp.wear.data";
    private GoogleApiClient mGoogleApiClient;
    final Messenger messageReceiver = new Messenger(new IncomingHandler());
    ArrayList<Messenger> messageSender = new ArrayList<>();

    public static int recentRecordedActionID;
    ExecutorService executorService;

    private static String URLS = "http://droidzepp.azurewebsites.net/Service.asmx";
    private static String NAMESPACE = "http://tempuri.org/";
    private static String SOAP_ACTION = "http://tempuri.org/";
    private static String METHOD_NAME = "Classify";

    private static String LOGTAG = "ClassifyService";
    private static boolean hiddenClassify = false;


//    int recheckingInterval = 1000;

    //    private final Runnable prcsCheckForStart = new Runnable() {
//        @Override
//        public void run() {
//            if (SensorHandlerService.newDataRecorded){
//                Log.d("p", "Data checking started");
//                hndlClassify.post(prcsClassify);
//                SensorHandlerService.newDataRecorded = false;
//                hndlCheckForStart.postDelayed(prcsCheckForStart, recheckingInterval);
//            } else{
//                hndlCheckForStart.postDelayed(prcsCheckForStart, recheckingInterval);
//            }
//        }
//    };
//
//    private final Runnable prcsClassify = new Runnable() {
//        @Override
//        public void run() {
//            //extractFeatures();
//            //classify();
//        }
//    };
    @Override
    public IBinder onBind(Intent intent) {
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
//        hndlCheckForStart = new Handler();
//        hndlClassify = new Handler();
//        hndlCheckForStart.post(prcsCheckForStart);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        //       hndlCheckForStart.removeCallbacks(prcsCheckForStart);
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        //      hndlCheckForStart.removeCallbacks(prcsCheckForStart);
        super.onLowMemory();
    }

    public long extractFeatures(ArrayList<DataMap> wData) {
        Log.d(LOGTAG, "New data is received and it will be combined with existing data");
        AccelerometerNewDataHandler dbAccNewData = new AccelerometerNewDataHandler(getApplicationContext());
        GyroscopeNewDataHandler dbGyroNewData = new GyroscopeNewDataHandler(getApplicationContext());
        ActionsDatabase actionsDB = new ActionsDatabase(getApplicationContext());
        actionsDB.openWritableDB();
        dbAccNewData.openReadableDB();
        dbGyroNewData.openReadableDB();
        List<XYZwithTime> mDataAcc = dbAccNewData.getAllData();
        List<XYZ> mDataGyro = dbGyroNewData.getAllData();
        dbAccNewData.closeDB();
        dbGyroNewData.closeDB();
        FeatureContainer extractedFeatures = new FeatureContainer();

        long actionLabelID = actionsDB.addNewLabel("unknown action");
        Log.d(LOGTAG, "Most recent action label: " + actionLabelID);
        int sizeOfDataSetToBeAdded = Math.min(Math.min(mDataAcc.size(), mDataGyro.size()), wData.size());


        for (int i = 0; i < sizeOfDataSetToBeAdded; i++) {
            extractedFeatures.setTime(mDataAcc.get(i).getTime());
            extractedFeatures.setAccMX(mDataAcc.get(i).getX());
            extractedFeatures.setAccMY(mDataAcc.get(i).getY());
            extractedFeatures.setAccMZ(mDataAcc.get(i).getZ());
            extractedFeatures.setGyroMX(mDataGyro.get(i).getX());
            extractedFeatures.setGyroMY(mDataGyro.get(i).getY());
            extractedFeatures.setGyroMZ(mDataGyro.get(i).getZ());
            extractedFeatures.setAccWX(wData.get(i).getFloat("accWX"));
            extractedFeatures.setAccWY(wData.get(i).getFloat("accWY"));
            extractedFeatures.setAccWZ(wData.get(i).getFloat("accWZ"));
            extractedFeatures.setGyroWX(wData.get(i).getFloat("gyroWX"));
            extractedFeatures.setGyroWY(wData.get(i).getFloat("gyroWY"));
            extractedFeatures.setGyroWZ(wData.get(i).getFloat("gyroWZ"));
            extractedFeatures.setlId(actionLabelID);
            actionsDB.addFeatures(extractedFeatures);
        }
        actionsDB.closeDB();
        if (hiddenClassify) {
            sendMessageToMainActivity(HelperFunctions.MSG_COMBINING_DONE_HIDDEN, actionLabelID);
        } else {
            sendMessageToMainActivity(HelperFunctions.MSG_COMBINING_DONE, actionLabelID);
        }
        return actionLabelID;
    }

    public void classify(int lIdToTest) {
        new DroidAsyncTask().execute(lIdToTest);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(LOGTAG, "New data event happened");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/wearData") == 0) {
                    Log.d(LOGTAG, "New data came from wear");
                    extractFeatures(DataMapItem.fromDataItem(item).getDataMap().getDataMapArrayList(DATA_KEY));
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOGTAG, "Wearable connection failed");
    }

    class DroidAsyncTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            ActionsDatabase actionsDB = new ActionsDatabase(getApplicationContext());
            actionsDB.openReadableDB();
            int lIdToTest = params[0];
            Log.d(LOGTAG, "lId to test: " + lIdToTest);
            double[][][] trainDataSet = actionsDB.getDataSet(lIdToTest);

//            double[][] newtrain = new double[trainDataSet.length][trainDataSet[0].length * trainDataSet[0][0].length];
//            int counter = 0;
//            for (int a = 0; a<trainDataSet.length; a++)
//            {
//                for(int b = 0; b< trainDataSet[0].length; b++)
//                {
//                    for (int c = 0; c<trainDataSet[0][0].length; c++){
//                        newtrain[a][counter]=trainDataSet[a][b][c];
//                        counter++;
//                    }
//                }
//                counter =0;
//            }

            int[] trainLabels = actionsDB.getLabels(lIdToTest);
            double[][] testData = actionsDB.getTestData(lIdToTest);

//            double[] newTest = new double[testData.length * testData[0].length];
//
//            counter = 0;
//            for (int a = 0; a< testData.length; a++){
//                for(int b=0; b<testData[0].length; b++){
//                    newTest[counter] = testData[a][b];
//                    counter++;
//                }
//            }

            String[] classes = actionsDB.getClasses(lIdToTest);
            actionsDB.closeDB();

//            MulticlassSupportVectorMachine svm = new MulticlassSupportVectorMachine(new Gaussian(800), 1, classes.length);
//            svm.Learn(newtrain, trainLabels);
//
//            String resTxt = String.valueOf(svm.Predict(newTest));
//            Log.d(LOGTAG, resTxt);
//            return resTxt;

            String resTxt = "";
            // Create request
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            PropertyInfo pi = new PropertyInfo();
            pi.setName("trainDataSet");
            pi.setValue(trainDataSet);
            pi.type = double[][][].class;
            request.addProperty(pi);

            PropertyInfo pi2 = new PropertyInfo();
            pi2.setName("trainLabels");
            pi2.setValue(trainLabels);
            pi2.type = int[].class;
            request.addProperty(pi2);

            PropertyInfo pi3 = new PropertyInfo();
            pi3.setName("testData");
            pi3.setValue(testData);
            pi3.type = double[][].class;
            request.addProperty(pi3);

            PropertyInfo pi4 = new PropertyInfo();
            pi4.setName("classes");
            pi4.setValue(classes);
            pi4.type = String[].class;
            request.addProperty(pi4);

            // Create envelope
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            new MarshalDouble3D().register(envelope);
            new MarshalInt1D().register(envelope);
            new MarshalDouble2D().register(envelope);
            new MarshalString1D().register(envelope);

            // Create HTTP call object
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URLS);
            androidHttpTransport.debug = true;
            try {
                // Invoke web service
                androidHttpTransport.call(SOAP_ACTION + METHOD_NAME, envelope);
                // Get the response
                SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                // Assign it to resTxt variable static variable
                resTxt = response.toString();
            } catch (Exception e) {
                //Print error
                e.printStackTrace();
                //Assign error message to resTxt
                resTxt = resTxt + "Error occurred";
            }
            //Return resTxt to calling object
            Log.d(LOGTAG, "Result of classifier: " + resTxt);
            return resTxt;
        }

        @Override
        protected void onPostExecute(String resTxt) {
            if (hiddenClassify){
                sendMessageToMainActivity(HelperFunctions.MSG_CLASSIFIER_RESULT_HIDDEN, resTxt);
            }else{
                sendMessageToMainActivity(HelperFunctions.MSG_CLASSIFIER_RESULT, resTxt);
            }
        }
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
                case HelperFunctions.MSG_START_RECORDING_HIDDEN:
                    hiddenClassify = true;
                    break;
                case HelperFunctions.MSG_START_RECORDING:
                    hiddenClassify = false;
                    break;
                case HelperFunctions.MSG_START_CLASSIFICATION:
                    recentRecordedActionID = (int) msg.obj;
                    classify(recentRecordedActionID);
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
