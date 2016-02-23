package com.droidzepp.droidzepp;

import android.app.DialogFragment;
import android.app.ProgressDialog;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.droidzepp.droidzepp.classification.ActionsDatabase;
import com.droidzepp.droidzepp.classification.ClassifyService;
import com.droidzepp.droidzepp.datacollection.SensorHandlerService;
import com.droidzepp.droidzepp.uiclasses.ActionsListViewArrayAdapter;
import com.droidzepp.droidzepp.uiclasses.ClassificationDialogFragment;
import com.droidzepp.droidzepp.uiclasses.ConfirmationDialogFragment;

public class MainActivity extends AppCompatActivity implements ConfirmationDialogFragment.ConfirmationDialogListener, ClassificationDialogFragment.ClassificationDialogListener {

    ActionsDatabase actionsDatabase;
    static final int MSG_RECORDING_DONE = 4;
    static final int MSG_COMBINING_DONE = 5;
    static final int MSG_START_RECORDING = 3;
    static int recentRecordedActionID;
    private static final String LOGTAG = "MainActivity";

    ListView actionsListView;
    ProgressDialog progress;
    DialogFragment confirmation;
    DialogFragment classification;
    ActionsListViewArrayAdapter actionsListAdapter;

    // Messengers between back-end services and front-end
    final Messenger messageReceiver = new Messenger(new IncomingHandler());

    Messenger classifyServiceMessageSender;
    boolean mClassifyBound;

    Messenger sensorHandlerServiceMessageSender;
    boolean mSensorHandlerBound;

    private ServiceConnection classifyServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            classifyServiceMessageSender = new Messenger(service);
            mClassifyBound = true;
            try {
                Message msg = Message.obtain(null, ClassifyService.MSG_REGISTER_CLIENT);
                msg.replyTo = messageReceiver;
                classifyServiceMessageSender.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            classifyServiceMessageSender = null;
            mClassifyBound = false;
        }
    };


    private ServiceConnection sensorHandlerServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            sensorHandlerServiceMessageSender = new Messenger(service);
            mSensorHandlerBound = true;
            try {
                Message msg = Message.obtain(null, SensorHandlerService.MSG_REGISTER_CLIENT);
                msg.replyTo = messageReceiver;
                sensorHandlerServiceMessageSender.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            sensorHandlerServiceMessageSender = null;
            mSensorHandlerBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                confirmation = new ConfirmationDialogFragment(getApplicationContext());
                confirmation.show(getFragmentManager(), "Confirmation");
            }
        });
        actionsDatabase = new ActionsDatabase(this);
        actionsListView = (ListView) findViewById(R.id.actions_listview);
        actionsListAdapter = new ActionsListViewArrayAdapter(this, R.layout.actions_list_item, actionsDatabase.getRecordedActions());
        actionsListView.setAdapter(actionsListAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        bindService(sensorHandlerService, sensorHandlerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mClassifyBound) {
            unbindService(classifyServiceConnection);
            mClassifyBound = false;
        }
        if (mSensorHandlerBound) {
            unbindService(sensorHandlerServiceConnection);
            mSensorHandlerBound = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String addedLabel) {
        Log.d(LOGTAG, "Save clicked");
        actionsDatabase.updateLabel(recentRecordedActionID, addedLabel);
        actionsListAdapter.updateContent(actionsDatabase.getRecordedActions());
        actionsListView.invalidateViews();
        actionsListView.setAdapter(actionsListAdapter);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Log.d(LOGTAG, "Start clicked");
        progress = ProgressDialog.show(this, "DroidZepp", "Recording the action...", true, false);
        try {
            sensorHandlerServiceMessageSender.send(Message.obtain(null, MSG_START_RECORDING));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {
        Log.d(LOGTAG, "Neutral clicked");
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.d(LOGTAG, "Cancel clicked");
        actionsDatabase.deleteRecordedAction(recentRecordedActionID);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what > 10) {
                recentRecordedActionID = msg.what - 11;
            } else {
                switch (msg.what) {
                    case MSG_RECORDING_DONE:
                        Log.d(LOGTAG, "Recording done");
                        progress.dismiss();
                        progress = ProgressDialog.show(MainActivity.this, "DroidZepp", "Receiving wearable device data...", true, false);
                        break;
                    case MSG_COMBINING_DONE:
                        Log.d(LOGTAG, "Receiving and combining of wearable data is done");
                        progress.dismiss();
                        classification = new ClassificationDialogFragment(getApplicationContext());
                        classification.show(getFragmentManager(), "Classification");
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }
}
