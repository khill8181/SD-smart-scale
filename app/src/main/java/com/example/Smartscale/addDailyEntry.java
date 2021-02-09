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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class addDailyEntry extends AppCompatActivity {
    double calMassRatio;
    double calCountRatio;
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
    int calGoal;
    boolean isCountEntry;
    double firstEntryRatio;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_daily_entry);
        intent = getIntent();
        isProportionEntry = intent.getBooleanExtra("isProportionEntry", false);
        isCountEntry = intent.getBooleanExtra("isCountEntry",false);
        TextView propEntryText = (TextView) findViewById(R.id.propEntryText);
        propEntryValue = (TextView) findViewById(R.id.propEntryValue);
        dbText = (TextView) findViewById(R.id.foodName);
        calLeft = (TextView) findViewById(R.id.calLeftAddingEntry);
        db = smartscaleDBHelper.getReadableDatabase();
        setCaloriesLeft();
        if(!isProportionEntry) {
            propEntryText.setVisibility(View.GONE);
            propEntryValue.setVisibility(View.GONE);
            int intNum = intent.getIntExtra("id", 0);
            Cursor cursor = db.query("Foodlist", new String[]{"food", "mass", "calories","count"}
                    , "_id = ?", new String[]{Integer.toString(intNum)}, null, null, null);
            if (cursor.moveToFirst()) {
                food = cursor.getString(0);
                double mass = Double.parseDouble(cursor.getString(1));
                double calories = Double.parseDouble(cursor.getString(2));
                int count = cursor.getInt(3);
                calCountRatio = calories/count;
                calMassRatio = calories / mass;
            }
            dbText.setText(food);
            cursor.close();
        }
        //handles combos based on given weight
        else if (!isCountEntry)
        {
            proportionData = intent.getStringArrayListExtra("proportionData");
            for(int i = 1; i < proportionData.size() ; i += 5 )
                sumOfRatios += Double.parseDouble(proportionData.get(i));
            double chosenAmountOfCalories = intent.getDoubleExtra("comboCalAmount",-1);
            if (chosenAmountOfCalories != -1) totalCaloriesBeingProportioned = chosenAmountOfCalories;
            else totalCaloriesBeingProportioned = currentCalLeft;
            proportionedEntry();
        }
        //handles combo based on count of item
        else
        {
            ArrayList<String> firstEntry = intent.getStringArrayListExtra("firstEntry");
            food = firstEntry.get(0);
            int count = Integer.parseInt(firstEntry.get(2));
            double calories = Double.parseDouble(firstEntry.get(1));
            firstEntryRatio = Double.parseDouble(firstEntry.get(3));
            dbText.setText(food);
            calCountRatio = calories/count;
            proportionData = new ArrayList<String>();
            proportionData.add("token string for logic purposes in insertDailyEntry");
        }
    }

    public void setCaloriesLeft() {
        Cursor calConsumedCursor = db.query("calories", new String[] {"calGoal","calConsumed"},"date = ?",
                new String [] {MainActivity.createDateString(Calendar.getInstance(),true)},null,null,null);
        calConsumedCursor.moveToFirst();
        calGoal = calConsumedCursor.getInt(0);
        calConsumedToday = calConsumedCursor.getDouble(1);
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
        proportionData.subList(0,5).clear();
        propEntryValue.setText(String.format("%.1f", fractionOfTotalCalories*totalCaloriesBeingProportioned));
        dbText.setText(food);
    }

    public void calcCalories(View view)
    {
        EditText editText = (EditText) findViewById(R.id.givenMass);
        strEntryMass = editText.getText().toString();
        double entryMass = Double.parseDouble(strEntryMass);

        if(isCountEntry) entryCalories = calCountRatio*entryMass;
        else entryCalories = calMassRatio*entryMass;

        strEntryCalories = String.format("%.1f", entryCalories);
        strEntryMass = String.format("%.1f", entryMass);
        TextView calories = (TextView) findViewById(R.id.calcCalories);
        calories.setText(strEntryCalories);
        strCalLeft = String.format("%.1f", currentCalLeft-entryCalories);
        calLeft.setText(strCalLeft);
    }

    public void insertDailyEntry(View view)
    {
        String date = MainActivity.createDateString(Calendar.getInstance(),true);
        ContentValues contentValues = new ContentValues();
        contentValues.put("calConsumed",calConsumedToday+entryCalories);
        db.update("calories",contentValues,"date=?",new String[]{date});
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
            if(!isCountEntry) proportionedEntry();
            else countComboSetup();
        }
    }

    public void countComboSetup()
    {
        isCountEntry = false;
        proportionData = intent.getStringArrayListExtra("proportionData");
        sumOfRatios = firstEntryRatio;
        for(int i = 1; i < proportionData.size() ; i += 5 )
            sumOfRatios += Double.parseDouble(proportionData.get(i));
        totalCaloriesBeingProportioned = entryCalories*(sumOfRatios/firstEntryRatio);
        proportionedEntry();
    }
}