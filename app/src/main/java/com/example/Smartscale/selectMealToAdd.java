package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.ListView;

public class selectMealToAdd extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_meal_to_add);
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        SQLiteDatabase db = smartscaleDBHelper.getReadableDatabase();
        Cursor cursor = db.query("mealNames", new String[] {"_id","name"},
                null,null,
                null,null,null);
        selectMealAdapter adapter = new selectMealAdapter(this,cursor);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
    }
}