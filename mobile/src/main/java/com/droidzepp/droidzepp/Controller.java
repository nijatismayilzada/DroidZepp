//package com.droidzepp.droidzepp;
//
//import android.annotation.SuppressLint;
//import android.app.Service;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Message;
//import android.os.Messenger;
//import android.os.RemoteException;
//import android.support.annotation.Nullable;
//
//import com.droidzepp.droidzepp.alarm.AlarmService;
//import com.droidzepp.droidzepp.classification.ClassifyService;
//import com.droidzepp.droidzepp.datacollection.SensorHandlerService;
//
//public class Controller extends Service {
//    // Messengers between back-end services and front-end
//    final Messenger messageReceiver = new Messenger(new IncomingHandler());
//
//    Messenger classifyServiceMessageSender;
//    boolean mClassifyBound;
//
//    Messenger sensorHandlerServiceMessageSender;
//    boolean mSensorHandlerBound;
//
//    Messenger alarmServiceMessageSender;
//    boolean mAlarmBound;
//    private ServiceConnection classifyServiceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            classifyServiceMessageSender = new Messenger(service);
//            mClassifyBound = true;
//            try {
//                Message msg = Message.obtain(null, HelperFunctions.MSG_REGISTER_CLIENT);
//                msg.replyTo = messageReceiver;
//                classifyServiceMessageSender.send(msg);
//            } catch (RemoteException e) {
//                // In this case the service has crashed before we could even do anything with it
//            }
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            classifyServiceMessageSender = null;
//            mClassifyBound = false;
//        }
//    };
//
//    private ServiceConnection sensorHandlerServiceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            sensorHandlerServiceMessageSender = new Messenger(service);
//            mSensorHandlerBound = true;
//            try {
//                Message msg = Message.obtain(null, HelperFunctions.MSG_REGISTER_CLIENT);
//                msg.replyTo = messageReceiver;
//                sensorHandlerServiceMessageSender.send(msg);
//            } catch (RemoteException e) {
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
//
//    private ServiceConnection alarmServiceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            alarmServiceMessageSender = new Messenger(service);
//            mAlarmBound = true;
//            try {
//                Message msg = Message.obtain(null, HelperFunctions.MSG_REGISTER_CLIENT);
//                msg.replyTo = messageReceiver;
//                alarmServiceMessageSender.send(msg);
//            } catch (RemoteException e) {
//                // In this case the service has crashed before we could even do anything with it
//            }
//
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            alarmServiceMessageSender = null;
//            mAlarmBound = false;
//        }
//    };
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return messageReceiver.getBinder();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Intent sensorHandlerService = new Intent(this, SensorHandlerService.class);
//        Intent classifyService = new Intent(this, ClassifyService.class);
//        Intent alarmService = new Intent(this, AlarmService.class);
//        startService(sensorHandlerService);
//        startService(classifyService);
//        startService(alarmService);
//        bindService(classifyService, classifyServiceConnection, Context.BIND_AUTO_CREATE);
//        bindService(sensorHandlerService, sensorHandlerServiceConnection, Context.BIND_AUTO_CREATE);
//        bindService(alarmService, alarmServiceConnection, Context.BIND_AUTO_CREATE);
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        if (mClassifyBound) {
//            unbindService(classifyServiceConnection);
//            mClassifyBound = false;
//        }
//        if (mSensorHandlerBound) {
//            unbindService(sensorHandlerServiceConnection);
//            mSensorHandlerBound = false;
//        }
//        if (mAlarmBound) {
//            unbindService(alarmServiceConnection);
//            mAlarmBound = false;
//        }
//
//        super.onDestroy();
//    }
//
//    @SuppressLint("HandlerLeak")
//    class IncomingHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case HelperFunctions.MSG_RECORDING_DONE:
//                    break;
//                default:
//                    super.handleMessage(msg);
//            }
//        }
//    }
//}
