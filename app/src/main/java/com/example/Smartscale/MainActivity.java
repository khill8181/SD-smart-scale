package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.util.Calendar;
//import androidx.appcompat.widget.Toolbar;
import android.widget.Toolbar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.Smartscale.MESSAGE";
    private SQLiteDatabase db;
    private Cursor cursor;
    SimpleCursorAdapter adapter;
    Calendar currentDate, focusedDate;
    TextView date;
    ListView list;
    TextView calGoalView;
    TextView calLeftView;
    SharedPreferences sharedpreferences;
    static boolean startUp = true;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setActionBar(toolbar);
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        list = (ListView) findViewById(R.id.dailyEntries);
        calGoalView = (TextView) findViewById(R.id.calorieGoal);
        calLeftView = (TextView) findViewById(R.id.caloriesLeft);
        db = smartscaleDBHelper.getReadableDatabase();
        date = (TextView) findViewById(R.id.date);
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        currentDate = Calendar.getInstance(); // Returns instance with current date and time set
        if (startUp) {focusedDate = Calendar.getInstance(); editor.putString("focusedDate",createDateString(currentDate,true));}
        else focusedDate = parseDateStringToCalendar(sharedpreferences.getString("focusedDate","string"));
        //String lastDayOpened = sharedpreferences.getString("lastDayOpened", "never");
        String lastDayOpened = "1-10-2021";//for testing
        String currentDateString = createDateString(currentDate,true);

        editor.putString("oldestDateAvailable", "1-10-2021");//for testing
        SmartscaleDatabaseHelper.insertCalorieEntry(db, lastDayOpened, 2000, 0);
        if (lastDayOpened == "never" || lastDayOpened != currentDateString)
        {
            if(lastDayOpened == "never") {
                SmartscaleDatabaseHelper.insertCalorieEntry(db, currentDateString, 2000, 0); //insert todays date into the table, only for first time opening app
                editor.putString("oldestDateAvailable", createDateString(currentDate,true));
            }
            else {
                Calendar parsedDate = parseDateStringToCalendar(lastDayOpened);
                Cursor mostRecentGoalCursor = db.query("calories", new String[] {"calGoal"},"date = ?",
                        new String [] {lastDayOpened},null,null,null);
                mostRecentGoalCursor.moveToFirst();
                int calGoal = mostRecentGoalCursor.getInt(0);
                do {
                rollForward(parsedDate);
                SmartscaleDatabaseHelper.insertCalorieEntry(db, createDateString(parsedDate, true), calGoal, 0);
            }
            while (parsedDate.get(Calendar.DAY_OF_YEAR) != currentDate.get(Calendar.DAY_OF_YEAR));
        //starting at day after given date, add entries up to and including today
        //update the last day app was opened
               }
            editor.putString("lastDayOpened",currentDateString);
            editor.commit();
        }

        setCalGoalAndLeft(focusedDate);
        displayDate(focusedDate);

        cursor = db.query("Foodlog", new String[] {"_id","food","calories","mass","massUnit"},"date = ?",new String[] {createDateString(focusedDate,true)}
                ,null,null,null);

        foodLogAdapter adapter = new foodLogAdapter(this,cursor);

        list.setAdapter(adapter);
    }

    static public Calendar parseDateStringToCalendar(String dateString)
    {
        Calendar parsedDate = Calendar.getInstance();
        int index = dateString.indexOf('-');
        parsedDate.set(Calendar.MONTH, Integer.parseInt(dateString.substring(0, index)));
        parsedDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateString.substring(index + 1, dateString.lastIndexOf('-'))));
        parsedDate.set(Calendar.YEAR, 2021);
        return parsedDate;
    }

    public void setCalGoalAndLeft(Calendar date)
    {
        Cursor calConsumedCursor = db.query("calories", new String[] {"calGoal","calConsumed"},"date = ?",
                new String [] {createDateString(focusedDate,true)},null,null,null);
        calConsumedCursor.moveToFirst();
        int calGoal = calConsumedCursor.getInt(0);
        double calConsumed = calConsumedCursor.getDouble(1);
        calGoalView.setText(Integer.toString(calGoal));
        calLeftView.setText(String.format("%.1f", calGoal-calConsumed));
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
        db.close();
    }

    @Override
    public void onPause() {
        super.onPause();
        startUp = false;
    }

    static public String createDateString(Calendar date, boolean db)
    {
        int month;
       //for db vs for display
        if (db) month = date.get(Calendar.MONTH);
        else month = date.get(Calendar.MONTH) + 1;
        int year = date.get(Calendar.YEAR);
        int day = date.get(Calendar.DAY_OF_MONTH);
        return Integer.toString(month) +"-"+day+"-"+year;
    }

    public void displayDate(Calendar focusedDate)
    {
        if (focusedDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR)) date.setText("Today");
        else date.setText(createDateString(focusedDate,false));
    }

    public void previousDay(View view)
    {
        String oldestDateAvailable = sharedpreferences.getString("oldestDateAvailable","string");
        String string = createDateString(focusedDate,true);
        if(!oldestDateAvailable.equals(string))
        {
            rollBackward(focusedDate);
            editor.putString("focusedDate",createDateString(focusedDate,true));
            Cursor newCursor = db.query("Foodlog", new String[]{"_id", "food", "calories","mass"}, "date = ?", new String[]{createDateString(focusedDate, true)}
                    , null, null, null);
            adapter.changeCursor(newCursor);
            displayDate(focusedDate);
            cursor = newCursor;
            setCalGoalAndLeft(focusedDate);
            editor.commit();
        }
    }

    public static void rollForward(Calendar date)
    {
        if (date.get(Calendar.MONTH)==Calendar.JANUARY && date.get(Calendar.DAY_OF_MONTH)==31)
            date.set(2021, Calendar.FEBRUARY,1);
        else date.roll(Calendar.DATE, true);
    }

    public static void rollBackward(Calendar date)
    {
        if(date.get(Calendar.MONTH)==0 && date.get(Calendar.DAY_OF_MONTH)==1)
        {int year = date.get(Calendar.YEAR) - 1; date.set(year,11,31);}
        else if (date.get(Calendar.MONTH)==Calendar.FEBRUARY && date.get(Calendar.DAY_OF_MONTH)==1) date.set(2021,Calendar.JANUARY,31);
        else date.roll(Calendar.DATE, false);
    }

    public void nextDay(View view)
    {
        if (focusedDate.get(Calendar.DAY_OF_YEAR) != currentDate.get(Calendar.DAY_OF_YEAR))
        {
            rollForward(focusedDate);
            editor.putString("focusedDate",createDateString(focusedDate,true));
            Cursor newCursor = db.query("Foodlog", new String[] {"_id","food","calories","mass"},"date = ?",new String[] {createDateString(focusedDate,true)}
                    ,null,null,null);
            adapter.changeCursor(newCursor);
            displayDate(focusedDate);
            cursor = newCursor;
            setCalGoalAndLeft(focusedDate);
            editor.commit();
        }
    }

    public void changeGoal(View view)
    {
        Intent intent = new Intent(this, calorieGoal.class);
        startActivity(intent);
    }

    public void completeDelayedMeasurement(View view)
    {
        Intent intent = new Intent(MainActivity.this, completeDelayedMeas.class);
        startActivity(intent);
    }

    public void insertDailyEntry(View view)
    {

        Intent intent = new Intent(MainActivity.this, chooseFood.class);
        if (view.getId() == R.id.begDelMeas) intent.putExtra("isDelayedMeasurement",true);
        startActivity(intent);


    }

}
