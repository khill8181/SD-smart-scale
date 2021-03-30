package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class addFoodToTable extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food_to_table);
    }

    public void submitNewFood(View view)
    {
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        SQLiteDatabase db = smartscaleDBHelper.getReadableDatabase();
        EditText foodNameET = findViewById(R.id.foodNameET);
        EditText massET = findViewById(R.id.massET);
        EditText caloriesET = findViewById(R.id.caloriesET);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        EditText countET = findViewById(R.id.countET);

        String foodName = foodNameET.getText().toString();
        String massStr = massET.getText().toString();
        String caloriesStr = caloriesET.getText().toString();
        String countStr = countET.getText().toString();

        if (foodName.contentEquals("")||massStr.contentEquals("")||caloriesStr.contentEquals("")||
                massStr.contentEquals("0")||caloriesStr.contentEquals("0"))
        {
            Context context = getApplicationContext();
            CharSequence text = "Missing a required input";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        else
        {
            double gramMass;
            if(radioGroup.getCheckedRadioButtonId()==R.id.radio_grams) gramMass = Integer.parseInt(massStr);
            else gramMass = Integer.parseInt(massStr)*28.35;

            int count;
            if(countStr.isEmpty()) count = 0;
            else count = Integer.parseInt(countStr);

            SmartscaleDatabaseHelper.insertNewFood(db,foodName,gramMass,Integer.parseInt(caloriesStr),count);
            startActivity(new Intent(addFoodToTable.this, MainActivity.class));
        }
    }
}