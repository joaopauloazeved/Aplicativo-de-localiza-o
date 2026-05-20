package com.example.atividade3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textView);
        Button buttonLocation = findViewById(R.id.button_location);
        Button buttonmaps = findViewById(R.id.button_maps);

        Animation fade = AnimationUtils.loadAnimation(this, R.anim.suav);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);

        textView.startAnimation(fade);
        buttonLocation.startAnimation(pulse);

        buttonLocation.setOnClickListener(this);
        buttonmaps.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId()== R.id.button_location){
            Intent i=new Intent(this, LocationActivity.class);
            startActivity(i);
            }
        if (view.getId()== R.id.button_maps){
            Intent i=new Intent(this, MapsActivity.class);
            startActivity(i);
        }
    }
}