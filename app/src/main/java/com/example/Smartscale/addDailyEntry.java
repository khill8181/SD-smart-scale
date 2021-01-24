package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class addDailyEntry extends AppCompatActivity {
    double calMassRatio;
    SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
    SQLiteDatabase db;
    String food;
    String strEntryCalories;
    String strEntryMass;
    String strCalLeft;
    SharedPreferences sharedpreferences;
    double currentCalLeft;
    double projectedCaloriesLeft;
    TextView calLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_daily_entry);
        Intent intent = getIntent();
        int intNum = intent.getIntExtra("id", 0);
        db = smartscaleDBHelper.getReadableDatabase();
        Cursor cursor = db.query("Foodlist", new String[] {"food","mass","calories"}
            ,"_id = ?", new String[] {Integer.toString(intNum)}, null, null, null);
        if (cursor.moveToFirst()){
            food = cursor.getString(0);
            TextView dbText = (TextView) findViewById(R.id.foodName);
            dbText.setText(food);
            double mass = cursor.getDouble(1);
            double calories = cursor.getDouble(2);
            calMassRatio = calories/mass;
        }
        cursor.close();
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        currentCalLeft = sharedpreferences.getFloat("calLeft", 2000);
        calLeft = (TextView) findViewById(R.id.calLeftAddingEntry);
        calLeft.setText(String.format("%.1f", currentCalLeft));
    }

    public void calcCalories(View view)
    {
        EditText editText = (EditText) findViewById(R.id.givenMass);
        strEntryMass = editText.getText().toString();
        double entryMass = Double.parseDouble(strEntryMass);
        double entryCalories = calMassRatio*entryMass;
        strEntryCalories = String.format("%.1f", entryCalories);
        strEntryMass = String.format("%.1f", entryMass);
        TextView calories = (TextView) findViewById(R.id.calcCalories);
        calories.setText(strEntryCalories);

        projectedCaloriesLeft = currentCalLeft-entryCalories;
        strCalLeft = String.format("%.1f", projectedCaloriesLeft);
        calLeft.setText(strCalLeft);
    }

    public void insertDailyEntry(View view)
    {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat("calLeft", (float) projectedCaloriesLeft);
        editor.commit();
        SmartscaleDatabaseHelper.insertEntry(db, food, strEntryMass, strEntryCalories);
        db.close();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}