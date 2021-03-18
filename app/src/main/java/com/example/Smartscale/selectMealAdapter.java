package com.example.Smartscale;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class selectMealAdapter extends CursorAdapter {

    public selectMealAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView mealNameTV = view.findViewById(android.R.id.text1);
        String mealName = cursor.getString(1);
        mealNameTV.setText(mealName);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), confirmAddMeal.class);
                intent.putExtra("mealName", mealName);
                view.getContext().startActivity(intent);
            }});
    }
}
