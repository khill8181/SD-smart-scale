package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

public class choosingProportions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing_proportions);
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        SQLiteDatabase db = smartscaleDBHelper.getReadableDatabase();
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("theBundle");
        //Cursor cursor = db.query("Foodlist", new String[] {"_id","food"},"date = ?",new String[] {dateString}
          //      ,null,null,null);
        int carp = 7;
    }
}