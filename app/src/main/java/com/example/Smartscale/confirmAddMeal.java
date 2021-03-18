package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class confirmAddMeal extends AppCompatActivity {
    Cursor cursor;
    SQLiteOpenHelper smartscaleDBHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_add_meal);
        Intent receivedIntent = getIntent();
        String mealName = receivedIntent.getStringExtra("mealName");
        smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        db = smartscaleDBHelper.getReadableDatabase();
        cursor = db.query("savedMeals", new String[] {"_id","food","calories","mass","massUnit"},
                "mealName = ?",new String[]{mealName},
                null,null,null);
        foodLogAdapter adapter = new foodLogAdapter(this,cursor);
        adapter.setListener(new foodLogAdapter.Listener() {
            @Override
            public void onClickCustom(int id) {
            }
        });
        ListView listView = findViewById(R.id.confirmAddMealList);
        listView.setAdapter(adapter);
    }

    public void addMeal(View view)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        String focusedDate = sharedPreferences.getString("focusedDate","string");
        cursor.moveToFirst();
        do {
            SmartscaleDatabaseHelper.insertEntry(db,cursor.getString(1),focusedDate,cursor.getDouble(3),
                    cursor.getString(4),cursor.getDouble(2),"breakfast");
            cursor.moveToNext();
        }
        while (!cursor.isAfterLast());
        startActivity(new Intent(this, MainActivity.class));
    }


}