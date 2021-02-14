package com.example.Smartscale;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Date;
import java.util.Calendar;

public class SmartscaleDatabaseHelper extends SQLiteOpenHelper {

    static Calendar date;
    private static final String DB_NAME = "smartScale";
    private static final int DB_VERSION = 1;
    SmartscaleDatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE Foodlog ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "food TEXT, "
                    + "date TEXT, "
                    + "mass text, "
                    + "calories text);");

        db.execSQL("CREATE TABLE Foodlist ("
                +"_id integer primary key autoincrement, "
                +"food text, "
                +"mass text, "
                +"calories text, "
                +"count integer);");

        db.execSQL("create table calories (date text primary key, calGoal integer, calConsumed real);");
        db.execSQL("create table delayedEntries (_id integer primary key autoincrement, foodID integer, mass real)");
        insertNewFood(db, "popcorn", "1.2","5.6",3);
        insertNewFood(db, "corn", "3.6","4.8",0);
        insertNewFood(db, "pop", "9.6","3.8",0);
        insertNewFood(db, "opcor", "4.2","1.7",1);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    }

    public static void insertDelayedMeasurement(SQLiteDatabase db, int foodID, double mass)
    {
        ContentValues entryValues = new ContentValues();
        entryValues.put("foodID",foodID);
        entryValues.put("mass",mass);
        db.insert("delayedEntries",null,entryValues);
    }

    //daily entry
    public static void insertEntry(SQLiteDatabase db,
                                    String food,
                                    String mass,
                                    String calories) {
        ContentValues entryValues = new ContentValues();
        entryValues.put("food", food);
        date = Calendar.getInstance();
        entryValues.put("date", MainActivity.createDateString(date,true));
        entryValues.put("mass", mass);
        entryValues.put("calories", calories);
        db.insert("Foodlog", null, entryValues);
    }

    public static void insertEntryTesting(SQLiteDatabase db,
                                   String food,
                                   Calendar date,
                                   String mass,
                                   String calories) {
        ContentValues entryValues = new ContentValues();
        entryValues.put("food", food);
        entryValues.put("date", MainActivity.createDateString(date,true));
        entryValues.put("mass", mass);
        entryValues.put("calories", calories);
        db.insert("Foodlog", null, entryValues);
    }

    //food table
    public static long insertNewFood(SQLiteDatabase db,
                                   String food,
                                   String mass,
                                   String calories,
                                   int count) {
        ContentValues entryValues = new ContentValues();
        entryValues.put("food", food);
        entryValues.put("mass", mass);
        entryValues.put("calories", calories);
        entryValues.put("count", count);
        return db.insert("Foodlist", null, entryValues);
    }

    public static void insertCalorieEntry(SQLiteDatabase db,String date, int calGoal, double calConsumed)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("calGoal", calGoal);
        contentValues.put("calConsumed", calConsumed);
        db.insert("calories", null, contentValues);
    }

}
