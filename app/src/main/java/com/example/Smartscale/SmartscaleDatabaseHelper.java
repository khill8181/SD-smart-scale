package com.example.Smartscale;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Date;
import java.util.Calendar;

public class SmartscaleDatabaseHelper extends SQLiteOpenHelper {

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
                    + "mass real, "
                    + "massUnit text,"
                    + "calories real,"
                    + "mealTime text);");

        db.execSQL("CREATE TABLE Foodlist ("
                +"_id integer primary key autoincrement, "
                +"food text, "
                +"mass integer, "
                +"calories integer, "
                +"count integer);");

        db.execSQL("create table calories (date text primary key, calGoal integer, calConsumed real);");
        db.execSQL("create table delayedEntries (_id integer primary key autoincrement, foodID integer, mass real, massUnit text, mealTime text)");
        insertNewFood(db, "popcorn", 2,10,3);
        insertNewFood(db, "corn", 3,20,0);
        insertNewFood(db, "pop", 4,15,0);
        insertNewFood(db, "opcor", 6,25,1);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    }

    public static void insertDelayedMeasurement(SQLiteDatabase db, int foodID, double mass, String massUnit, String mealTime)
    {
        ContentValues entryValues = new ContentValues();
        entryValues.put("foodID",foodID);
        entryValues.put("mass",mass);
        entryValues.put("massUnit",massUnit);
        entryValues.put("mealTime", mealTime);
        db.insert("delayedEntries",null,entryValues);
    }

    //daily entry
    public static void insertEntry(SQLiteDatabase db,
                                    String food,
                                    String date,
                                    double mass,
                                    String massUnit,
                                    double calories,
                                    String mealTime) {
        ContentValues entryValues = new ContentValues();
        entryValues.put("food", food);
        entryValues.put("date", date);
        entryValues.put("mass", mass);
        entryValues.put("massUnit", massUnit);
        entryValues.put("calories", calories);
        entryValues.put("mealTime", mealTime);
        db.insert("Foodlog", null, entryValues);
    }


    //food table
    public static long insertNewFood(SQLiteDatabase db,
                                   String food,
                                   int mass,
                                   int calories,
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
