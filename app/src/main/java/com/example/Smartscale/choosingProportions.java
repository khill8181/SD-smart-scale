package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class choosingProportions extends AppCompatActivity {
    ListView list;
    SimpleCursorAdapter adapter;
    ArrayList<String> data;
    Cursor cursor;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing_proportions);
        data = new ArrayList<String>();
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        db = smartscaleDBHelper.getReadableDatabase();
        Intent intent = getIntent();
        ArrayList<Integer> ids = intent.getIntegerArrayListExtra("ids");
        int foodCount = ids.size();
        String[] stringIds = new String[foodCount];
        for(int i=0; i<foodCount ; i++)
            stringIds[i] = ids.get(i).toString();
        String threeOrMoreFoods = "";
        if(foodCount>2)
        {
            String specifyId = "or _id = ?";
            for(int i = foodCount-2; i >0;i--)
            {
                threeOrMoreFoods = threeOrMoreFoods + specifyId;
            }
        }
        cursor = db.query("Foodlist", new String[] {"_id","food","mass","calories"},"_id = ? or _id = ?".concat(threeOrMoreFoods), stringIds
                ,null,null,null);
        list = (ListView) findViewById(R.id.foodList);
        choosingProportionsAdapter adapter = new choosingProportionsAdapter(this, cursor);
        list.setAdapter(adapter);
        adapter.setListener(new choosingProportionsAdapter.Listener() {
            @Override
            public void onLoseFocus(String food, String proportion, String mass, String calories) {
                if (!data.contains(food)) {data.add(food);data.add(proportion);data.add(mass);data.add(calories);}
                else if (data.get(data.indexOf(food) + 1) != proportion) data.set(data.indexOf(food)+1,proportion);
                }
        });

    }


    public void submitProportions(View view)
    {
        Intent intent = new Intent(this, addDailyEntry.class);
        intent.putStringArrayListExtra("proportionData",data);
        intent.putExtra("isProportionEntry",true);
        startActivity(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
        db.close();
    }
}