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
    int foodCount;
    Button restOfCalButton;
    Button chooseCalAmtBttn;
    Button chooseCountableBttn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing_proportions);
        restOfCalButton = findViewById(R.id.restOfCal);
        chooseCalAmtBttn = findViewById(R.id.chooseCalAmount);
        chooseCountableBttn = findViewById(R.id.chooseCountable);
        restOfCalButton.setEnabled(false);
        chooseCalAmtBttn.setEnabled(false);
        chooseCountableBttn.setEnabled(false);
        data = new ArrayList<String>();
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        db = smartscaleDBHelper.getReadableDatabase();
        Intent intent = getIntent();
        ArrayList<Integer> ids = intent.getIntegerArrayListExtra("ids");
        foodCount = ids.size();
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
        cursor = db.query("Foodlist", new String[] {"_id","food","mass","calories","count"},"_id = ? or _id = ?".concat(threeOrMoreFoods), stringIds
                ,null,null,null);
        list = (ListView) findViewById(R.id.foodList);
        choosingProportionsAdapter adapter = new choosingProportionsAdapter(this, cursor);
        list.setAdapter(adapter);
        adapter.setListener(new choosingProportionsAdapter.Listener() {
            @Override
            public void onLoseFocus(String food, String proportion, String mass, String calories, String count) {
                if (!data.contains(food)) {data.add(food);data.add(proportion);data.add(mass);data.add(calories);data.add(count);}
                else if (data.get(data.indexOf(food) + 1) != proportion) data.set(data.indexOf(food)+1,proportion);
                if(data.size()/5 == foodCount)
                {
                    restOfCalButton.setEnabled(true);
                    chooseCalAmtBttn.setEnabled(true);
                    chooseCountableBttn.setEnabled(true);
                }
            }
        });

    }


    public void submitProportions(View view)
    {
        if (view.getId() == R.id.restOfCal)
        {
            Intent intent = new Intent(this, addDailyEntry.class);
            intent.putExtra("isProportionEntry",true);
            intentChooserHelperForsubmitProportions(intent);
        }
        if (view.getId() == R.id.chooseCalAmount){
            Intent intent = new Intent(this, chooseCalAmount.class);
            intentChooserHelperForsubmitProportions(intent);
        }
        if (view.getId() == R.id.chooseCountable){
            Intent intent = new Intent(this, chooseCountableItem.class);
            intent.putExtra("foodCount",foodCount);
            intentChooserHelperForsubmitProportions(intent);

        }
    }

    public void intentChooserHelperForsubmitProportions(Intent intent)
    {
        intent.putStringArrayListExtra("proportionData",data);
        startActivity(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
        db.close();
    }
}