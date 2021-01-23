package com.example.Smartscale;
//InsertDailyEntry was changed to chooseFood

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

public class chooseFood extends AppCompatActivity {
    private SQLiteDatabase db;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_food);
        ListView list = (ListView) findViewById(R.id.foodList);
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        db = smartscaleDBHelper.getReadableDatabase();
        cursor = db.query("Foodlist", new String[] {"_id","food",
                "weight", "calories", "countable"},null,null,null,null,null);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.food_log_item ,cursor,
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
                        Intent intent = new Intent(chooseFood.this, addDailyEntry.class);
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

    /*
    public void addEntryToDB(View view)
    {
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        Intent intent = new Intent(this, MainActivity.class);
        EditText food = (EditText) findViewById(R.id.food1);
        EditText calories = (EditText) findViewById(R.id.calorie1);
        int calInt = Integer.parseInt(calories.getText().toString());
        String foodString = food.getText().toString();
        try {
            db = smartscaleDBHelper.getReadableDatabase();
            SmartscaleDatabaseHelper.insertEntry(db, foodString, 3.33, calInt);
        }
        catch(SQLiteException e){
            Toast toast = Toast.makeText(this,
                    "DB unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
        startActivity(intent);
    }*/

}