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
    private static final String KEY_1 = "a";
    private static final String KEY_2 = "b";
    private static final String KEY_3 = "c";
    private static final String KEY_4 = "d";
    private static final String KEY_5 = "e";
    private static final String KEY_6 = "f";
    private static final String KEY_7 = "g";
    private static final String KEY_8 = "h";
    private static final String KEY_9 = "i";
    private static final String KEY_10 = "j";
    private static final String KEY_11 = "k";
    private static final String KEY_12 = "l";
    private static final String KEY_LID = "lid";

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

    void addFeatures(FeatureContainer data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, data.getTime());
        values.put(KEY_1, data.getA());
        values.put(KEY_2, data.getB());
        values.put(KEY_3, data.getC());
        values.put(KEY_4, data.getD());
        values.put(KEY_5, data.getE());
        values.put(KEY_6, data.getF());
        values.put(KEY_7, 0);
        values.put(KEY_8, 0);
        values.put(KEY_9, 0);
        values.put(KEY_10, 0);
        values.put(KEY_11, 0);
        values.put(KEY_12, 0);
        values.put(KEY_LID, data.getLid());

        // Inserting Row
        db.insert(TABLE_ACTIONS, null, values);
        db.close(); // Closing database connection
    }

    long addNewLabel(String newLabel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, newLabel);

        // Inserting Row
        long recent = db.insert(TABLE_LABELS, null, values);
        db.close(); // Closing database connection
        return recent;
    }

    double[][][] getDataSet(){

        String selectQuery = "SELECT "+ KEY_1 + ", " + KEY_2 + ", " + KEY_3 + ", " + KEY_4 + ", " + KEY_5 + ", " + KEY_6 + ", " + KEY_LID + " FROM " + TABLE_ACTIONS;
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

    int[] getLabels(){
        int[] labels = {0,0,0,0,1,1,1,1,2,2,2,2};
        return labels;
    }

    public static String getForeignKey() {
        return TABLE_LABELS + "("+ KEY_ID +")";
    }
}
