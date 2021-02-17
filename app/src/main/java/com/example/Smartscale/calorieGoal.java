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
import android.widget.EditText;

import java.lang.Object;

public class calorieGoal extends AppCompatActivity {
    SharedPreferences sharedpreferences;
    private SQLiteDatabase db;
    String focusedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_goal);
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        db = smartscaleDBHelper.getReadableDatabase();
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        focusedDate = sharedpreferences.getString("focusedDate","string");
        Cursor calGoalCursor = db.query("calories", new String[] {"calGoal"},"date = ?",
                new String [] {focusedDate},null,null,null);
        calGoalCursor.moveToFirst();
        int calGoal = calGoalCursor.getInt(0);
        EditText editText = (EditText) findViewById(R.id.editText2);
        editText.setText(Integer.toString(calGoal));
    }

    public void submitNewGoal(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText2);
        int calGoal = Integer.parseInt(editText.getText().toString());
        ContentValues contentValues = new ContentValues();
        contentValues.put("calGoal",calGoal);
        db.update("calories",contentValues,"date=?",new String[]{focusedDate});
        startActivity(intent);
    }
}