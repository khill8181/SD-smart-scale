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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        list = (ListView) findViewById(R.id.dailyEntries);
        db = smartscaleDBHelper.getReadableDatabase();
        date = (TextView) findViewById(R.id.date);
        //currentDate would be for going forward in time
        focusedDate = Calendar.getInstance();
        currentDate = Calendar.getInstance(); // Returns instance with current date and time set
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        //int calGoal = sharedpreferences.getInt("calGoal", 2000);
        //double calConsumedToday = sharedpreferences.getFloat("calConsumedToday", 0);


        String lastDayOpened = sharedpreferences.getString("lastDayOpened", "never");
        //String lastDayOpened = "0-29-2021";
        //SmartscaleDatabaseHelper.insertCalorieEntry(db, lastDayOpened, 2000, 0);
        String currentDateString = createDateString(currentDate,true);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("oldestDateAvailable", "0-29-2021");
        SmartscaleDatabaseHelper.insertCalorieEntry(db, "0-29-2021", 2000, 0);
        if (lastDayOpened == "never" || lastDayOpened != currentDateString){
            if(lastDayOpened == "never") {
                SmartscaleDatabaseHelper.insertCalorieEntry(db, currentDateString, 2000, 0); //insert todays date into the table, only for first time opening app
                editor.putString("oldestDateAvailable", createDateString(currentDate,true));
            }
            else {
                Calendar parsedDate = parseDateStringToCalendar(lastDayOpened);
                Cursor mostRecentGoalCursor = db.query("calories", new String[] {"calGoal"},"date = ?",
                        new String [] {createDateString(parsedDate,true)},null,null,null);
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
        calGoalView = (TextView) findViewById(R.id.calorieGoal);
        calLeftView = (TextView) findViewById(R.id.caloriesLeft);
        //currently, focusedDate is always currentDate
        setCalGoalAndLeft(focusedDate);
        /*Cursor calConsumedCursor = db.query("calories", new String[] {"calGoal","calConsumed"},"date = ?",
                            new String [] {createDateString(focusedDate,true)},null,null,null);
        calConsumedCursor.moveToFirst();
        int calGoal = calConsumedCursor.getInt(0);
        double calConsumed = calConsumedCursor.getDouble(1);
        calGoalView.setText(Integer.toString(calGoal));
        calLeftView.setText(String.format("%.1f", calGoal-calConsumed));*/

        cursor = db.query("Foodlog", new String[] {"_id","food","calories"},"date = ?",new String[] {createDateString(focusedDate,true)}
                ,null,null,null);

        adapter = new SimpleCursorAdapter(this, R.layout.food_log_item ,cursor,
        new String[] {"food","calories"},
        new int[] {R.id.food, R.id.calories}, 0);


        list.setAdapter(adapter);



        //Create the listener
        AdapterView.OnItemClickListener itemClickListener =
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> list,
                                            View itemView,
                                            int position,
                                            long id) {
                        //Pass the drink the user clicks on to DrinkActivity
                        Intent intent = new Intent(MainActivity.this, DeleteDailyEntry.class);
                        intent.putExtra("id", (int) id);
                        startActivity(intent);
                    }
                };

        //Assign the listener to the list view
        list.setOnItemClickListener(itemClickListener);
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

    public void previousDay(View view)
    {
        String oldestDateAvailable = sharedpreferences.getString("oldestDateAvailable","string");
        String string = createDateString(focusedDate,true);
        if(!oldestDateAvailable.equals(string))
        {
            rollBackward(focusedDate);
            Cursor newCursor = db.query("Foodlog", new String[]{"_id", "food", "calories"}, "date = ?", new String[]{createDateString(focusedDate, true)}
                    , null, null, null);
            adapter.changeCursor(newCursor);
            date.setText(createDateString(focusedDate, false));
            cursor = newCursor;
            setCalGoalAndLeft(focusedDate);
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
            Cursor newCursor = db.query("Foodlog", new String[] {"_id","food","calories"},"date = ?",new String[] {createDateString(focusedDate,true)}
                    ,null,null,null);
            adapter.changeCursor(newCursor);
            if (focusedDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR)) date.setText("Today");
            else date.setText(createDateString(focusedDate,false));
            cursor = newCursor;
            setCalGoalAndLeft(focusedDate);
        }
    }

    public void changeGoal(View view)
    {
        Intent intent = new Intent(this, calorieGoal.class);
        startActivity(intent);
    }

    public void insertDailyEntry(View view)
    {
        Intent intent = new Intent(MainActivity.this, chooseFood.class);
        startActivity(intent);
        /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose entry type")
                .setItems(new String[] {"individual item","proportioned combo"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0)
                        {
                            intent.putExtra("isCounted", true);
                            startActivity(intent);
                        }
                        if (which == 1)
                        {
                            intent.putExtra("isCounted", false);
                            startActivity(intent);
                        }
                        if (which == 2) {}
                    }})
                .show();*/

    }
/*
    public boolean onOptionsItemSelected(MenuItem item) {

        //switch (item.getItemId()) {
          //  case R.id.goal: return true;
            //default: return true;}
        Intent intent = new Intent(this, calorieGoal.class);
        startActivity(intent);
        return true;


    }
*/
}
