package com.droidzepp.droidzepp.alarm;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.droidzepp.droidzepp.HelperFunctions;

import java.util.ArrayList;

public class AlarmService extends Service {
    private static final String LOGTAG = "AlarmService";

    final Messenger messageReceiver = new Messenger(new IncomingHandler());
    ArrayList<Messenger> messageSender = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messageReceiver.getBinder();
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
                case HelperFunctions.MSG_REGISTER_NEW_ALARM:
                    Log.d(LOGTAG, "New alarm created");
                    AlarmObject newAlarm = (AlarmObject) msg.obj;
                    Log.d(LOGTAG, "Contents: " + newAlarm.getActionToAlarm() + ", " + newAlarm.getFrequency() + ", " + newAlarm.getHour() + ", " + newAlarm.getMinute());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMessageToMainActivity(int message) {
        try {
            messageSender.get(0).send(Message.obtain(null, message));
        } catch (RemoteException e) {
            messageSender.remove(0);
        }
    }

    private void sendMessageToMainActivity(int message, Object a) {
        try {
            messageSender.get(0).send(Message.obtain(null, message, a));
        } catch (RemoteException e) {
            messageSender.remove(0);
        }
    }
}
