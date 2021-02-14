package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import static android.database.DatabaseUtils.dumpCursorToString;

public class completeDelayedMeas extends AppCompatActivity {
    private SQLiteDatabase db;
    private Cursor cursor;
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_delayed_meas);
        ListView listView = findViewById(R.id.listOfIncompleteMeas);
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        db = smartscaleDBHelper.getReadableDatabase();
        cursor = db.rawQuery("select delayedEntries._id,Foodlist.food,delayedEntries.foodID,delayedEntries.mass from Foodlist " +
                "inner join delayedEntries on Foodlist._id=delayedEntries.foodID",null);
        //String balls = dumpCursorToString(cursor);
        completeDelayedMeasAdapter adapter = new completeDelayedMeasAdapter(this,cursor);
        listView.setAdapter(adapter);
        /*
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

*/

    }
}