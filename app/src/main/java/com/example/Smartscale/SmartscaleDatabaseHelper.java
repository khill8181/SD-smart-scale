package com.example.Smartscale;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Date;

public class SmartscaleDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smartScale";
    private static final int DB_VERSION = 1;
    SmartscaleDatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE FOODLOG ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "FOOD TEXT, "
                    + "DATE TEXT, "
                    + "WEIGHT REAL, "
                    + "CALORIES INTEGER);");

        db.execSQL("CREATE TABLE Foodlist ("
                +"_id integer primary key autoincrement, "
                +"food text, "
                +"weight real, "
                +"calories real, "
                +"countable integer);");
        insertNewFood(db, "popcorn", 1.2,5.6,0);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    }

    //daily entry
    public static void insertEntry(SQLiteDatabase db,
                                    String food,
                                    double mass,
                                    int calories) {
        long millis=System.currentTimeMillis();
        java.sql.Date currentDate=new java.sql.Date(millis);
        ContentValues entryValues = new ContentValues();
        entryValues.put("FOOD", food);
        entryValues.put("DATE", currentDate.toString());
        entryValues.put("WEIGHT", mass);
        entryValues.put("CALORIES", calories);
        db.insert("FOODLOG", null, entryValues);
    }

    //food table
    public static void insertNewFood(SQLiteDatabase db,
                                   String food,
                                   double mass,
                                   double calories,
                                   int countable) {
        ContentValues entryValues = new ContentValues();
        entryValues.put("food", food);
        entryValues.put("weight", mass);
        entryValues.put("calories", calories);
        entryValues.put("countable", countable);
        db.insert("Foodlist", null, entryValues);
    }


}
