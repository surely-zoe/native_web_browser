package com.example.nativebrowser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "HBDb.db";
    Date currentTime = Calendar.getInstance().getTime();

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table BrowserHistory " +
                        "(time text , url text)"
        );
        db.execSQL(
                "create table MyBookmarks " +
                        "(url text  primary key)"
        );
        db.execSQL(
                "create table DownloadHistory " +
                        "(addr text  primary key)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS BrowserHistory");
        db.execSQL("DROP TABLE IF EXISTS MyBookmarks");
        db.execSQL("DROP TABLE IF EXISTS DownloadHistory");
        onCreate(db);
    }

    public boolean addHistory(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("time", String.valueOf(currentTime));
        contentValues.put("url", url);
        long result;
        result = db.insert("BrowserHistory", null, contentValues);
        if (result == -1) {
            Log.d("Test", "not inserted");
            return false;
        } else {
            Log.d("Test", "History inserted");
            return true;
        }
    }

    public boolean addBookmark(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("url", url);
        long result = db.insert("MyBookmarks", null, contentValues);
        if (result == -1) {
            Log.d("Test", "not bookmarked");
            return false;
        } else {
            Log.d("Test", "bookmark inserted");
            return true;
        }
    }

    public boolean addDownloads(String addr) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("addr", addr);
        long result = db.insert("DownloadHistory", null, contentValues);
        if (result == -1) {
            Log.d("Test", "download not inserted");
            return false;
        } else {
            Log.d("Test", "download inserted");
            return true;
        }
    }

    public ArrayList<String> getHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> history = new ArrayList<String>();
        Cursor res = db.rawQuery("select * from BrowserHistory", null);
        res.moveToLast();
        while (res.isBeforeFirst() == false) {
            history.add(res.getString(1));
            res.moveToPrevious();
        }
        return history;
    }

    public ArrayList<String> getBookmark() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> bookmark = new ArrayList<String>();
        Cursor cursor = db.rawQuery("select * from MyBookmarks", null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            bookmark.add(cursor.getString(cursor.getColumnIndex("url")));
            cursor.moveToNext();
        }
        return bookmark;
    }
    public ArrayList<String> getDownloads() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> downloads = new ArrayList<String>();
        Cursor cursor = db.rawQuery("select * from DownloadHistory", null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            downloads.add(cursor.getString(cursor.getColumnIndex("addr")));
            cursor.moveToNext();
        }
        return downloads;
    }

    public ArrayList<String> getDownloadsForList() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> downloads = new ArrayList<String>();
        Cursor cursor = db.rawQuery("select * from DownloadHistory", null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            String test = cursor.getString(cursor.getColumnIndex("addr"));
            int pos = test.lastIndexOf('/');
            String sub = test.substring(pos+1);
            downloads.add(sub);
            cursor.moveToNext();
        }
        return downloads;
    }

    public void deleteHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("BrowserHistory", null, null);
        db.close();
    }

    public void deleteBookmarks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("MyBookmarks", null, null);
        db.close();
    }

    public void deleteDownloads() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("DownloadHistory", null, null);
        db.close();
    }

    public int numberOfRowsInHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, "BrowserHistory");
        return numRows;
    }

    public int numberOfRowsInBookmarks() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, "MyBookmarks");
        return numRows;
    }

    public int numberOfRowsInDownloads() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, "DownloadHistory");
        return numRows;
    }

    public void removeBookmark(String url){
        SQLiteDatabase db= this.getWritableDatabase();
        db.delete("MyBookmarks","url" + " = ?",new String[] { String.valueOf(url) } );
        db.close();
    }


    public void removeHistory(String url){
        SQLiteDatabase db= this.getWritableDatabase();
        db.delete("BrowserHistory","url" + " = ?",new String[] { String.valueOf(url) } );
        db.close();
    }

    public void removeDownload(String addr){
        SQLiteDatabase db= this.getWritableDatabase();
        db.delete("DownloadHistory","addr" + " = ?",new String[] { String.valueOf(addr) } );
        db.close();
    }

    public boolean inBookmarks(String url){
        SQLiteDatabase db= this.getReadableDatabase();
        String Query = "Select * from MyBookmarks where url = '" + url + "'";
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }


}
