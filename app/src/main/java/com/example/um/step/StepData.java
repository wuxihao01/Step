package com.example.um.step;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StepData extends SQLiteOpenHelper {
    private static final String DBName = "Step.db";
    private static final String STEP = "STEPDATA";
    private static final String CREAT_STEP_TABLE
            ="create table " + STEP + "(id integer primary key autoincrement, time text,stepcount text)";
    private static final int version=1;

    public StepData(Context context){
        super(context,DBName,null,version);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAT_STEP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
