package com.droidzepp.droidzepp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.droidzepp.droidzepp.classification.ActionsDatabase;
import com.droidzepp.droidzepp.classification.ClassifyService;
import com.droidzepp.droidzepp.datacollection.SensorHandlerService;

public class MainActivity extends AppCompatActivity {

    ActionsDatabase actionsDatabase;
    static final int MSG_RECORDING_DONE = 4;
    ListView actionsListView;
    ActionArrayAdapter actionsListAdapter;

    Messenger classifyServiceMessageSender = null;
    boolean mClassifyBound;

//    Messenger sensorHandlerServiceMessageSender = null;
//    boolean mSensorHandlerBound;

    final Messenger messageReceiver = new Messenger(new IncomingHandler());

    private ServiceConnection classifyServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            classifyServiceMessageSender = new Messenger(service);
            mClassifyBound = true;
            try {
                Message msg = Message.obtain(null, ClassifyService.MSG_REGISTER_CLIENT);
                msg.replyTo = messageReceiver;
                classifyServiceMessageSender.send(msg);
            }
            catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            classifyServiceMessageSender = null;
            mClassifyBound = false;
        }
    };

//    private ServiceConnection sensorHandlerServiceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            sensorHandlerServiceMessageSender = new Messenger(service);
//            mSensorHandlerBound = true;
//            try {
//                Message msg = Message.obtain(null, SensorHandlerService.MSG_REGISTER_CLIENT);
//                msg.replyTo = messageReceiver;
//                sensorHandlerServiceMessageSender.send(msg);
//            }
//            catch (RemoteException e) {
//                // In this case the service has crashed before we could even do anything with it
//            }
//
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            sensorHandlerServiceMessageSender = null;
//            mSensorHandlerBound = false;
//        }
//    };

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECORDING_DONE:
                    Log.d("droidzepp.mob.main", "Recording done");
                    actionsListAdapter.updateContent(actionsDatabase.getRecordedActions());
                    actionsListView.invalidateViews();
                    actionsListView.setAdapter(actionsListAdapter);
                    Log.d("droidzepp.mob.main", "Adapter updated");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        actionsDatabase = new ActionsDatabase(this);
        actionsListView = (ListView) findViewById(R.id.recordedActionsList);
        actionsListAdapter = new ActionArrayAdapter(this, R.layout.listitem, actionsDatabase.getRecordedActions());
        actionsListView.setAdapter(actionsListAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        Intent sensorHandlerService = new Intent(this, SensorHandlerService.class);
        Intent classifyService = new Intent(this, ClassifyService.class);
        startService(sensorHandlerService);
        startService(classifyService);
        bindService(classifyService, classifyServiceConnection, Context.BIND_AUTO_CREATE);
//        bindService(sensorHandlerService, sensorHandlerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mClassifyBound) {
            unbindService(classifyServiceConnection);
            mClassifyBound = false;
        }
//        if(mSensorHandlerBound){
//            unbindService(sensorHandlerServiceConnection);
//            mSensorHandlerBound = false;
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
