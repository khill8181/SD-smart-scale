package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class chooseCountableItem extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_countable_item);
        ListView listView = findViewById(R.id.chooseCountableList);
        Intent intent = getIntent();
        ArrayList<String> data = intent.getStringArrayListExtra("proportionData");
        ArrayList<String> countables = new ArrayList<String>();
        int foodCount = intent.getIntExtra("foodCount",0);
        int i = 1;
        int j = 0;
        String food;
        while(i <= foodCount)//5th thing
        {
            food = data.get(j); j += 4;
            if (!data.get(j).contentEquals("0")) countables.add(food);
            if(i != foodCount)j++;
            i++;
        }
        String[] countableArray = new String[countables.size()];
        for(i=0; i<countables.size() ; i++)
            countableArray[i] = countables.get(i);
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countableArray);
        listView.setAdapter(itemsAdapter);

        //Create the listener
        AdapterView.OnItemClickListener itemClickListener =
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> listDrinks,
                                            View itemView,
                                            int position,
                                            long id) {
                        //Pass the drink the user clicks on to DrinkActivity
                        Intent newIntent = new Intent(chooseCountableItem.this, addDailyEntry.class);
                        String chosenFood = countableArray[position];
                        int indexOfChosenFoodInDataArrayList = data.indexOf(chosenFood);
                        ArrayList<String> firstEntry = new ArrayList<String>();
                        firstEntry.add(chosenFood);
                        firstEntry.add(data.get(indexOfChosenFoodInDataArrayList+3));
                        firstEntry.add(data.get(indexOfChosenFoodInDataArrayList+4));
                        firstEntry.add(data.get(indexOfChosenFoodInDataArrayList+1));
                        //for (int z=1;z<=5;z++)
                        data.remove(indexOfChosenFoodInDataArrayList);
                        data.remove(indexOfChosenFoodInDataArrayList);
                        data.remove(indexOfChosenFoodInDataArrayList);
                        data.remove(indexOfChosenFoodInDataArrayList);
                        data.remove(indexOfChosenFoodInDataArrayList);
                        newIntent.putStringArrayListExtra("firstEntry",firstEntry);
                        newIntent.putExtra("isProportionEntry",true);
                        newIntent.putExtra("isCountEntry",true);
                        newIntent.putStringArrayListExtra("proportionData",data);
                        startActivity(newIntent);
                    }
                };

        //Assign the listener to the list view
        listView.setOnItemClickListener(itemClickListener);
    }
}