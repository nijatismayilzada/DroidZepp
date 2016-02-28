package com.droidzepp.droidzepp.datacollection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class GyroscopeNewDataHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "gyroscopeNewRecord.db";
    private static final String TABLE_GYROSCOPE = "xyzRecords";
    private static final String KEY_ID = "id";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_Z = "z";
    private SQLiteDatabase gyroTempDB;
    private ContentValues values = new ContentValues();

    public GyroscopeNewDataHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_GYROSCOPE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_X + " REAL,"
                + KEY_Y + " REAL," + KEY_Z + " REAL" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GYROSCOPE);
        onCreate(db);
    }

    public void openWritableDB(){
        gyroTempDB = this.getWritableDatabase();
    }

    public void openReadableDB(){
        gyroTempDB = this.getReadableDatabase();
    }

    public void closeDB(){
        gyroTempDB.close();
    }

    public long addXYZ(XYZ data) {
        values.put(KEY_X, data.getX());
        values.put(KEY_Y, data.getY());
        values.put(KEY_Z, data.getZ());
        // Inserting Row
        long insertedRow = gyroTempDB.insert(TABLE_GYROSCOPE, null, values);
        return insertedRow;
    }

    public List<XYZ> getAllData() {
        List<XYZ> dataList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_GYROSCOPE;
        Cursor cursor = gyroTempDB.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                XYZ entry = new XYZ();
                entry.setX(cursor.getFloat(1));
                entry.setY(cursor.getFloat(2));
                entry.setZ(cursor.getFloat(3));
                dataList.add(entry);
            } while (cursor.moveToNext());
        }
        return dataList;
    }

    public void clearTable(){
        gyroTempDB.execSQL("DELETE FROM " + TABLE_GYROSCOPE + ";");
    }
}
