package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.chonbonstudios.smartplaylists.ModelData.DataHandler;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_SCREEN_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DataHandler dh = new DataHandler(SplashActivity.this);
                // compare both tokens to default values
                if(dh.getAppleMusicUserToken() != "" || dh.getSpotifyUserToken() != ""){
                    // User has Tokens, eventually add a check to see if they are still valid, for now send it
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, NotSignedInActivity.class));
                }
                //the current activity will get finished.
                finish();
            }
        }, SPLASH_SCREEN_TIME_OUT);

    }
}
