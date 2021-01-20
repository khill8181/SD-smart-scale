package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;

public class DeleteDailyEntry extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_daily_entry);
    }

    public void dontDelete(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void doDelete(View view)
    {
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        SQLiteDatabase db = smartscaleDBHelper.getReadableDatabase();
        Intent intent = getIntent();
        int intNum = intent.getIntExtra("id", 0);
        db.delete("FOODLOG","_id = ?", new String[] {Integer.toString(intNum)} );
        Intent leaveIntent = new Intent(this, MainActivity.class);
        startActivity(leaveIntent);
    }


}