package com.droidzepp.droidzepp.classification;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nijat on 28/10/15.
 */
public class ActionsDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "actionsDatabase.db";
    private static final String TABLE_ACTIONS = "features";
    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";
    private static final String KEY_1 = "accMX";
    private static final String KEY_2 = "accMY";
    private static final String KEY_3 = "accMZ";
    private static final String KEY_4 = "gyroMX";
    private static final String KEY_5 = "gyroMY";
    private static final String KEY_6 = "gyroMZ";
    private static final String KEY_7 = "accWX";
    private static final String KEY_8 = "accWY";
    private static final String KEY_9 = "accWZ";
    private static final String KEY_10 = "gyroWX";
    private static final String KEY_11 = "gyroWY";
    private static final String KEY_12 = "gyroWZ";
    private static final String KEY_LID = "lId";

    private static final String TABLE_LABELS = "labels";
    private static final String KEY_NAME = "actionName";

    public ActionsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_LABELS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);

        CREATE_TABLE = "CREATE TABLE " + TABLE_ACTIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TIME + " TEXT," + KEY_1 + " REAL,"
                + KEY_2 + " REAL," + KEY_3 + " REAL," + KEY_4 + " REAL,"
                + KEY_5 + " REAL," + KEY_6 + " REAL," + KEY_7 + " REAL,"
                + KEY_8 + " REAL," + KEY_9 + " REAL," + KEY_10 + " REAL,"
                + KEY_11 + " REAL," + KEY_12 + " REAL," + KEY_LID + " INTEGER,"
                + " FOREIGN KEY(" + KEY_LID + ") REFERENCES labels(id))";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LABELS);

        onCreate(db);
    }

    public long addFeatures(FeatureContainer data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, data.getTime());
        values.put(KEY_1, data.getAccMX());
        values.put(KEY_2, data.getAccMY());
        values.put(KEY_3, data.getAccMZ());
        values.put(KEY_4, data.getGyroMX());
        values.put(KEY_5, data.getGyroMY());
        values.put(KEY_6, data.getGyroMZ());
        values.put(KEY_7, data.getAccWX());
        values.put(KEY_8, data.getAccWY());
        values.put(KEY_9, data.getAccWZ());
        values.put(KEY_10, data.getGyroWX());
        values.put(KEY_11, data.getGyroWY());
        values.put(KEY_12, data.getGyroWZ());
        values.put(KEY_LID, data.getlId());

        // Inserting Row
        long rowNumber = db.insert(TABLE_ACTIONS, null, values);
        db.close(); // Closing database connection
        return rowNumber;
    }

    public long addNewLabel(String newLabel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, newLabel);

        // Inserting Row
        long recent = db.insert(TABLE_LABELS, null, values);
        db.close(); // Closing database connection
        return recent;
    }

    public double[][][] getDataSet(){

        String selectQuery = "SELECT * FROM " + TABLE_ACTIONS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor1 = db.rawQuery(selectQuery, null);

        double[][][] dataSet = new double[12][145][6];

        int counter = 0;
        int previous = 0;
        if (cursor1.moveToFirst()) {
            do {
                int klid = cursor1.getInt(cursor1.getColumnIndex(KEY_LID))-1;
                if(klid>previous)
                    counter = 0;
                if (counter<145 && klid<12) {
                    dataSet[klid][counter][0] = cursor1.getDouble(0);
                    dataSet[klid][counter][1] = cursor1.getDouble(1);
                    dataSet[klid][counter][2] = cursor1.getDouble(2);
                    dataSet[klid][counter][3] = cursor1.getDouble(3);
                    dataSet[klid][counter][4] = cursor1.getDouble(4);
                    dataSet[klid][counter][5] = cursor1.getDouble(5);
                    previous = klid;
                }
                counter++;
            } while (cursor1.moveToNext());
        }

        db.close();
        return dataSet;
    }

    public int[] getLabels(){
        int[] labels = {1,1,1,1,0,0,0,0,2,2,2,2};
        return labels;
    }

    public static String getForeignKey() {
        return TABLE_LABELS + "("+ KEY_ID +")";
    }
}
