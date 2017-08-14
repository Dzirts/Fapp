package com.davemorrissey.labs.subscaleview.sample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "dataInfo";
    // Contacts table name
    private static final String TABLE_APP_DATA = "appData";
    // Shops Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_XLS_FILE_NAME = "xls_name";
    private static final String KEY_XLS_FILE_PATH = "xls_path";
    private static final String KEY_XLS_FILE_DIR  = "xls_dir";
    private static final String KEY_PROJECT_NAME  = "project_name";
    private static final String KEY_SERIES_NUMBER = "series_number";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " +
                TABLE_APP_DATA + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_XLS_FILE_NAME + " TEXT,"
                + KEY_XLS_FILE_PATH + " TEXT,"
                + KEY_XLS_FILE_DIR  + " TEXT,"
                + KEY_PROJECT_NAME  + " TEXT,"
                + KEY_SERIES_NUMBER + " TEXT,"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_DATA);
        // Creating tables again
        onCreate(db);
    }
    // Adding new internalData
    public void addInternalData(InternalData internalData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_XLS_FILE_NAME, internalData.getXlsFileName());
        values.put(KEY_XLS_FILE_PATH, internalData.getXlsFilePath());
        values.put(KEY_XLS_FILE_DIR , internalData.getXlsFileDirectory());
        values.put(KEY_PROJECT_NAME , internalData.getProjectName());
        values.put(KEY_SERIES_NUMBER, internalData.getCurrentSeries());

// Inserting Row
        db.insert(TABLE_APP_DATA, null, values);
        db.close(); // Closing database connection
    }
    // Getting one shop
    public InternalData getInternalData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_APP_DATA, new String[]{
                        KEY_ID,KEY_PROJECT_NAME,
                        KEY_SERIES_NUMBER, KEY_XLS_FILE_NAME,
                        KEY_XLS_FILE_PATH, KEY_XLS_FILE_DIR},
                        KEY_ID + "=?", new String[]{String.valueOf(id)},
                        null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        InternalData contact = new InternalData(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1),cursor.getString(2),
                cursor.getString(3),cursor.getString(4),
                cursor.getString(5));
        return contact;
    }
//    // Getting All Shops
//    public List<InternalData> getAllData() {
//        List<InternalData> internalDataList = new ArrayList<InternalData>();
//        // Select All Query
//        String selectQuery = "SELECT * FROM " + TABLE_APP_DATA;
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                InternalData internalData = new InternalData();
//                internalData.setProjectname(cursor.getString(0));
//                internalData.setSeries(cursor.getString(1));
//                internalData.setXlsFileName(cursor.getString(2));
//                internalData.setXlsFilePath(cursor.getString(3));
//                internalData.setXlsFileDirectory(cursor.getString(4));
//                // Adding contact to list
//                internalDataList.add(internalData);
//            } while (cursor.moveToNext());
//        }
//
//        // return contact list
//        return internalDataList;
//    }
//    // Getting shops Count
//    public int getShopsCount() {
//        String countQuery = "SELECT * FROM " + TABLE_APP_DATA;
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(countQuery, null);
//        cursor.close();
//
//// return count
//        return cursor.getCount();
//    }
    // Updating a internalData
    public int updateInternalData(InternalData internalData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_XLS_FILE_NAME, internalData.getXlsFileName());
        values.put(KEY_XLS_FILE_PATH, internalData.getXlsFilePath());
        values.put(KEY_XLS_FILE_DIR , internalData.getXlsFileDirectory());
        values.put(KEY_PROJECT_NAME , internalData.getProjectName());
        values.put(KEY_SERIES_NUMBER, internalData.getCurrentSeries());

// updating row
        return db.update(TABLE_APP_DATA, values, KEY_ID + " = ?",
                new String[]{String.valueOf(internalData.getId())});
    }

    // Deleting a internalData
    public void deleteInternalData(InternalData internalData) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APP_DATA, KEY_ID + " = ?",
                new String[] { String.valueOf(internalData.getId()) });
        db.close();
    }
}