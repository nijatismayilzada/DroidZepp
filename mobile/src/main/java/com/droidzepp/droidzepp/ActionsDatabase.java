package com.droidzepp.droidzepp;

import android.content.ContentValues;
import android.content.Context;
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
    private static final String KEY_1 = "a";
    private static final String KEY_2 = "b";
    private static final String KEY_3 = "c";
    private static final String KEY_4 = "d";
    private static final String KEY_5 = "e";
    private static final String KEY_6 = "f";

    public ActionsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_ACTIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_1 + " REAL,"
                + KEY_2 + " REAL," + KEY_3 + " REAL," + KEY_4 + " REAL,"
                + KEY_5 + " REAL," + KEY_6 + " REAL" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIONS);

        onCreate(db);
    }

    void addFeatures(FeatureContainer data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_1, data.getA());
        values.put(KEY_2, data.getB());
        values.put(KEY_3, data.getC());
        values.put(KEY_4, data.getD());
        values.put(KEY_5, data.getE());
        values.put(KEY_6, data.getF());

        // Inserting Row
        db.insert(TABLE_ACTIONS, null, values);
        db.close(); // Closing database connection
    }
}
