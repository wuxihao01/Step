package com.example.um.step;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataDAO {
    private StepData data;
    private SQLiteDatabase db;
    private Boolean isinit = false;
    private int step = -1;
    private String whereclause;

    public DataDAO() {
    }

    public void init(Context context) {
        data = new StepData(context);
        db = data.getWritableDatabase();
        isinit = true;
    }

    public int savaData(String time, String step) {
        int flag = 0;                     //判断当前是否为新的一天
        if(getData(time)==-1){
            ContentValues values=new ContentValues();
            values.put("time",time);
            values.put("stepcount",step);
            db.insert("STEPDATA",null,values);
            flag=1;
        }
        else{
            upData(time,String.valueOf(step));
        }
        return flag;
    }

    public void upData(String time, String step) {
        ContentValues values = new ContentValues();
        values.put("stepcount", step);
        whereclause = "time==?";
        Log.d("ummmm","当前步数为:"+step);
        db.update("STEPDATA", values, whereclause, new String[]{time});
    }

    public void delData(String time) {
        db.delete("STEPDATA", "time=?", new String[]{time});
    }

    public int getData(String time) {
        whereclause = "time == ?";
        Cursor cursor = db.query("STEPDATA", null, whereclause, new String[]{time}, null, null, "time");
        if (cursor.getCount() == 0) {
        } else {
            if (cursor.moveToFirst()) {
                do {
                    step = Integer.valueOf(cursor.getString(cursor.getColumnIndex("stepcount")));
                } while (cursor.moveToNext());
            }
        }
        return step;
    }



}

