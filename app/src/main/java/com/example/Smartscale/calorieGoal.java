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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.lang.Object;

public class calorieGoal extends AppCompatActivity {
    SharedPreferences sharedpreferences;
    private SQLiteDatabase db;
    String focusedDate;
    String gender = null;
    String weightGoal = null;

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
/*
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_male:
                if (checked) gender = "male";
                    break;
            case R.id.radio_female:
                if (checked) gender = "female";
                    break;
        }
    }
    public void onRadioButton2Clicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_loseWeight:
                if (checked) weightGoal = "lose";
                break;
            case R.id.radio_gainWeight:
                if (checked) weightGoal = "gain";
                break;
            case R.id.radio_maintainWeight:
                if (checked) weightGoal = "maintain";
                break;
        }
    }*/
    public void calcCalories(View view) {
        EditText editText = (EditText) findViewById(R.id.editText2);
        EditText ageText = (EditText) findViewById(R.id.ageEditText);
        EditText weightText = (EditText) findViewById(R.id.weightEditText);
        EditText heightText = (EditText) findViewById(R.id.heightEditText);

        RadioGroup sexRG = findViewById(R.id.sexRG);
        if(sexRG.getCheckedRadioButtonId()==R.id.male) gender = "male";
        else gender = "female";

        RadioGroup goalRG = findViewById(R.id.goalRG);
        if(goalRG.getCheckedRadioButtonId()==R.id.lose) weightGoal = "lose";
        else if(goalRG.getCheckedRadioButtonId()==R.id.gain) weightGoal = "gain";
        else weightGoal = "maintain";

        if(!ageText.getText().toString().equals("")&&
                !weightText.getText().toString().equals("")&&
                !heightText.getText().toString().equals("")&&
                gender!=null) {
            int age = Integer.parseInt(ageText.getText().toString());
            int height = Integer.parseInt(heightText.getText().toString());
            height = (int) (2.54 * height);
            int weight = Integer.parseInt(weightText.getText().toString());
            weight = (int) (weight / 2.205);
            int calGoal;
            if (gender.equals("male"))
                calGoal = (int) ((10 * weight) + (6.25 * height) - (5 * age) + 5);
            else
                calGoal = (int) ((10 * weight) + (6.25 * height) - (5 * age) - 161);
            if (weightGoal!=null){
                if(weightGoal.equals("lose"))
                    calGoal = (int)(calGoal*.9);
                else if(weightGoal.equals("gain"))
                    calGoal = (int)(calGoal*1.1);
            }
            editText.setText(Integer.toString(calGoal));
        }
        else{
            Context context = getApplicationContext();
            CharSequence text = "Missing one or more parameters";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        }
    }


}