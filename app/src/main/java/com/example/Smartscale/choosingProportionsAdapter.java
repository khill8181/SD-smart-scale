package com.example.Smartscale;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class choosingProportionsAdapter extends CursorAdapter {
    private  Listener listener;

    interface Listener {
        void onLoseFocus(String food, String proportion, String mass, String calories);
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    public choosingProportionsAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.proportion_food_choice, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView food = (TextView) view.findViewById(R.id.food);
        String foodString = cursor.getString(cursor.getColumnIndex("food"));
        String mass = cursor.getString(cursor.getColumnIndex("mass"));
        String calories = cursor.getString(cursor.getColumnIndex("calories"));
        food.setText(foodString);

        EditText proportion = (EditText) view.findViewById((R.id.proportion));
        proportion.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    listener.onLoseFocus(foodString,proportion.getText().toString(),mass,calories);
            }
        });


        /*// Find fields to populate in inflated template
        TextView tvBody = (TextView) view.findViewById(R.id.tvBody);
        TextView tvPriority = (TextView) view.findViewById(R.id.tvPriority);
        // Extract properties from cursor
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        int priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority"));
        // Populate fields with extracted properties
        tvBody.setText(body);
        tvPriority.setText(String.valueOf(priority));*/
    }

}
