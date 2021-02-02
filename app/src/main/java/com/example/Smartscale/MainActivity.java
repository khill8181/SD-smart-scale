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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        date = (TextView) findViewById(R.id.date);
        //currentDate would be for going forward in time
        focusedDate = Calendar.getInstance();
        currentDate = Calendar.getInstance(); // Returns instance with current date and time set
        SharedPreferences sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        int calGoal = sharedpreferences.getInt("calGoal",2000);
        double calLeft = sharedpreferences.getFloat("calLeft", 2000);
        TextView text = (TextView) findViewById(R.id.calorieGoal);
        text.setText(Integer.toString(calGoal));
        TextView calLeftView = (TextView) findViewById(R.id.caloriesLeft);
        calLeftView.setText(String.format("%.1f", calLeft));
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        list = (ListView) findViewById(R.id.dailyEntries);
        db = smartscaleDBHelper.getReadableDatabase();
        cursor = db.query("Foodlog", new String[] {"_id","food","calories"},"date = ?",new String[] {createDateString(focusedDate,true)}
                ,null,null,null);
  /*      if (cursor.moveToFirst()){
            String food = cursor.getString(1);
            TextView dbText = (TextView) findViewById(R.id.textView4);
            dbText.setText(food);
        }*/

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
        if(focusedDate.get(Calendar.MONTH)==0 && focusedDate.get(Calendar.DAY_OF_MONTH)==1)
            {int year = focusedDate.get(Calendar.YEAR) - 1; focusedDate.set(year,11,31);}
        else if (focusedDate.get(Calendar.MONTH)==Calendar.FEBRUARY && focusedDate.get(Calendar.DAY_OF_MONTH)==1) focusedDate.set(2021,Calendar.JANUARY,31);
        else focusedDate.roll(Calendar.DATE, false);
        Cursor newCursor = db.query("Foodlog", new String[] {"_id","food","calories"},"date = ?",new String[] {createDateString(focusedDate,true)}
                                                                                ,null,null,null);
        adapter.changeCursor(newCursor);
        date.setText(createDateString(focusedDate,false));
        cursor = newCursor;
    }

    public void nextDay(View view)
    {
        if (focusedDate.get(Calendar.DAY_OF_YEAR) != currentDate.get(Calendar.DAY_OF_YEAR))
        {
            if (focusedDate.get(Calendar.MONTH)==Calendar.JANUARY && focusedDate.get(Calendar.DAY_OF_MONTH)==31)
                focusedDate.set(2021, Calendar.FEBRUARY,1);
            else focusedDate.roll(Calendar.DATE, true);

            Cursor newCursor = db.query("Foodlog", new String[] {"_id","food","calories"},"date = ?",new String[] {createDateString(focusedDate,true)}
                    ,null,null,null);
            adapter.changeCursor(newCursor);
            if (focusedDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR)) date.setText("Today");
            else date.setText(createDateString(focusedDate,false));
            cursor = newCursor;
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
