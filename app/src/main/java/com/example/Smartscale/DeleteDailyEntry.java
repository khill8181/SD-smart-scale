package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;

import java.util.Calendar;

public class DeleteDailyEntry extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_daily_entry);
    }

    public void dontDelete(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void doDelete(View view)
    {
        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        SQLiteDatabase db = smartscaleDBHelper.getReadableDatabase();
        Intent intent = getIntent();
        int intNum = intent.getIntExtra("id", 0);
        Cursor cursor = db.query("Foodlog",new String[]{"calories"}, "_id=?",new String[]{Integer.toString(intNum)},
                            null,null,null);
        cursor.moveToFirst();
        double deletedCalories = Double.parseDouble(cursor.getString(0));
       //Note that deletions can only be made for the current day
        String focusedDate = sharedPreferences.getString("focusedDate","string");
        Cursor secondCursor = db.query("calories", new String[]{"calConsumed"},"date=?",
                                new String[]{focusedDate},null,null,null);
        secondCursor.moveToFirst();
        double caloriesConsumed = secondCursor.getDouble(0);
        ContentValues contentValues = new ContentValues();
        contentValues.put("calConsumed",caloriesConsumed-deletedCalories);
        db.update("calories",contentValues,"date=?",new String[]{focusedDate});
        db.delete("Foodlog","_id = ?", new String[] {Integer.toString(intNum)} );
        Intent leaveIntent = new Intent(this, MainActivity.class);
        startActivity(leaveIntent);
    }

}