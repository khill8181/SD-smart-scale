package com.example.Smartscale;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class foodLogAdapter extends CursorAdapter {

    public foodLogAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.food_log_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView food = (TextView) view.findViewById(R.id.food);
        TextView amounts = (TextView) view.findViewById(R.id.amounts);
        //TextView mass = (TextView) view.findViewById(R.id.mass);
        String foodString = cursor.getString(cursor.getColumnIndex("food"));
        String caloriesString = String.format("%.1f", cursor.getDouble(cursor.getColumnIndex("calories")));
        String massUnit = cursor.getString(4);
        String massString = String.format("%.1f", cursor.getDouble(cursor.getColumnIndex("mass")));
        if(massUnit.contentEquals("")) massString = massString.substring(0,massString.indexOf('.'));
        int id = cursor.getInt(0);
        massString+=massUnit;
        food.setText(foodString); amounts.setText(caloriesString); //mass.setText(massString);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), DeleteDailyEntry.class);
                intent.putExtra("id", id);
                view.getContext().startActivity(intent);
            }});
    }
}
