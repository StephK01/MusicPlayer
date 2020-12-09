package com.example.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DBNAME="login.db";
    public DBHelper(Context context){
        super(context,"login.db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDB) {
        MyDB.execSQL("create Table users(username TEXT primary key, password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int oldVersion, int newVersion) {
        MyDB.execSQL("Drop table if exists users");
    }

    public boolean insertData(String username,String password){
        SQLiteDatabase MyDB=this.getWritableDatabase();
        ContentValues  contentvalues=new ContentValues();
        contentvalues.put("username",username);
        contentvalues.put("password",password);
        long result=MyDB.insert("users",null,contentvalues);
        if(result==-1)  //-1 indicates failure in insertion of data.
            return false;
        else
            return true;
    }

    public boolean checkUsername(String username){
        SQLiteDatabase MyDB=this.getWritableDatabase();
        Cursor cursor=MyDB.rawQuery("Select * from users where username=?",new String[] {username});
        if(cursor.getCount()>0){
            return true;
        }
        else {
            return false;
        }
    }

    public boolean checkusernamepassword(String username,String password){
        SQLiteDatabase MyDB=this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("Select * from users where username = ? and password = ?", new String[] {username,password});
        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }
}

