package com.example.Smartscale;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class chooseFood extends AppCompatActivity {
    private SQLiteDatabase db;
    private Cursor cursor;
    SimpleCursorAdapter adapter;
    ListView list;
    Button submitFoodChoices;
    ArrayList<Integer> ids;
    String searchedTerm;
    EditText searchedTermView;
    ArrayList<webAPIFood> searchResults;
    boolean noResults = false;
    TextView noResultsView;
    Button addPropComboBttn;
    Button addByCountBttn;
    LinearLayout searchBarLayout;
    static boolean onMainPage = true;
    Intent oldIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_food2);
        oldIntent = getIntent();
        onMainPage = true;
        addPropComboBttn = findViewById(R.id.addPropComboButton);
        noResultsView = findViewById(R.id.noResultsView);
        noResultsView.setVisibility(View.GONE);
        searchedTermView = findViewById(R.id.searchBar);
        submitFoodChoices = (Button) findViewById(R.id.submitCombo);
        addByCountBttn = findViewById(R.id.addByCountBttn);
        searchBarLayout = findViewById(R.id.searchBarLayout);
        submitFoodChoices.setVisibility(View.GONE);
        list = (ListView) findViewById(R.id.foodList);
        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        db = smartscaleDBHelper.getReadableDatabase();
        //messing around with stupid blinking cursor
        searchedTermView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) searchedTermView.clearFocus();}});

        cursor = db.query("Foodlist", new String[] {"_id","food",
                "mass", "calories", "count"},null,null,null,null,null);

        adapter = new SimpleCursorAdapter(this, R.layout.food_log_item ,cursor,
                new String[] {"food","calories"},
                new int[] {R.id.food, R.id.amounts}, 0);

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
                        intent.putExtra("isDelayedMeasurement",oldIntent.getBooleanExtra("isDelayedMeasurement",false));
                        startActivity(intent);
                    }
                };
        //Assign the listener to the list view
        list.setOnItemClickListener(itemClickListener);
    }

    public void proportionedFoodSelection(View view)
    {
        onMainPage = false;
        searchBarLayout.setVisibility(View.GONE);
        addByCountBttn.setVisibility(View.GONE);
        addPropComboBttn.setVisibility(View.GONE);
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

    public void onBackPressed()
    {
        if(onMainPage){
            Intent newIntent = new Intent(this,MainActivity.class);
            startActivity(newIntent);
        }
        else
        {Intent newIntent = new Intent(this,chooseFood.class);
        startActivity(newIntent);}
    }

    public void listCountableFoods(View view){
        onMainPage = false;
        searchBarLayout.setVisibility(View.GONE);
        addPropComboBttn.setVisibility(View.GONE);
        addByCountBttn.setVisibility(View.GONE);
        Cursor newCursor = db.query("Foodlist", new String[] {"_id","food",
                "mass", "calories", "count"},"count != ? ",new String[]{"0"},null,null,null);
        adapter.changeCursor(newCursor);
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
                        intent.putExtra("isCountEntry", true);
                        startActivity(intent);
                    }
                };
        //Assign the listener to the list view
        list.setOnItemClickListener(itemClickListener);
    }

    public void displaySearchResults(View view)
    {
        onMainPage = false;
        addPropComboBttn.setVisibility(View.GONE);
        addByCountBttn.setVisibility(View.GONE);
        searchResults = new ArrayList<webAPIFood>();
        searchedTerm = searchedTermView.getText().toString();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://trackapi.nutritionix.com/")
                .build();

        PlaceholderAPI placeholderAPI = retrofit.create(PlaceholderAPI.class);
        Call<ResponseBody> call = placeholderAPI.getPosts(searchedTerm,true,false);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) return;
                try{
                    String rawText = response.body().string();
                    if(rawText.contentEquals("{\"common\":[]}")) noResults = true;
                    int currentIndex = 0;
                    while(currentIndex != -1 && !noResults)
                    {
                        webAPIFood food = new webAPIFood();
                        currentIndex = rawText.indexOf("food_name",currentIndex) + 12;
                        food.foodName = rawText.substring(currentIndex,rawText.indexOf('\"',currentIndex));
                        if(rawText.indexOf("serving_weight_grams",currentIndex) < rawText.indexOf("\"attr_id\":208",currentIndex))
                        {
                            currentIndex = rawText.indexOf("serving_weight_grams", currentIndex) + "erving_weight_grams\":1".length();
                            food.mass = Math.round(Float.parseFloat(rawText.substring(currentIndex, rawText.indexOf(',', currentIndex))));
                            currentIndex = rawText.indexOf("\"attr_id\":208", currentIndex);
                            currentIndex = rawText.lastIndexOf("value", currentIndex) + 7;
                            food.calories = Math.round(Float.parseFloat(rawText.substring(currentIndex, rawText.indexOf(',', currentIndex))));
                        }
                        else
                        {
                            currentIndex = rawText.indexOf("\"attr_id\":208", currentIndex);
                            currentIndex = rawText.lastIndexOf("value", currentIndex) + 7;
                            food.calories = Math.round(Float.parseFloat(rawText.substring(currentIndex, rawText.indexOf(',', currentIndex))));
                            currentIndex = rawText.indexOf("serving_weight_grams", currentIndex) + "erving_weight_grams\":1".length();
                            food.mass = Math.round(Float.parseFloat(rawText.substring(currentIndex, rawText.indexOf(',', currentIndex))));
                        }

                        searchResults.add(food);
                        //testing to see if any foods left
                        currentIndex = rawText.indexOf("food_name",currentIndex);
                    }
                    if(noResults) noResultsView.setVisibility(View.VISIBLE);
                    noResults = false;
                    webAPIFood[] array = new webAPIFood[searchResults.size()];
                    searchResults.toArray(array);
                    ArrayAdapter<webAPIFood> arrayAdapter = new ArrayAdapter<webAPIFood>(chooseFood.this, android.R.layout.simple_list_item_1, array);
                    list.setAdapter(arrayAdapter);
                    AdapterView.OnItemClickListener itemClickListener =
                            new AdapterView.OnItemClickListener(){
                                @Override
                                public void onItemClick(AdapterView<?> list,
                                                        View itemView,
                                                        int position,
                                                        long id) {
                                    int dbID = (int) SmartscaleDatabaseHelper.insertNewFood(db,array[position].foodName,
                                                                            array[position].mass,
                                                                            array[position].calories,0);
                                    Intent intent = new Intent(chooseFood.this, addDailyEntry.class);
                                    intent.putExtra("id", (int) dbID);
                                    startActivity(intent);
                                }
                            };
                    //Assign the listener to the list view
                    list.setOnItemClickListener(itemClickListener);

                }
                catch(IOException ex){
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

    }



}