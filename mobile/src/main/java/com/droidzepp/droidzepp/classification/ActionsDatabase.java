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

    private SQLiteDatabase actionsDB;
    private ContentValues values = new ContentValues();

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

    public void openWritableDB(){
        actionsDB = this.getWritableDatabase();
    }

    public void openReadableDB(){
        actionsDB = this.getReadableDatabase();
    }

    public void closeDB(){
        actionsDB.close();
    }

    public long addFeatures(FeatureContainer data) {
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
        long rowNumber = actionsDB.insert(TABLE_ACTIONS, null, values);
        values.clear();
        return rowNumber;
    }

    public long addNewLabel(String newStringLabel) {
        String queryMaxLabel = "SELECT MAX(" + KEY_LABEL + ") FROM " + TABLE_LABELS;
        long recent;
        int newIntLabel;

        Cursor cursorMax = actionsDB.rawQuery(queryMaxLabel, null);
        cursorMax.moveToFirst();
            newIntLabel = cursorMax.getInt(0) + 1;

        values.put(KEY_NAME, newStringLabel);
        values.put(KEY_LABEL, newIntLabel);
        recent = actionsDB.insert(TABLE_LABELS, null, values);
        values.clear();

        return recent;
    }

    public long updateLabel(long rowId, String newStringLabel) {
        String queryMaxLabel = "SELECT MAX(" + KEY_LABEL + ") FROM " + TABLE_LABELS;
        String querySameLabel = "SELECT " + KEY_LABEL + " FROM " + TABLE_LABELS + " WHERE " + KEY_NAME + " = '" + newStringLabel + "'";
        int newIntLabel;

        Cursor cursorMax = actionsDB.rawQuery(queryMaxLabel, null);
        Cursor cursorSame = actionsDB.rawQuery(querySameLabel, null);
        if (cursorSame.getCount() == 0) {
            cursorMax.moveToFirst();
            newIntLabel = cursorMax.getInt(0);
        } else {
            cursorSame.moveToFirst();
            newIntLabel = cursorSame.getInt(0);
        }

        values.put(KEY_NAME, newStringLabel);
        values.put(KEY_LABEL, newIntLabel);
        int affectedRows = actionsDB.update(TABLE_LABELS, values, KEY_ID + "=" + rowId, null);
        values.clear();
        return affectedRows;
    }

    public double[][][] getDataSet(int forThisTestData) {
        String selectQuery = "SELECT * FROM " + TABLE_ACTIONS + " EXCEPT SELECT * FROM " + TABLE_ACTIONS + " WHERE " + KEY_LID + " = " + forThisTestData;
        Cursor cursor = actionsDB.rawQuery(selectQuery, null);

        int numFeatures = getMinCountOfDistinctLabels();
        int numLabels = getLabels(forThisTestData).length;
        double[][][] dataSet = new double[numLabels][numFeatures][12];

        int counter = 0;
        int previous = 0;
        if (cursor.moveToFirst()) {
            do {
                int lId = cursor.getInt(cursor.getColumnIndex(KEY_LID)) - 1;
                if (lId != previous)
                    counter = 0;
                if (counter < numFeatures) {
                    dataSet[lId][counter][0] = cursor.getDouble(cursor.getColumnIndex(KEY_1));
                    dataSet[lId][counter][1] = cursor.getDouble(cursor.getColumnIndex(KEY_2));
                    dataSet[lId][counter][2] = cursor.getDouble(cursor.getColumnIndex(KEY_3));
                    dataSet[lId][counter][3] = cursor.getDouble(cursor.getColumnIndex(KEY_4));
                    dataSet[lId][counter][4] = cursor.getDouble(cursor.getColumnIndex(KEY_5));
                    dataSet[lId][counter][5] = cursor.getDouble(cursor.getColumnIndex(KEY_6));
                    dataSet[lId][counter][6] = cursor.getDouble(cursor.getColumnIndex(KEY_7));
                    dataSet[lId][counter][7] = cursor.getDouble(cursor.getColumnIndex(KEY_8));
                    dataSet[lId][counter][8] = cursor.getDouble(cursor.getColumnIndex(KEY_9));
                    dataSet[lId][counter][9] = cursor.getDouble(cursor.getColumnIndex(KEY_10));
                    dataSet[lId][counter][10] = cursor.getDouble(cursor.getColumnIndex(KEY_11));
                    dataSet[lId][counter][11] = cursor.getDouble(cursor.getColumnIndex(KEY_12));
                    previous = lId;
                }
                counter++;
            } while (cursor.moveToNext());
        }

        return dataSet;
    }

    public double [][] getTestData(int forThisTestData){
        String selectQuery = "SELECT * FROM " + TABLE_ACTIONS + " WHERE " + KEY_LID + " = " + forThisTestData;
        Cursor cursor = actionsDB.rawQuery(selectQuery, null);

        int numFeatures = cursor.getCount();

        double[][] testData = new double[numFeatures][12];
        int counter = 0;

        if(cursor.moveToFirst()){
            do {
                testData[counter][0] = cursor.getDouble(cursor.getColumnIndex(KEY_1));
                testData[counter][1] = cursor.getDouble(cursor.getColumnIndex(KEY_2));
                testData[counter][2] = cursor.getDouble(cursor.getColumnIndex(KEY_3));
                testData[counter][3] = cursor.getDouble(cursor.getColumnIndex(KEY_4));
                testData[counter][4] = cursor.getDouble(cursor.getColumnIndex(KEY_5));
                testData[counter][5] = cursor.getDouble(cursor.getColumnIndex(KEY_6));
                testData[counter][6] = cursor.getDouble(cursor.getColumnIndex(KEY_7));
                testData[counter][7] = cursor.getDouble(cursor.getColumnIndex(KEY_8));
                testData[counter][8] = cursor.getDouble(cursor.getColumnIndex(KEY_9));
                testData[counter][9] = cursor.getDouble(cursor.getColumnIndex(KEY_10));
                testData[counter][10] = cursor.getDouble(cursor.getColumnIndex(KEY_11));
                testData[counter][11] = cursor.getDouble(cursor.getColumnIndex(KEY_12));
                counter++;
            }while (cursor.moveToNext());
        }
        return testData;
    }

    public int[] getLabels(int forThisTestData) {
        String selectQuery = "SELECT * FROM " + TABLE_LABELS + " EXCEPT SELECT * FROM " + TABLE_LABELS + " WHERE " + KEY_ID + " = " + forThisTestData;
        Cursor cursor = actionsDB.rawQuery(selectQuery, null);
        int[] labels = new int[cursor.getCount()];
        int count = 0;
        if(cursor.moveToFirst()){
            do{
                labels[count] = cursor.getInt(cursor.getColumnIndex(KEY_LABEL))-1;
                count++;
            }while (cursor.moveToNext());
        }
        return labels;
    }

    public String getLabel(int lId){
        String selectQuery = "SELECT * FROM " + TABLE_LABELS + " WHERE " + KEY_LABEL + " = " + lId;
        Cursor cursor = actionsDB.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(KEY_NAME));
    }

    public String[] getClasses(int forThisTestData) {
        String selectQuery = "SELECT DISTINCT(" + KEY_LABEL + ") FROM " + TABLE_LABELS + " EXCEPT SELECT DISTINCT(" + KEY_LABEL + ") FROM " + TABLE_LABELS + " WHERE " + KEY_ID + " = " + forThisTestData;
        Cursor cursor = actionsDB.rawQuery(selectQuery, null);
        String[] classes = new String[cursor.getCount()];
        int count = 0;
        if(cursor.moveToFirst()){
            do{
                classes[count] = String.valueOf(cursor.getInt(0)-1);
                count++;
            }while (cursor.moveToNext());
        }
        return classes;
    }

    public int getMinCountOfDistinctLabels() {
        String selectQuery = "SELECT MIN(myCount) FROM (SELECT COUNT(" + KEY_LID + ") myCount FROM " + TABLE_ACTIONS + " GROUP BY " + KEY_LID + ")";

        Cursor cursor = actionsDB.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public void deleteRecordedAction(int lId) {
        actionsDB.delete(TABLE_ACTIONS, KEY_LID + " = " + lId, null);
        actionsDB.delete(TABLE_LABELS, KEY_ID + " = " + lId, null);
    }

    public void deleteRecordedAction(String label) {
        String selectQuery = "SELECT * FROM " + TABLE_LABELS + " WHERE " + KEY_NAME + " = '" + label + "'";
        Cursor cursor = actionsDB.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                actionsDB.delete(TABLE_ACTIONS, KEY_LID + " = " + cursor.getString(0), null);
            }while (cursor.moveToNext());
        }

        actionsDB.delete(TABLE_LABELS, KEY_NAME + " = '" + label + "'", null);
    }

    public ArrayList<RecordedActionListElement> getRecordedActions() {
        String selectQuery = "SELECT * FROM " + TABLE_LABELS + " GROUP BY " + KEY_NAME;
        Cursor cursor = actionsDB.rawQuery(selectQuery, null);

        ArrayList<RecordedActionListElement> recordedActions = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                recordedActions.add(new RecordedActionListElement(cursor.getString(cursor.getColumnIndex(KEY_NAME)), cursor.getLong(cursor.getColumnIndex(KEY_ID))));
            } while (cursor.moveToNext());
        }
        return recordedActions;
    }
}
