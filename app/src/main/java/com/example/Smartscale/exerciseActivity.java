package com.example.Smartscale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import static com.example.Smartscale.exercise.*;


public class exerciseActivity extends Activity {
    public static final String EXTRA_EXERCISEID = "exerciseId";
    int exerciseId;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        TextView name = (TextView) findViewById(R.id.title);

        exerciseId = (Integer)getIntent().getExtras().get(EXTRA_EXERCISEID);
        exercise exercise = exercises[exerciseId];
        name.setText(exercise.getName());
    }
    public void submitCaloriesBurned(View view){
        Intent intent = new Intent(this, MainActivity.class);
        EditText calories = (EditText) findViewById(R.id.calories);


    }
    public void calcCalories(View view){
        EditText calories = (EditText) findViewById(R.id.calories);
        EditText duration = (EditText) findViewById(R.id.durationEditText);
        EditText weight = (EditText) findViewById(R.id.weightEditText);

        if (!duration.getText().toString().equals("") && !weight.getText().toString().equals("")){
            double dur = (double)Integer.parseInt(duration.getText().toString());
            double wei = (double)Integer.parseInt(weight.getText().toString());
            double met = exercises[exerciseId].getMET();
            double cal = dur*(met*3.5*(wei/2.205))/200;
            calories.setText(Integer.toString((int) cal));
        }
    }
}
