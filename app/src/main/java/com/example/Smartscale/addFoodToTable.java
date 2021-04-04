package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class addFoodToTable extends AppCompatActivity {
    boolean noResults = false;
    String searchedTerm;
    EditText searchedTermET;
    ArrayList<webAPIFood> searchResults;
    TextView noResultsView;
    ListView list;
    static boolean onMainPage = true;
    SQLiteOpenHelper smartscaleDBHelper;
    SQLiteDatabase db;
    LinearLayout foodDetailsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food_to_table);
        smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        db = smartscaleDBHelper.getReadableDatabase();
        searchedTermET = findViewById(R.id.searchBar);
        noResultsView = findViewById(R.id.noResultsView);
        foodDetailsLayout = findViewById(R.id.foodDetailsLayout);
        list = findViewById(R.id.foodList);
        noResultsView.setVisibility(View.GONE);
        onMainPage = true;
    }

    public void submitNewFood(View view)
    {
        EditText foodNameET = findViewById(R.id.foodNameET);
        EditText massET = findViewById(R.id.massET);
        EditText caloriesET = findViewById(R.id.caloriesET);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        EditText countET = findViewById(R.id.countET);

        String foodName = foodNameET.getText().toString();
        String massStr = massET.getText().toString();
        String caloriesStr = caloriesET.getText().toString();
        String countStr = countET.getText().toString();

        if (foodName.contentEquals("")||massStr.contentEquals("")||caloriesStr.contentEquals("")||
                massStr.contentEquals("0")||caloriesStr.contentEquals("0"))
        {
            Context context = getApplicationContext();
            CharSequence text = "Missing a required input";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        else
        {
            double gramMass;
            if(radioGroup.getCheckedRadioButtonId()==R.id.radio_grams) gramMass = Integer.parseInt(massStr);
            else gramMass = Integer.parseInt(massStr)*28.35;

            int count;
            if(countStr.isEmpty()) count = 0;
            else count = Integer.parseInt(countStr);

            SmartscaleDatabaseHelper.insertNewFood(db,foodName,gramMass,Integer.parseInt(caloriesStr),count);
            startActivity(new Intent(addFoodToTable.this, MainActivity.class));
        }
    }

    public void displaySearchResults(View view)
    {
        searchedTerm = searchedTermET.getText().toString();
        if(searchedTerm.contentEquals("")) return;
        foodDetailsLayout.setVisibility(View.GONE);
        onMainPage = false;
        searchResults = new ArrayList<webAPIFood>();
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
                    ArrayAdapter<webAPIFood> arrayAdapter = new ArrayAdapter<webAPIFood>(addFoodToTable.this, android.R.layout.simple_list_item_1, array);
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

                                    Context context = getApplicationContext();
                                    CharSequence text = "Food added to database";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
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

    public void addRecipe(View view)
    {
        SharedPreferences sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat("recipeCalorieTotal",0); editor.commit();
        Intent intent = new Intent(this,chooseFood.class);
        intent.putExtra("isRecipeItem",true);
        startActivity(intent);
    }

    public void onBackPressed()
    {
        if(onMainPage){
            Intent newIntent = new Intent(this,MainActivity.class);
            startActivity(newIntent);
        }
        else
        {Intent newIntent = new Intent(this,addFoodToTable.class);
            startActivity(newIntent);}
    }
}