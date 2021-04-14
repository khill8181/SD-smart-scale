package com.example.Smartscale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Text;

import static com.example.Smartscale.exercise.*;



public class exerciseActivity extends Activity {
    SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
    SQLiteDatabase db;
    SharedPreferences sharedPreferences;
    String focusedDate;
    double calConsumedToday;
    int calGoal;


    public static final String EXTRA_EXERCISEID = "exerciseId";
    int exerciseId;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        TextView name = (TextView) findViewById(R.id.title);

        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        focusedDate = sharedPreferences.getString("focusedDate","string");
        db = smartscaleDBHelper.getReadableDatabase();

        exerciseId = (Integer)getIntent().getExtras().get(EXTRA_EXERCISEID);
        exercise exercise = exercises[exerciseId];
        name.setText(exercise.getName());
    }
    public void submitCaloriesBurned(View view){
        Intent intent = new Intent(this, MainActivity.class);
        EditText calories = (EditText) findViewById(R.id.calories);
        EditText duration = (EditText) findViewById(R.id.durationEditText);
        TextView title = (TextView) findViewById(R.id.title);
        String exerciseName = title.getText().toString();
        if (calories.getText().toString().equals("") || duration.getText().toString().equals("")){
            Context context = getApplicationContext();
            CharSequence text = "Calories and Duration required";
            int time = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, time);
            toast.show();
        }
        if (!calories.getText().toString().equals("") && !duration.getText().toString().equals("")){
            int calBurned = -1*Integer.parseInt(calories.getText().toString());
            float dur = (float) Integer.parseInt(duration.getText().toString());
            Cursor calConsumedCursor = db.query("calories", new String[] {"calGoal","calConsumed"},"date = ?",
                    new String [] {focusedDate},null,null,null);
            calConsumedCursor.moveToFirst();
            calGoal = calConsumedCursor.getInt(0);
            calConsumedToday = calConsumedCursor.getDouble(1);
            ContentValues contentValues = new ContentValues();
            contentValues.put("calConsumed", calConsumedToday + calBurned);
            db.update("calories", contentValues, "date=?", new String[]{focusedDate});
            SmartscaleDatabaseHelper.insertEntry(db, exerciseName, focusedDate, dur, "Min", calBurned ,"breakfast");
            Context context = getApplicationContext();
            CharSequence text = "Exercise added";
            int time = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, time);
            toast.show();
            startActivity(intent);
        }
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
