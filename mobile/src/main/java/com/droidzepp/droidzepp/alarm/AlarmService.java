package com.droidzepp.droidzepp.alarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.droidzepp.droidzepp.HelperFunctions;
import com.droidzepp.droidzepp.MainActivity;
import com.droidzepp.droidzepp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmService extends Service {
    private static final String LOGTAG = "AlarmService";

    final Messenger messageReceiver = new Messenger(new IncomingHandler());
    static ArrayList<Messenger> messageSender = new ArrayList<>();
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private static boolean alarmNeed = false;
    private static String actionToAlarm = "";
    static ExecutorService executorService;

    @Override
    public void onCreate() {
        alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        executorService = Executors.newCachedThreadPool();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.droidzepp.droidzepp.CLASSIFY");
        filter.addCategory("com.droidzepp.droidzepp");
        this.registerReceiver(new AlarmReceiver(), filter);

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
                    actionToAlarm = newAlarm.getActionToAlarm();
                    setAlarm(newAlarm);
                    break;
                case HelperFunctions.MSG_CLASSIFIER_RESULT_HIDDEN:
                    if (alarmNeed) {
                        String classificationResult = (String) msg.obj;
                        Log.d(LOGTAG, "Hidden classifier result: " + classificationResult);
                        if (!classificationResult.equals(actionToAlarm)) {
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(AlarmService.this)
                                    .setSmallIcon(R.drawable.droidzepp)
                                    .setContentTitle("DroidZepp")
                                    .setContentText("You missed " + actionToAlarm + "!");
                            Intent resultIntent = new Intent(AlarmService.this, MainActivity.class);

                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(AlarmService.this);
                            stackBuilder.addParentStack(MainActivity.class);
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent =
                                    stackBuilder.getPendingIntent(
                                            0,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );
                            mBuilder.setContentIntent(resultPendingIntent);
                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(0, mBuilder.build());
                        } else {
                            Log.d(LOGTAG, "User did the action. Alarm is not needed");
                        }
                        alarmNeed = false;
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void setAlarm(AlarmObject newAlarm) {

        Intent intent = new Intent(AlarmService.this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(AlarmService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d(LOGTAG, "setting alarm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, newAlarm.getHour());
        calendar.set(Calendar.MINUTE, newAlarm.getMinute());
        long interval;
        switch (newAlarm.getFrequency()) {
            case 0:
                interval = AlarmManager.INTERVAL_DAY;
                break;
            case 1:
                interval = 3 * AlarmManager.INTERVAL_DAY;
                break;
            case 2:
                interval = 5 * AlarmManager.INTERVAL_DAY;
                break;
            case 3:
                interval = 7 * AlarmManager.INTERVAL_DAY;
                break;
            default:
                interval = AlarmManager.INTERVAL_DAY;
        }

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, alarmIntent);
    }

    private static void sendMessageToMainActivity(final int message) {
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

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOGTAG, "Alarmmmmm");
            sendMessageToMainActivity(HelperFunctions.MSG_START_RECORDING_HIDDEN);
            alarmNeed = true;
        }
    }
}
