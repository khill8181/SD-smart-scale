package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.View;
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
        ListView list = (ListView) findViewById(R.id.listView);

        try{
            db = smartscaleDBHelper.getReadableDatabase();
            cursor = db.query("FOODLOG", new String[] {"_id","FOOD","DATE",
                    "WEIGHT", "CALORIES"},null,null,null,null,null);
      /*      if (cursor.moveToFirst()){
                String food = cursor.getString(1);
                TextView dbText = (TextView) findViewById(R.id.textView4);
                dbText.setText(food);
            }*/

                   SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.food_log_item ,cursor,
                    new String[] {"FOOD","CALORIES"},
                    new int[] {R.id.food, R.id.calories}, 0);


            list.setAdapter(adapter);
        }
        catch(SQLiteException e){
            Toast toast = Toast.makeText(this,
                    "DB unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        //Create the listener
        AdapterView.OnItemClickListener itemClickListener =
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> list,
                                            View itemView,
                                            int position,
                                            long id) {
                        //Pass the drink the user clicks on to DrinkActivity
                        Intent intent = new Intent(MainActivity.this, DeleteDailyEntry.class);
                        intent.putExtra("id", (int) id);
                        startActivity(intent);
                    }
                };

        //Assign the listener to the list view
        list.setOnItemClickListener(itemClickListener);
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

    public void insertDailyEntry(View view)
    {
        Intent intent = new Intent(MainActivity.this, chooseFood.class);
        startActivity(intent);
        /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose entry type")
                .setItems(new String[] {"individual item","proportioned combo"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0)
                        {
                            intent.putExtra("isCounted", true);
                            startActivity(intent);
                        }
                        if (which == 1)
                        {
                            intent.putExtra("isCounted", false);
                            startActivity(intent);
                        }
                        if (which == 2) {}
                    }})
                .show();*/

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
