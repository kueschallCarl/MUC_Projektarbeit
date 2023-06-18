package com.example.menu_template;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SettingsDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "settings_database";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "settings";
    private static final String COLUMN_ID = "id";
    public static final String COLUMN_STEERING_METHOD = "steering_method";
    public static final String COLUMN_BROKER_IP = "broker_ip";

    private static SettingsDatabase instance = null;
    private Context mContext;

    public static final String COLUMN_AUDIO_ENABLED = "audio_enabled";


    private SettingsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public void updateAudioEnabled(boolean audioEnabled) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_AUDIO_ENABLED, audioEnabled ? 1 : 0);

        db.update(TABLE_NAME, values, null, null);
    }


    public boolean isAudioEnabled() {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {COLUMN_AUDIO_ENABLED};
        Cursor cursor = db.query(TABLE_NAME, projection, null, null, null, null, null);

        boolean audioEnabled = false;

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(COLUMN_AUDIO_ENABLED);
            int audioEnabledValue = cursor.getInt(columnIndex);
            audioEnabled = (audioEnabledValue == 1);
        }

        cursor.close();

        return audioEnabled;
    }

    public boolean containsSetting(String columnName) {
        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + columnName + " IS NOT NULL";
        Cursor cursor = db.rawQuery(selectQuery, null);

        boolean containsSetting = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return containsSetting;
    }


    public static SettingsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsDatabase(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_STEERING_METHOD + " TEXT, " +
                COLUMN_BROKER_IP + " TEXT)";
        db.execSQL(createTableQuery);
    }




    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
    }

    public void saveSetting(String setting, String column) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(column, setting);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void updateLastSetting(String setting, String column) {
        SQLiteDatabase db = getWritableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int rowCount = cursor.getInt(0);
        cursor.close();

        if (rowCount > 0) {
            ContentValues values = new ContentValues();
            values.put(column, setting);
            String whereClause = COLUMN_ID + " = (SELECT MAX(" + COLUMN_ID + ") FROM " + TABLE_NAME + ")";
            db.update(TABLE_NAME, values, whereClause, null);
        } else {
            saveSetting(setting, column);
        }

        db.close();
    }


    public String getSetting(String columnName) {
        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);
        String setting = null;

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {
                setting = cursor.getString(columnIndex);
            } else {
                Log.d("ColumnIndex", "Column index not found");
            }
        }
        cursor.close();
        db.close();
        return setting;
    }


    public Context getContext() {
        return mContext;
    }
}

