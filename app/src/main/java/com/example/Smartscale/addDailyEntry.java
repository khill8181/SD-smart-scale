package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class addDailyEntry extends AppCompatActivity {
    double calMassRatio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_daily_entry);

        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        SQLiteDatabase db = smartscaleDBHelper.getReadableDatabase();
        Intent intent = getIntent();
        int intNum = intent.getIntExtra("id", 0);
        Cursor cursor = db.query("Foodlist", new String[] {"food","weight","calories"}
            ,"_id = ?", new String[] {Integer.toString(intNum)}, null, null, null);
        if (cursor.moveToFirst()){
            String food = cursor.getString(0);
            TextView dbText = (TextView) findViewById(R.id.foodName);
            dbText.setText(food);
            double mass = cursor.getDouble(1);
            double calories = cursor.getDouble(2);
            calMassRatio = calories/mass;
        }
    }

    public void calcCalories(View view)
    {
        EditText editText = (EditText) findViewById(R.id.givenMass);
        double mass = Double.parseDouble(editText.getText().toString());
        double calcCalories = calMassRatio*mass;
        String strDouble = String.format("%.1f", calcCalories);
        TextView calories = (TextView) findViewById(R.id.calcCalories);
        calories.setText(strDouble);
    }
}