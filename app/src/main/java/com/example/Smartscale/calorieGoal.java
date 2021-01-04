package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.lang.Object;

public class calorieGoal extends AppCompatActivity {
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_goal);
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        int calGoal = sharedpreferences.getInt("calGoal",2000);
        EditText editText = (EditText) findViewById(R.id.editText2);
        editText.setText(Integer.toString(calGoal));
    }

    public void submitNewGoal(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        EditText editText = (EditText) findViewById(R.id.editText2);
        int calGoal = Integer.parseInt(editText.getText().toString());
        editor.putInt("calGoal",calGoal);
        editor.commit();
        startActivity(intent);
    }
}