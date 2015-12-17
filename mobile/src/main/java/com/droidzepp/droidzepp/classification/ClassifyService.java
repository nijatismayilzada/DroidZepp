package com.droidzepp.droidzepp.classification;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.droidzepp.droidzepp.datacollection.AccelerometerNewDataHandler;
import com.droidzepp.droidzepp.datacollection.GyroscopeNewDataHandler;
import com.droidzepp.droidzepp.datacollection.SensorHandlerService;
import com.droidzepp.droidzepp.datacollection.XYZ;
import com.droidzepp.droidzepp.datacollection.XYZwithTime;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.List;


/**
 * Created by nijat on 28/10/15.
 */
public class ClassifyService extends Service {


    private Handler hndlCheckForStart;
    private Handler hndlClassify;

    //Webservice URL - It is asmx file location hosted in the server in case of .Net
    //Change the IP address to your machine IP address
    private static String URLS = "http://asdfnijat.azurewebsites.net/Service.asmx";

    private static String NAMESPACE = "http://tempuri.org/";
    //SOAP Action URI again http://tempuri.org
    private static String SOAP_ACTION = "http://tempuri.org/";

    private static String METHOD_NAME = "Classify";


    int recheckingInterval = 1000;

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
    void classify() {
        AccelerometerNewDataHandler dbAccNewData = new AccelerometerNewDataHandler(getApplicationContext());
        GyroscopeNewDataHandler dbGyroNewData = new GyroscopeNewDataHandler(getApplicationContext());
        ActionsDatabase actionsDB = new ActionsDatabase(getApplicationContext());
        double[][][] dataset = actionsDB.getDataSet();
        int[] labels = actionsDB.getLabels();

        List<XYZwithTime> accDataList = dbAccNewData.getAllData();
        List<XYZ> gyroDataList = dbGyroNewData.getAllData();
        double[][] testData = new double[145][6];


        for (int i = 0; i < 145; i++){
            testData[i][0]=(double)accDataList.get(i).getX();
            testData[i][1]=(double)accDataList.get(i).getY();
            testData[i][2]=(double)accDataList.get(i).getZ();
            testData[i][3]=(double)gyroDataList.get(i).getX();
            testData[i][4]=(double)gyroDataList.get(i).getY();
            testData[i][5]=(double)gyroDataList.get(i).getZ();
        }

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
        pi3.setValue(testData);
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
}
