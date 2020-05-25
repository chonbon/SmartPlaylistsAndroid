package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

// This activity is for initial launch or the user is signed out of all streaming services,
// this will handle logging in to a single service to allow user to then initiate transfer of playlists
public class NotSignedInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_signed_in);

    }

    // User clicks to log in to spotify
    public void loginSpotify(View view) {
        Toast.makeText(this, "Clicked Spotify Login", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(NotSignedInActivity.this, MainActivity.class));
    }

    // User clicks to log in to Apple Music
    public void loginAppleMusic(View view) {
        Toast.makeText(this, "Clicked Apple Music Login", Toast.LENGTH_SHORT).show();

    }
}
