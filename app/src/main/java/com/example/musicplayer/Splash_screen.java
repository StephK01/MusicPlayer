package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class Splash_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent1=new Intent(Splash_screen.this, Register.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.no_animation,R.anim.no_animation);
                finish();
            }
        },2000);
        setContentView(R.layout.activity_splash_screen);
    }
}