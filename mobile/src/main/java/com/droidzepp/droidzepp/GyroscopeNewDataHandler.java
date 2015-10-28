package com.droidzepp.droidzepp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GyroscopeNewDataHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "gyroscopeNewRecord.db";
    private static final String TABLE_GYROSCOPE = "xyzRecords";
    private static final String KEY_ID = "id";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_Z = "z";

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

    void addXYZ(XYZ data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_X, data.getX());
        values.put(KEY_Y, data.getY());
        values.put(KEY_Z, data.getZ());

        // Inserting Row
        db.insert(TABLE_GYROSCOPE, null, values);
        db.close(); // Closing database connection
    }

    void clearTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM xyzRecords;");
        db.close();
    }
}
