package com.example.Smartscale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class nameNewMealDialogFragment extends DialogFragment {

    public interface nameNewMealDialogListener {
        public void onSubmitMealName(String mealName);}

    // Use this instance of the interface to deliver action events
    nameNewMealDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
            // Instantiate the Listener so we can send events to the host
            listener = (nameNewMealDialogListener) context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View mealNameView = inflater.inflate(R.layout.meal_name_dialog, null);

        builder.setTitle("Please provide a meal name")
                .setView(mealNameView)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText mealNameET = mealNameView.findViewById(R.id.mealName);
                        String mealName = mealNameET.getText().toString();
                        listener.onSubmitMealName(mealName);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
