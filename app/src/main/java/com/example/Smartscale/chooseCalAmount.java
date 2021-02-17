package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class chooseCalAmount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_cal_amount);


    }

    public void submitCalAmountForCombo(View view)
    {
        Intent receivedIntent = getIntent();
        Intent intent = new Intent(this, addDailyEntry.class);
        EditText comboCalAmount = findViewById(R.id.comboCalAmount);
        double comboCalAmountDouble = Double.parseDouble(comboCalAmount.getText().toString());
        intent.putStringArrayListExtra("proportionData",receivedIntent.getStringArrayListExtra("proportionData"));
        intent.putExtra("isProportionEntry",true);
        intent.putExtra("comboCalAmount", comboCalAmountDouble);
        startActivity(intent);
    }

}