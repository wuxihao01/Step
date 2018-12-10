package com.example.um.step;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class StepService extends Service implements SensorEventListener{
    private String nowdate;
    private BroadcastReceiver mInfoReceiver;
    private static int stepSensor = -1;
    private SensorManager sensorManager;
    private NotificationManager notificationManager;
    private Notification.Builder builder;
    private boolean hasRecord;
    private final IBinder mBinder = new LocalBinder();
    private int hasStepCount;
    private int allstep;
    static final int RETURN_STEP = 0;
    private int previousStepCount;
    private Intent intent;
    private int savedatatime;
    private int stepcount=0;
    private Timer timer;
    private TimerTask task;
    private DataDAO dao;
    public StepService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        savedatatime=5000;
        nowdate=new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
        dao=new DataDAO();
        dao.init(this);
        savadata();
        if(dao.getData(nowdate)==-1){
            stepcount=0;
        }
        else {
            stepcount=dao.getData(nowdate);
        }
        initSensor();
        initNotification();
        initBroadcastReceiver();
        inittask();

    }


    public class LocalBinder extends Binder {
        StepService getService() {
            // Return this instance of LocalService so clients can call public methods
            return StepService.this;
        }
    }

    public void inittask(){
        timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                savadata();
            }
        };
        timer.scheduleAtFixedRate(timerTask,3000,savedatatime);
    }
    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }



    public void initBroadcastReceiver(){
        IntentFilter intentFilter=new IntentFilter();
        //屏幕灭屏
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        //关机
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        //解锁
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //监听日期变化
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        mInfoReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();
                // 屏幕灭屏广播
                switch (action){
                    case Intent.ACTION_SCREEN_OFF:{
                        savedatatime=20000;
                    }break;
                    case Intent.ACTION_USER_PRESENT:{
                        savedatatime=5000;
                    }break;
                    case Intent.ACTION_SHUTDOWN:
                    case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:{
                        savadata();
                    }break;
                    case Intent.ACTION_DATE_CHANGED:
                    case Intent.ACTION_TIME_CHANGED:
                    case Intent.ACTION_TIME_TICK:{
                        String zero="00:00";
                        if(zero.equals(new SimpleDateFormat("HH:mm").format(new Date())))
                        {
                            dao.savaData(nowdate,String.valueOf(stepcount));
                            stepcount=0;
                            nowdate=new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
                            savadata();
                        }
                        savadata();
                    }break;
                    default:break;
                }

            }
        };
    }

    public void initNotification(){
        intent=new Intent(this,MainActivity.class);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder=new Notification.Builder(getApplicationContext());
        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                .setContentText("计步器")
                .setContentTitle("运动精彩人生！")
                .setSmallIcon(R.drawable.run)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.drawable.run));
        Notification notification=builder.build();
        notificationManager.notify(123,notification);
    }


    public void initSensor(){
        sensorManager=(SensorManager)this.getSystemService(SENSOR_SERVICE);
        if(Build.VERSION.SDK_INT>=19){
            Sensor countersensor=sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            Sensor detectorsensor=sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            if(countersensor!=null){
                stepSensor=0;
                sensorManager.registerListener(StepService.this,countersensor,sensorManager.SENSOR_DELAY_NORMAL);
            }
            if(detectorsensor!=null){
                stepSensor=1;
                sensorManager.registerListener(StepService.this,detectorsensor,sensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }


    public void savadata(){
        nowdate=new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
        int flag=dao.savaData(nowdate,String.valueOf(stepcount));
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        allstep=(int)event.values[0];
        if(stepSensor==0){
            int tempStep = (int) event.values[0];
            if(!hasRecord){
                hasRecord=true;
                hasStepCount=tempStep;
                Toast.makeText(getApplicationContext(),"第一次使用",Toast.LENGTH_SHORT).show();
            }else{
                int thisStepCount = tempStep - hasStepCount;
                stepcount+=thisStepCount-previousStepCount;
                previousStepCount=thisStepCount;
                Toast.makeText(getApplicationContext(),"运动1",Toast.LENGTH_SHORT).show();
            }
        }else if(stepSensor==1){
            if(event.values[0]==1.0){
                stepcount++;
                Toast.makeText(getApplicationContext(),"运动2",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"出错啦！",Toast.LENGTH_SHORT).show();
        }
    }


    public int getstepcount(){
        return stepcount;
    }

    public String getallstepcount(){
        return String.valueOf(allstep);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        savadata();

    }
}
