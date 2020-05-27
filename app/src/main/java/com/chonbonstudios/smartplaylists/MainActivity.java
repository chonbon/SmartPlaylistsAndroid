package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    String token, appleToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Uri uri = getIntent().getData();
        Log.v(TAG, ""+ uri);

        if(uri != null)parseToken(uri);

        appleToken = getIntent().getStringExtra("AppleMusicToken");

    }

    // User clicked Transfer Playlists

    public void transferPlaylists(View view) {
        Toast.makeText(this, "Clicked Transfer Playlists", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, TransferActivity.class).putExtra("TOKEN", token));
    }

    //User clicked schedule transfer of playlists
    public void scheduleTransfer(View view) {
        Toast.makeText(this, "Clicked Schedule Transfer", Toast.LENGTH_SHORT).show();
    }

    //User clicked upgrade to pro
    public void upgradeToPro(View view) {
        Toast.makeText(this,"Clicked Upgrade To Pro",Toast.LENGTH_SHORT).show();
    }

    //If there is a uri from the intent, parse the token
    public void parseToken(Uri uri){
        token = uri.getFragment();
        token = token.substring(13);
        String temp ="";
        for(int i = 0; i < token.length(); i++){
            if(token.charAt(i) != '&'){
                temp += token.charAt(i);
            } else {
                break;
            }
        }
        Log.v(TAG, "Token = " + temp);
    }
}
