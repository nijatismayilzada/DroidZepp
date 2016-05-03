package com.droidzepp.droidzepp;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TimePicker;

import com.droidzepp.droidzepp.alarm.AlarmService;
import com.droidzepp.droidzepp.alarm.AlarmObject;
import com.droidzepp.droidzepp.classification.ActionsDatabase;
import com.droidzepp.droidzepp.classification.ClassifyService;
import com.droidzepp.droidzepp.datacollection.DataCollectionService;
import com.droidzepp.droidzepp.uiclasses.ActionsListViewArrayAdapter;
import com.droidzepp.droidzepp.uiclasses.AlarmFrequencyDialogFragment;
import com.droidzepp.droidzepp.uiclasses.ClassificationDialogFragment;
import com.droidzepp.droidzepp.uiclasses.ConfirmationDialogFragment;
import com.droidzepp.droidzepp.uiclasses.TimePickerFragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements ConfirmationDialogFragment.ConfirmationDialogListener,
        ClassificationDialogFragment.ClassificationDialogListener,
        TimePickerFragment.TimePickerListener,
        AlarmFrequencyDialogFragment.AlarmFrequencyDialogListener {

    ActionsDatabase actionsDatabase;

    static int recentRecordedActionID;
    static final String LOGTAG = "MainActivity";

    ListView actionsListView;
    ProgressDialog progress;
    DialogFragment confirmation;
    DialogFragment classification;
    DialogFragment alarmTimePicker;
    DialogFragment alarmFrequencyPicker;
    ActionsListViewArrayAdapter actionsListAdapter;
    ExecutorService executorService;

    // Messengers between back-end services and front-end
    final Messenger messageReceiver = new Messenger(new IncomingHandler());

    Messenger classifyServiceMessageSender;
    boolean mClassifyBound;

    Messenger sensorHandlerServiceMessageSender;
    boolean mSensorHandlerBound;

    Messenger alarmServiceMessageSender;
    boolean mAlarmBound;

    private ServiceConnection classifyServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            classifyServiceMessageSender = new Messenger(service);
            mClassifyBound = true;
            try {
                Message msg = Message.obtain(null, HelperFunctions.MSG_REGISTER_CLIENT);
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
                Message msg = Message.obtain(null, HelperFunctions.MSG_REGISTER_CLIENT);
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

    private ServiceConnection alarmServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            alarmServiceMessageSender = new Messenger(service);
            mAlarmBound = true;
            try {
                Message msg = Message.obtain(null, HelperFunctions.MSG_REGISTER_CLIENT);
                msg.replyTo = messageReceiver;
                alarmServiceMessageSender.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            alarmServiceMessageSender = null;
            mAlarmBound = false;
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
        actionsDatabase.openReadableDB();
        actionsListAdapter = new ActionsListViewArrayAdapter(this, R.layout.actions_list_item, actionsDatabase.getRecordedActions());
        actionsDatabase.closeDB();
        actionsListView.setAdapter(actionsListAdapter);
        registerForContextMenu(actionsListView);
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.actions_listview) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String selectedAction = ((ActionsListViewArrayAdapter.ViewHolder) actionsListAdapter.getView(info.position, null, null).getTag()).getActionName();
        switch (item.getItemId()) {
            case R.id.add:
                Log.d(LOGTAG, "Menu info: " + info.position);
                alarmTimePicker = new TimePickerFragment(getApplicationContext(), selectedAction);
                alarmTimePicker.show(getFragmentManager(), "Time Picker");
                return true;
            case R.id.delete:
                Log.d(LOGTAG, "Menu info: " + info.position);
                Log.d(LOGTAG, "Menu info2: " + selectedAction);
                actionsDatabase.openWritableDB();
                actionsDatabase.deleteRecordedAction(selectedAction);
                actionsListAdapter.updateContent(actionsDatabase.getRecordedActions());
                actionsDatabase.closeDB();
                actionsListView.invalidateViews();
                actionsListView.setAdapter(actionsListAdapter);
                registerForContextMenu(actionsListView);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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
        Intent sensorHandlerService = new Intent(this, DataCollectionService.class);
        Intent classifyService = new Intent(this, ClassifyService.class);
        Intent alarmService = new Intent(this, AlarmService.class);
        startService(sensorHandlerService);
        startService(classifyService);
        startService(alarmService);
        bindService(classifyService, classifyServiceConnection, Context.BIND_AUTO_CREATE);
        bindService(sensorHandlerService, sensorHandlerServiceConnection, Context.BIND_AUTO_CREATE);
        bindService(alarmService, alarmServiceConnection, Context.BIND_AUTO_CREATE);
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
        if (mAlarmBound) {
            unbindService(alarmServiceConnection);
            mAlarmBound = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent setting = new Intent(this, SettingsActivity.class);
            startActivity(setting);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfirmationDialogPositiveClick(DialogFragment dialog) {
        Log.d(LOGTAG, "Start clicked");
        progress = ProgressDialog.show(this, "Please wait", "Recording the action...", true, false);
        sendMessageToService(sensorHandlerServiceMessageSender, HelperFunctions.MSG_START_RECORDING);
        sendMessageToService(classifyServiceMessageSender, HelperFunctions.MSG_START_RECORDING);
    }

    @Override
    public void onConfirmationDialogNegativeClick(DialogFragment dialog) {
        Log.d(LOGTAG, "Cancel clicked");
    }

    @Override
    public void onClassificationDialogPositiveClick(DialogFragment dialog, String addedLabel) {
        Log.d(LOGTAG, "Save clicked");
        actionsDatabase.openWritableDB();
        actionsDatabase.updateLabel(recentRecordedActionID, addedLabel);
        actionsListAdapter.updateContent(actionsDatabase.getRecordedActions());
        actionsDatabase.closeDB();
        actionsListView.invalidateViews();
        actionsListView.setAdapter(actionsListAdapter);
        registerForContextMenu(actionsListView);
    }

    @Override
    public void onClassificationDialogNeutralClick(DialogFragment dialog) {
        Log.d(LOGTAG, "Neutral clicked");
        progress = ProgressDialog.show(this, "Please wait", "Classifying the action...", true, false);
        sendMessageToService(classifyServiceMessageSender, HelperFunctions.MSG_START_CLASSIFICATION, recentRecordedActionID);
    }

    @Override
    public void onClassificationDialogNegativeClick(DialogFragment dialog) {
        Log.d(LOGTAG, "Cancel clicked");
        actionsDatabase.openWritableDB();
        actionsDatabase.deleteRecordedAction(recentRecordedActionID);
        actionsDatabase.closeDB();
    }

    @Override
    public void onTimeSet(DialogFragment dialog, TimePicker view, int hourOfDay, int minute, String actionToAlarm) {
        AlarmObject newAlarm = new AlarmObject(hourOfDay, minute, 0, actionToAlarm);
        alarmFrequencyPicker = new AlarmFrequencyDialogFragment(getApplicationContext(), newAlarm);
        alarmFrequencyPicker.show(getFragmentManager(), "AlarmFrequency");
    }

    @Override
    public void onFrequencyItemClick(DialogInterface dialog, final AlarmObject newAlarm) {
        sendMessageToService(alarmServiceMessageSender, HelperFunctions.MSG_REGISTER_NEW_ALARM, newAlarm);
    }

    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HelperFunctions.MSG_RECORDING_DONE:
                    Log.d(LOGTAG, "Recording done");
                    progress.dismiss();
                    progress = ProgressDialog.show(MainActivity.this, "Please wait", "Receiving wearable device data...", true, false);
                    break;
                case HelperFunctions.MSG_RECORDING_DONE_HIDDEN:
                    Log.d(LOGTAG, "Hidden recording done");
                    break;
                case HelperFunctions.MSG_COMBINING_DONE:
                    Log.d(LOGTAG, "Receiving and combining of wearable data is done");
                    recentRecordedActionID = (int) (long) msg.obj;
                    progress.dismiss();
                    classification = new ClassificationDialogFragment(getApplicationContext());
                    classification.show(getFragmentManager(), "Classification");
                    break;
                case HelperFunctions.MSG_COMBINING_DONE_HIDDEN:
                    recentRecordedActionID = (int) (long) msg.obj;
                    Log.d(LOGTAG, "Hidden Receiving and combining of wearable data is done: " + recentRecordedActionID);
                    sendMessageToService(classifyServiceMessageSender, HelperFunctions.MSG_START_CLASSIFICATION, recentRecordedActionID);
                    break;
                case HelperFunctions.MSG_CLASSIFIER_RESULT:
                    String classificationResult = (String) msg.obj;
                    Log.d(LOGTAG, "Classifier result: " + classificationResult);
                    actionsDatabase.openReadableDB();
                    String actionName = actionsDatabase.getLabel(Integer.parseInt(classificationResult) + 1);
                    Log.d(LOGTAG, "Action name: " + actionName);
                    actionsDatabase.closeDB();
                    classification = new ClassificationDialogFragment(getApplicationContext(), actionName);
                    progress.dismiss();
                    classification.show(getFragmentManager(), "Classification");
                    break;
                case HelperFunctions.MSG_CLASSIFIER_RESULT_HIDDEN:
                    String classificationResultHidden = (String) msg.obj;
                    actionsDatabase.openWritableDB();
                    String actionNameHidden = actionsDatabase.getLabel(Integer.parseInt(classificationResultHidden) + 1);
                    Log.d(LOGTAG, "Hidden action name: " + actionNameHidden);
                    sendMessageToService(alarmServiceMessageSender, HelperFunctions.MSG_CLASSIFIER_RESULT_HIDDEN, actionNameHidden);
                    actionsDatabase.deleteRecordedAction(recentRecordedActionID);
                    actionsDatabase.closeDB();
                    break;
                case HelperFunctions.MSG_START_RECORDING_HIDDEN:
                    Log.d(LOGTAG, "Hidden recording start");
                    sendMessageToService(sensorHandlerServiceMessageSender, HelperFunctions.MSG_START_RECORDING_HIDDEN);
                    sendMessageToService(classifyServiceMessageSender, HelperFunctions.MSG_START_RECORDING_HIDDEN);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMessageToService(final Messenger toService, final int message) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    toService.send(Message.obtain(null, message));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendMessageToService(final Messenger toService, final int message, final Object a) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    toService.send(Message.obtain(null, message, a));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
