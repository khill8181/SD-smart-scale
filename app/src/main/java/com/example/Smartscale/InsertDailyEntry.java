package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class InsertDailyEntry extends AppCompatActivity {
    private SQLiteDatabase db;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_daily_entry);
    }

    public void addEntryToDB(View view)
    {
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        Intent intent = new Intent(this, MainActivity.class);
        EditText food = (EditText) findViewById(R.id.food1);
        EditText calories = (EditText) findViewById(R.id.calorie1);
        int calInt = Integer.parseInt(calories.getText().toString());
        String foodString = food.getText().toString();
        try {
            db = smartscaleDBHelper.getReadableDatabase();
            SmartscaleDatabaseHelper.insertEntry(db, foodString, 3.33, calInt);
        }
        catch(SQLiteException e){
            Toast toast = Toast.makeText(this,
                    "DB unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
        startActivity(intent);
    }

}