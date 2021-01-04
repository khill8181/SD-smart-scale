package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.Smartscale.MESSAGE";
    private SQLiteDatabase db;
    private Cursor cursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        int calGoal = sharedpreferences.getInt("calGoal",2000);
        TextView text = (TextView) findViewById(R.id.textView3);
        text.setText(Integer.toString(calGoal));

        SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
        try{
            db = smartscaleDBHelper.getReadableDatabase();
            cursor = db.query("FOODLOG", new String[] {"_id","FOOD","DATE",
                    "WEIGHT", "CALORIES"},null,null,null,null,null);
            /*if (cursor.moveToFirst()){
                String food = cursor.getString(1);
                TextView dbText = (TextView) findViewById(R.id.textView4);
                dbText.setText(food);
            }*/

                   SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.food_log_item ,cursor,
                    new String[] {"FOOD","CALORIES"},
                    new int[] {R.id.food, R.id.calories}, 0);

        //FoodLogCursorAdapter adapter = new FoodLogCursorAdapter(this, cursor);


            ListView list = (ListView) findViewById(R.id.listView);
            list.setAdapter(adapter);
        }
        catch(SQLiteException e){
            Toast toast = Toast.makeText(this,
                    "DB unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
        db.close();
    }

    public void changeGoal(View view)
    {
        Intent intent = new Intent(this, calorieGoal.class);
        startActivity(intent);
    }
/*
    public boolean onOptionsItemSelected(MenuItem item) {

        //switch (item.getItemId()) {
          //  case R.id.goal: return true;
            //default: return true;}
        Intent intent = new Intent(this, calorieGoal.class);
        startActivity(intent);
        return true;


    }
*/
}
