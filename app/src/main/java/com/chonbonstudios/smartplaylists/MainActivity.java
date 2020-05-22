package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // User clicked Transfer Playlists
    public void transferPlaylists(View view) {
        Toast.makeText(this, "Clicked Transfer Playlists", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, TransferActivity.class));
    }

    //User clicked schedule transfer of playlists
    public void scheduleTransfer(View view) {
        Toast.makeText(this, "Clicked Schedule Transfer", Toast.LENGTH_SHORT).show();
    }

    //User clicked upgrade to pro
    public void upgradeToPro(View view) {
        Toast.makeText(this,"Clicked Upgrade To Pro",Toast.LENGTH_SHORT).show();
    }
}
