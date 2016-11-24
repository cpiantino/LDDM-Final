package com.caio563272.lddmtp2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    final static String TABLE_NAME = "placas";
    final static String _ID = "_id";

    final static String PHOTO = "photo";
    final static String LATITUDE = "latitude";
    final static String LONGITUDE = "longitude";
    final static String DATE = "date";

    private static final String TEXT_TYPE = " TEXT";
    private static final String NUMERIC_TYPE = " NUMERIC";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    final private static String CREATE_CMD =

        "CREATE TABLE " + TABLE_NAME + " (" +
        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        PHOTO + TEXT_TYPE + COMMA_SEP +
        LATITUDE + REAL_TYPE + COMMA_SEP +
        LONGITUDE + REAL_TYPE + COMMA_SEP +
        DATE + NUMERIC_TYPE + " )";

    final private static String NAME = "placas_db";
    final private static Integer VERSION = 1;
    final private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, NAME, null, VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // N/A
    }

    void deleteDatabase() {
        mContext.deleteDatabase(NAME);
    }
}
