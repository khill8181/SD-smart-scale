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

import java.util.ArrayList;

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
    TextView calLeft;
    boolean isProportionEntry;
    protected ArrayList<String> proportionData;
    double sumOfRatios;
    TextView propEntryValue;
    TextView dbText;
    double totalCaloriesBeingProportioned;
    double calConsumedToday;
    double entryCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_daily_entry);
        Intent intent = getIntent();
        isProportionEntry = intent.getBooleanExtra("isProportionEntry", false);
        TextView propEntryText = (TextView) findViewById(R.id.propEntryText);
        propEntryValue = (TextView) findViewById(R.id.propEntryValue);
        dbText = (TextView) findViewById(R.id.foodName);
        calLeft = (TextView) findViewById(R.id.calLeftAddingEntry);
        setCaloriesLeft();
        db = smartscaleDBHelper.getReadableDatabase();
        if(!isProportionEntry) {
            propEntryText.setVisibility(View.GONE);
            propEntryValue.setVisibility(View.GONE);
            int intNum = intent.getIntExtra("id", 0);
            Cursor cursor = db.query("Foodlist", new String[]{"food", "mass", "calories"}
                    , "_id = ?", new String[]{Integer.toString(intNum)}, null, null, null);
            if (cursor.moveToFirst()) {
                food = cursor.getString(0);
                double mass = Double.parseDouble(cursor.getString(1));
                double calories = Double.parseDouble(cursor.getString(2));
                calMassRatio = calories / mass;
            }
            dbText.setText(food);
            cursor.close();
        }
        else
        {
            proportionData = intent.getStringArrayListExtra("proportionData");
            for(int i = 1; i < proportionData.size() ; i += 4 )
                sumOfRatios += Double.parseDouble(proportionData.get(i));
            totalCaloriesBeingProportioned = currentCalLeft;
            proportionedEntry();

        }
    }

    public void setCaloriesLeft()
    {
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        int calGoal = sharedpreferences.getInt("calGoal",2000);
        calConsumedToday = sharedpreferences.getFloat("calConsumedToday", 0);
        currentCalLeft = calGoal-calConsumedToday;
        calLeft.setText(String.format("%.1f", currentCalLeft));
    }

    public void proportionedEntry()
    {
        food = proportionData.get(0);
        double mass = Double.parseDouble(proportionData.get(2));
        double calories = Double.parseDouble(proportionData.get(3));
        calMassRatio = calories/mass;
        double fractionOfTotalCalories = Double.parseDouble(proportionData.get(1))/sumOfRatios;
        proportionData.subList(0,4).clear();
        propEntryValue.setText(String.format("%.1f", fractionOfTotalCalories*totalCaloriesBeingProportioned));
        dbText.setText(food);
    }

    public void calcCalories(View view)
    {
        EditText editText = (EditText) findViewById(R.id.givenMass);
        strEntryMass = editText.getText().toString();
        double entryMass = Double.parseDouble(strEntryMass);
        entryCalories = calMassRatio*entryMass;
        strEntryCalories = String.format("%.1f", entryCalories);
        strEntryMass = String.format("%.1f", entryMass);
        TextView calories = (TextView) findViewById(R.id.calcCalories);
        calories.setText(strEntryCalories);
        strCalLeft = String.format("%.1f", currentCalLeft-entryCalories);
        calLeft.setText(strCalLeft);
    }

    public void insertDailyEntry(View view)
    {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat("calConsumedToday", (float) (calConsumedToday + entryCalories));
        editor.commit();
        SmartscaleDatabaseHelper.insertEntry(db, food, strEntryMass, strEntryCalories);
        if (!isProportionEntry) db.close();
        if (!isProportionEntry || proportionData.isEmpty())
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else
        {
            setCaloriesLeft();
            proportionedEntry();
        }
    }
}