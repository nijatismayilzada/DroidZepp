package com.droidzepp.droidzepp.datacollection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class AccelerometerNewDataHandler extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "accelerometerNewRecord.db";
    private static final String TABLE_ACCELEROMETER = "xyzRecords";
    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_Z = "z";

    public AccelerometerNewDataHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_ACCELEROMETER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " TEXT,"
                + KEY_X + " REAL," + KEY_Y + " REAL," + KEY_Z + " REAL" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCELEROMETER);

        onCreate(db);
    }

    public long addXYZ(XYZwithTime data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, data.getTime());
        values.put(KEY_X, data.getX());
        values.put(KEY_Y, data.getY());
        values.put(KEY_Z, data.getZ());

        // Inserting Row
        long insertedRow = db.insert(TABLE_ACCELEROMETER, null, values);
        db.close(); // Closing database connection
        return insertedRow;
    }

    public List<XYZwithTime> getAllData() {
        List<XYZwithTime> dataList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_ACCELEROMETER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                XYZwithTime entry = new XYZwithTime();
                entry.setTime(cursor.getString(1));
                entry.setX(cursor.getFloat(2));
                entry.setY(cursor.getFloat(3));
                entry.setZ(cursor.getFloat(4));
                dataList.add(entry);
            } while (cursor.moveToNext());
        }

        db.close();
        return dataList;
    }

    public void clearTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM xyzRecords;");
        db.close();
    }
}
