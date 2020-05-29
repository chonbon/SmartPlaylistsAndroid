package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.chonbonstudios.smartplaylists.ModelData.DataHandler;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    // User clicked Transfer Playlists

    public void transferPlaylists(View view) {
        //Toast.makeText(this, "Clicked Transfer Playlists", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, TransferActivity.class));
    }

    //User clicked schedule transfer of playlists
    public void scheduleTransfer(View view) {
        //Toast.makeText(this, "Clicked Schedule Transfer", Toast.LENGTH_SHORT).show();
    }

    //User clicked upgrade to pro
    public void upgradeToPro(View view) {
        //Toast.makeText(this,"Clicked Upgrade To Pro",Toast.LENGTH_SHORT).show();
    }
}
