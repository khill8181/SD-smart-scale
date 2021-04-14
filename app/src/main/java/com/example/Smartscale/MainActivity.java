package com.example.Smartscale;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
                            implements nameNewMealDialogFragment.nameNewMealDialogListener{
    public static final String EXTRA_MESSAGE = "com.example.Smartscale.MESSAGE";
    private SQLiteDatabase db;
    private Cursor breakfastCursor;
    //Cursor dinnerCursor;
    foodLogAdapter breakfastAdapter;
    foodLogAdapter dinnerAdapter;
    Calendar currentDate, focusedDate;
    TextView date;
    ListView breakfastList;
    //ListView dinnerList;
    TextView calGoalView;
    TextView calLeftView;
    SharedPreferences sharedpreferences;
    static boolean startUp = true;
    SharedPreferences.Editor editor;
    private String deviceName = null;
    float caloriesLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        breakfastList = (ListView) findViewById(R.id.breakfastList);
        //dinnerList = findViewById(R.id.dinnerList);
        calGoalView = (TextView) findViewById(R.id.calorieGoal);
        calLeftView = (TextView) findViewById(R.id.caloriesLeft);
        db = smartscaleDBHelper.getReadableDatabase();
        date = (TextView) findViewById(R.id.date);
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        currentDate = Calendar.getInstance(); // Returns instance with current date and time set
        if (startUp) {focusedDate = Calendar.getInstance(); editor.putString("focusedDate",createDateString(currentDate,true));}
        else focusedDate = parseDateStringToCalendar(sharedpreferences.getString("focusedDate","string"));
        String lastDayOpened = sharedpreferences.getString("lastDayOpened", "never");
        //String lastDayOpened = "1-10-2021";//for testing
        String currentDateString = createDateString(currentDate,true);

        //editor.putString("oldestDateAvailable", "1-10-2021");//for testing
        //SmartscaleDatabaseHelper.insertCalorieEntry(db, lastDayOpened, 2000, 0);
        if (lastDayOpened == "never" || !lastDayOpened.contentEquals(currentDateString))
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

        breakfastCursor = db.query("Foodlog", new String[] {"_id","food","calories","mass","massUnit"},
                "date = ? and mealTime = ?",new String[] {createDateString(focusedDate,true),"breakfast"},
                null,null,null);

        /*dinnerCursor = db.query("Foodlog", new String[] {"_id","food","calories","mass","massUnit"},
                "date = ? and mealTime = ?",new String[] {createDateString(focusedDate,true),"dinner"}
                ,null,null,null);*/

        breakfastAdapter = new foodLogAdapter(this,breakfastCursor);
        //dinnerAdapter = new foodLogAdapter(this,dinnerCursor);
        breakfastAdapter.setListener(new foodLogAdapter.Listener() {
            @Override
            public void onClickCustom(int id) {
                Intent intent = new Intent(MainActivity.this, DeleteDailyEntry.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        breakfastList.setAdapter(breakfastAdapter);
        //dinnerList.setAdapter(dinnerAdapter);

        //Button breakfastEntry = findViewById(R.id.breakfastEntry);
        //Button dinnerEntry = findViewById(R.id.dinnerEntry);
        /*View.OnLongClickListener longClickListener = new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view){
                Intent intent = new Intent(MainActivity.this, chooseFood.class);
                if(view.getId() == R.id.breakfastEntry) editor.putString("mealTime", "breakfast");
                else editor.putString("mealTime","dinner");
                editor.commit();
                intent.putExtra("isDelayedMeasurement",true);
                startActivity(intent);
                return true;
            }
        };
        breakfastEntry.setOnLongClickListener(longClickListener);*/
        //dinnerEntry.setOnLongClickListener(longClickListener);

        View.OnLongClickListener longClickListener = new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view){
                pencilDailyEntryDialogFragment pencilDailyEntryDialogFragment = new pencilDailyEntryDialogFragment();
                pencilDailyEntryDialogFragment.show(getSupportFragmentManager(),"pencil in");
                return true;
            }
        };
        findViewById(R.id.addDailyEntry).setOnLongClickListener(longClickListener);

        //// Open Bluetooth for the first time
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        SharedPreferences btDetail = getSharedPreferences("btDetail", MODE_PRIVATE);
        SharedPreferences.Editor edit = btDetail.edit();
        if (isFirstRun) {
            //show start activity
            startActivity(new Intent(MainActivity.this, SelectDeviceActivity.class));
        }
        deviceName = btDetail.getString("btName", null);
        if (deviceName == null && !isFirstRun) {
            edit.putString("btName", getIntent().getStringExtra("deviceName"));
            edit.putString("btAddress", getIntent().getStringExtra("deviceAddress"));
            edit.commit();
        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).commit();
        ////

    }

    public void onSubmitMealName(String mealName)
    {
        if (breakfastCursor.moveToFirst())
        {
            SmartscaleDatabaseHelper.insertMealName(db,mealName);
            do {
                SmartscaleDatabaseHelper.insertSavedMealEntry(db,mealName,breakfastCursor.getString(1),breakfastCursor.getDouble(3)
                ,breakfastCursor.getString(4),breakfastCursor.getDouble(2));
                breakfastCursor.moveToNext();
            }
            while (!breakfastCursor.isAfterLast());

            Context context = getApplicationContext();
            CharSequence text = "Meal added!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        //no entries to be saved
        else {
            Context context = getApplicationContext();
            CharSequence text = "No entries to add";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
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
        caloriesLeft = (float) (calGoal-calConsumed);
        calGoalView.setText(Integer.toString(calGoal));
        calLeftView.setText(String.format("%.1f", caloriesLeft));
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        breakfastCursor.close();
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
            Cursor newBreakfastCursor = db.query("Foodlog", new String[]{"_id", "food", "calories","mass","massUnit"},
                    "date = ? and mealTime = ?", new String[]{createDateString(focusedDate, true),"breakfast"}
                    , null, null, null);
            breakfastCursor = newBreakfastCursor;
            breakfastAdapter.changeCursor(newBreakfastCursor);
            /*Cursor newDinnerCursor = db.query("Foodlog", new String[]{"_id", "food", "calories","mass","massUnit"},
                    "date = ? and mealTime = ?", new String[]{createDateString(focusedDate, true),"dinner"}
                    , null, null, null);
            dinnerAdapter.changeCursor(newDinnerCursor);*/

            displayDate(focusedDate);
            setCalGoalAndLeft(focusedDate);
            editor.commit();
        }
        else
        {
            Context context = getApplicationContext();
            CharSequence text = "This is the oldest date available";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
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
            Cursor newBreakfastCursor = db.query("Foodlog", new String[] {"_id","food","calories","mass","massUnit"}
            ,"date = ? and mealTime = ?",new String[] {createDateString(focusedDate,true), "breakfast"}
                    ,null,null,null);
            breakfastCursor = newBreakfastCursor;
            breakfastAdapter.changeCursor(newBreakfastCursor);
            /*Cursor newDinnerCursor = db.query("Foodlog", new String[] {"_id","food","calories","mass","massUnit"}
                    ,"date = ? and mealTime = ?",new String[] {createDateString(focusedDate,true), "dinner"}
                    ,null,null,null);
            dinnerAdapter.changeCursor(newDinnerCursor);*/

            displayDate(focusedDate);
            setCalGoalAndLeft(focusedDate);
            editor.commit();
        }
    }

    public void changeGoal()
    {
        Intent intent = new Intent(this, calorieGoal.class);
        startActivity(intent);
    }

    public void insertDailyEntry(View view)
    {

        Intent intent = new Intent(MainActivity.this, chooseFood.class);
        editor.putFloat("caloriesLeft",caloriesLeft);
        editor.putString("mealTime", "breakfast");
        editor.commit();
        startActivity(intent);
    }

    ///// Toolbar Menu /////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();  // Get the id of the selected item

        // Perform action based on which item was selected
        if (id == R.id.setCalorieGoal) {
            changeGoal();
            return true;
        }
        else if (id == R.id.connectScale) {
            Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.addFoodMenuItem) {
            startActivity(new Intent(MainActivity.this , addFoodToTable.class));
            return true;
        }
        else if(id == R.id.saveMealMenuItem) {
            nameNewMealDialogFragment nameMeal = new nameNewMealDialogFragment();
            nameMeal.show(getSupportFragmentManager(),"name meal");
        }
        else if(id == R.id.beginDelayedMI){
            Intent intent = new Intent(MainActivity.this, chooseFood.class);
            editor.putFloat("caloriesLeft",caloriesLeft);
            editor.putString("mealTime", "breakfast");
            intent.putExtra("isBeginDelayedMeasurement",true);
            editor.commit();
            startActivity(intent);
        }
        else if(id == R.id.completeDelayedMI){
            startActivity(new Intent(MainActivity.this, completeDelayedMeas.class));
        }
        else if(id == R.id.addExercise){
            startActivity(new Intent(MainActivity.this, chooseExercise.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed()
    {

    }
}
