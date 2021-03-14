package com.example.Smartscale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class completeDelayedMeasAdapter extends CursorAdapter {


    public completeDelayedMeasAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView food = (TextView) view.findViewById(android.R.id.text1);
        String mealTime = cursor.getString(cursor.getColumnIndex("mealTime"));
        String foodString = cursor.getString(cursor.getColumnIndex("food"));
        int foodID = cursor.getInt(cursor.getColumnIndex("foodID"));
        double initialMeas = cursor.getDouble(cursor.getColumnIndex("mass"));
        food.setText(foodString);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(),addDailyEntry.class);
                intent.putExtra("id",foodID);
                intent.putExtra("initialMeasurement",initialMeas);
                intent.putExtra("isCompleteDelayedMeasurement",true);
                intent.putExtra("mealTime",mealTime);
                view.getContext().startActivity(intent);
            }});
    }
}