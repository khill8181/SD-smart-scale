package com.example.Smartscale;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class chooseFood extends AppCompatActivity {
    private SQLiteDatabase db;
    private Cursor cursor;
    SimpleCursorAdapter adapter;
    ListView list;
    Button submitFoodChoices;
    ArrayList<Integer> ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_food2);
        submitFoodChoices = (Button) findViewById(R.id.submitCombo);
        submitFoodChoices.setVisibility(View.GONE);
        list = (ListView) findViewById(R.id.foodList);
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        db = smartscaleDBHelper.getReadableDatabase();
        cursor = db.query("Foodlist", new String[] {"_id","food",
                "mass", "calories", "countable"},null,null,null,null,null);

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
                        Intent intent = new Intent(chooseFood.this, addDailyEntry.class);
                        intent.putExtra("id", (int) id);
                        startActivity(intent);
                    }
                };
        //Assign the listener to the list view
        list.setOnItemClickListener(itemClickListener);
    }

    public void proportionedFoodSelection(View view)
    {
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        submitFoodChoices.setVisibility(view.VISIBLE);
        ids = new ArrayList<Integer>();
        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice ,cursor,
                new String[] {"food"},
                new int[] {android.R.id.text1}, 0);
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
                        //Intent intent = new Intent(chooseFood.this, addDailyEntry.class);
                        CheckedTextView box = (CheckedTextView) itemView;
                        if (box.isChecked()) ids.add(new Integer((int)id));
                        else ids.remove(new Integer((int)id));
                    }
                };

        //Assign the listener to the list view
        list.setOnItemClickListener(itemClickListener);


    }

    public void submitSelections(View view)
    {
        Intent intent = new Intent(this, choosingProportions.class);
        intent.putIntegerArrayListExtra("ids",ids);
        startActivity(intent);
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