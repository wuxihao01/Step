package com.example.um.step;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.lixs.charts.BarChart.DragInerfaces;
import com.lixs.charts.BarChart.LBarChartView;
import com.lixs.charts.RadarChartView;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    private TextView textView;
    private Intent intent;
    boolean mBound;
    private DataDAO dao;
    private StepService mService;
    private StepView stepView;
    private String[] nearday=new String[7];
    LBarChartView LBarChartView;

    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepService.LocalBinder binder = (StepService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dao=new DataDAO();
        dao.init(getApplicationContext());
        textView=(TextView)findViewById(R.id.tv);
        StepView stepView=(StepView)findViewById(R.id.step);
        stepView.setCurrentCount(20000,dao.getData(getPastDate(0)));
        initdata();
        initNewBarDatas();
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_friends) {
                    Toast.makeText(getApplicationContext(),"this is friends",Toast.LENGTH_SHORT).show();
                }
                else if(tabId==R.id.tab_recents){
                    Toast.makeText(getApplicationContext(),"this is recent",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    private void initdata() {
        for(int i=0;i<7;i++){
            String data=getPastDate(i);
            nearday[i]=data;
            Log.d("ummmm",nearday[i]);
        }
    }

    private void initNewBarDatas() {
        final List<Double> datas = new ArrayList<>();
        final List<String> description = new ArrayList<>();
        LBarChartView = (LBarChartView) findViewById(R.id.frameNewBase);
        for(int c=0;c<7;c++){
            datas.add(Double.valueOf(dao.getData(nearday[c])));
            description.add(nearday[c]);
        }


        LBarChartView.setDatas(datas, description, true);
        LBarChartView.setDragInerfaces(new DragInerfaces() {
            @Override
            public void onEnd() {

            }

            @Override
            public void onStart() {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, StepService.class), connection,
                Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(connection);
            mBound = false;
        }
    }

    public void getstep(View view) {
        if (!mBound) return;
        textView.setText("你当前的步数："+String.valueOf(mService.getstepcount()));
        Toast.makeText(getApplicationContext(),mService.getallstepcount(),Toast.LENGTH_SHORT).show();
        stepView.setCurrentCount(15000,mService.getstepcount());
    }


    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today);
        return result;
    }
}
