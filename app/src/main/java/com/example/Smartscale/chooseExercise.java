package com.example.Smartscale;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.content.Intent;
import android.widget.AdapterView;
import android.view.View;
import android.widget.ArrayAdapter;

import static com.example.Smartscale.exercise.*;

public class chooseExercise extends Activity {
    ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_exercise);
        list = (ListView)findViewById(R.id.exerciseList);
        ArrayAdapter<exercise> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, exercise.exercises);
        list.setAdapter(listAdapter);

        AdapterView.OnItemClickListener itemClickListener =
                new AdapterView.OnItemClickListener(){
                    public void onItemClick(AdapterView<?> exerciseList,
                                            View itemView,
                                            int position,
                                            long id){
                        Intent intent = new Intent(chooseExercise.this, exerciseActivity.class);
                        intent.putExtra(exerciseActivity.EXTRA_EXERCISEID, (int) id);
                        startActivity(intent);
                    }
                };
        list.setOnItemClickListener(itemClickListener);
    }
}
