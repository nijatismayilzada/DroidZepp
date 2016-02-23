package com.droidzepp.droidzepp.classification;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.droidzepp.droidzepp.uiclasses.RecordedActionListElement;

import java.util.ArrayList;

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
    private static final String KEY_LABEL = "label";

    private static final String LOGTAG = "ActionsDatabase";

    public ActionsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_LABELS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_LABEL + " INTEGER" + ")";
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
        values.clear();
        db.close(); // Closing database connection
        return rowNumber;
    }

    public long addNewLabel(String newStringLabel) {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryMaxLabel = "SELECT MAX(" + KEY_LABEL + ") FROM " + TABLE_LABELS;
        long recent;
        int newIntLabel;

        Cursor cursorMax = db.rawQuery(queryMaxLabel, null);
        if (cursorMax.getCount() == 0) {
            newIntLabel = 1;
        } else {
            cursorMax.moveToFirst();
            newIntLabel = cursorMax.getInt(0) + 1;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, newStringLabel);
        values.put(KEY_LABEL, newIntLabel);
        recent = db.insert(TABLE_LABELS, null, values);

        db.close();
        return recent;
    }

    public long updateLabel(long rowId, String newStringLabel) {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryMaxLabel = "SELECT MAX(" + KEY_LABEL + ") FROM " + TABLE_LABELS;
        String querySameLabel = "SELECT " + KEY_LABEL + " FROM " + TABLE_LABELS + " WHERE " + KEY_NAME + " = '" + newStringLabel + "'";
        int newIntLabel;

        Cursor cursorMax = db.rawQuery(queryMaxLabel, null);
        Cursor cursorSame = db.rawQuery(querySameLabel, null);
        if (cursorSame.getCount() == 0) {
            cursorMax.moveToFirst();
            newIntLabel = cursorMax.getInt(0);
        } else {
            cursorSame.moveToFirst();
            newIntLabel = cursorSame.getInt(0);
        }

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, newStringLabel);
        values.put(KEY_LABEL, newIntLabel);
        int affectedRows = db.update(TABLE_LABELS, values, KEY_ID + "=" + rowId, null);
        db.close();
        return affectedRows;
    }

    public double[][][] getDataSet() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ACTIONS;
        Cursor cursor1 = db.rawQuery(selectQuery, null);

        double[][][] dataSet = new double[12][145][6];

        int counter = 0;
        int previous = 0;
        if (cursor1.moveToFirst()) {
            do {
                int klid = cursor1.getInt(cursor1.getColumnIndex(KEY_LID)) - 1;
                if (klid > previous)
                    counter = 0;
                if (counter < 145 && klid < 12) {
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

    public int[] getLabels() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + KEY_LABEL + " FROM " + TABLE_LABELS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        int[] labels = new int[cursor.getCount()];
        int count = 0;
        if(cursor.moveToFirst()){
            do{
                labels[count] = cursor.getInt(cursor.getColumnIndex(KEY_LABEL));
                count++;
            }while (cursor.moveToNext());
        }
        db.close();
        return labels;
    }

    public int getMinCountOfDistinctLabels() {
        String selectQuery = "SELECT MIN(myCount) FROM (SELECT COUNT(" + KEY_LID + ") myCount FROM " + TABLE_ACTIONS + " GROUP BY " + KEY_LID + ")";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        db.close();
        return cursor.getInt(0);
    }

    public void deleteRecordedAction(int lId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACTIONS, KEY_LID + " = " + lId, null);
        db.delete(TABLE_LABELS, KEY_ID + " = " + lId, null);
        db.close();
    }

    public String getForeignKey() {
        return TABLE_LABELS + "(" + KEY_ID + ")";
    }

    public ArrayList<RecordedActionListElement> getRecordedActions() {
        String selectQuery = "SELECT * FROM " + TABLE_LABELS + " GROUP BY " + KEY_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        ArrayList<RecordedActionListElement> recordedActions = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                recordedActions.add(new RecordedActionListElement(cursor.getString(cursor.getColumnIndex(KEY_NAME)), cursor.getLong(cursor.getColumnIndex(KEY_ID))));
            } while (cursor.moveToNext());
        }
        db.close();
        return recordedActions;
    }
}
