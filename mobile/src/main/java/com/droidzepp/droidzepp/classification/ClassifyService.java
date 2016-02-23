package com.droidzepp.droidzepp.classification;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

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


/**
 * Created by nijat on 28/10/15.
 */
public class ClassifyService extends Service implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


//    private Handler hndlCheckForStart;
//    private Handler hndlClassify;
    private static final String DATA_KEY = "droidzepp.wear.data";
    private GoogleApiClient mGoogleApiClient;
    final Messenger messageReceiver = new Messenger(new IncomingHandler());
    ArrayList<Messenger> messageSender = new ArrayList<>();

    public static final int MSG_START_RECORDING = 3;
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_COMBINING_DONE = 5;
    public static int MSG_LABEL_ID = 10;

    private static String URLS = "http://asdfnijat.azurewebsites.net/Service.asmx";
    private static String NAMESPACE = "http://tempuri.org/";
    private static String SOAP_ACTION = "http://tempuri.org/";
    private static String METHOD_NAME = "Classify";

    private static String LOGTAG = "ClassifyService";


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
//            //extractfeatures();
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
    public void onLowMemory(){
  //      hndlCheckForStart.removeCallbacks(prcsCheckForStart);
        super.onLowMemory();
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
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public long extractfeatures(ArrayList<DataMap> wData){
        Log.d(LOGTAG, "New data is received and it will be combined with existing data");
        AccelerometerNewDataHandler dbAccNewData = new AccelerometerNewDataHandler(getApplicationContext());
        GyroscopeNewDataHandler dbGyroNewData = new GyroscopeNewDataHandler(getApplicationContext());
        ActionsDatabase actionsDB = new ActionsDatabase(getApplicationContext());

        List<XYZwithTime> mDataAcc = dbAccNewData.getAllData();
        List<XYZ> mDataGyro = dbGyroNewData.getAllData();
        FeatureContainer extractedFeatures = new FeatureContainer();

        long actionLabelID = actionsDB.addNewLabel("unknown action");
        Log.d(LOGTAG, "Most recent action label: " + actionLabelID);
        int sizeOfDataSetToBeAdded = Math.min(Math.min(mDataAcc.size(),mDataGyro.size()), wData.size());


        for (int i = 0; i < sizeOfDataSetToBeAdded; i++){
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
        actionLabelID += 11;
        sendMessageToMainActivity((int) actionLabelID);
        sendMessageToMainActivity(MSG_COMBINING_DONE);
        return actionLabelID;
    }
    void classify(int lIdToTest) {
        ActionsDatabase actionsDB = new ActionsDatabase(getApplicationContext());
        double[][][] dataset = actionsDB.getDataSet();
        int[] labels = actionsDB.getLabels();

//        List<XYZwithTime> accDataList = dbAccNewData.getAllData();
//        List<XYZ> gyroDataList = dbGyroNewData.getAllData();
//        double[][] testData = new double[145][6];
//
//
//        for (int i = 0; i < 145; i++){
//            testData[i][0]=(double)accDataList.get(i).getX();
//            testData[i][1]=(double)accDataList.get(i).getY();
//            testData[i][2]=(double)accDataList.get(i).getZ();
//            testData[i][3]=(double)gyroDataList.get(i).getX();
//            testData[i][4]=(double)gyroDataList.get(i).getY();
//            testData[i][5]=(double)gyroDataList.get(i).getZ();
//        }

        String resTxt = null;
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);


        PropertyInfo pi = new PropertyInfo();
        pi.setName("dataset");
        pi.setValue(dataset);
        pi.type = double[][][].class;
        request.addProperty(pi);

        PropertyInfo pi2 = new PropertyInfo();
        pi2.setName("labels");
        pi2.setValue(labels);
        pi2.type = int[].class;
        request.addProperty(pi2);

        PropertyInfo pi3 = new PropertyInfo();
        pi3.setName("testData");
 //       pi3.setValue(testData);
        pi3.type = double[][].class;
        request.addProperty(pi3);

        // Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);

        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        new MarshalDouble3D().register(envelope);

        new MarshalInt1D().register(envelope);

        new MarshalDouble2D().register(envelope);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        // Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URLS);

        androidHttpTransport.debug = true;
        try {
            // Invoke web service
            androidHttpTransport.call(SOAP_ACTION+METHOD_NAME, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to resTxt variable static variable
            resTxt = response.toString();

        } catch (Exception e) {
            //Print error
            e.printStackTrace();
            //Assign error message to resTxt
            resTxt = "Error occured";
        }
        //Return resTxt to calling object
        Log.d("Result: ", resTxt);
        Toast.makeText(this, resTxt, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("droidzepp.mobile.data", "New data event happened");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/wearData") == 0) {
                    Log.d("droidzepp.mobile.data", "New data came from wear");
                    extractfeatures(DataMapItem.fromDataItem(item).getDataMap().getDataMapArrayList(DATA_KEY));
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("droidzepp.mobile", "Connection failed");
    }

    private void sendMessageToMainActivity(int message){
        for (int i=messageSender.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                messageSender.get(i).send(Message.obtain(null, message));
            }
            catch (RemoteException e) {
                messageSender.remove(i);
            }
        }
    }
}
