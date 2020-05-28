package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.view.View;
import android.widget.Toast;

import com.chonbonstudios.smartplaylists.ModelData.DataHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    OkHttpClient client;
    DataHandler dh;

    String token, appleToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();
        Uri uri = getIntent().getData();
        Log.v(TAG, ""+ uri);

        if(uri != null)parseToken(uri);

        appleToken = getIntent().getStringExtra("AppleMusicToken");

        dh = new DataHandler(this);
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

    //If there is a uri from the intent, parse the code and request an auth token and refresh
    public void parseToken(Uri uri){
        token = uri.getQueryParameter("code");
        String base = getString(R.string.spotify_client_id) + ":" + getString(R.string.spotify_client_secret);
        String encoded = Base64.encodeToString(base.getBytes(),Base64.NO_WRAP);

        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code",token)
                .add("redirect_uri",getString(R.string.spotify_redirect)).build();
        Request request = new Request.Builder()
                .url(getString(R.string.api_spotify_accesss))
                .post(requestBody)
                .header("Authorization", "Basic "+ encoded)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback(){

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if(response.isSuccessful()){
                        String res = response.body().string();
                        Log.v(TAG, res);
                        JSONObject object = new JSONObject(res);
                        dh.writeStringData(dh.SPOTIFY, object.getString("access_token"));
                        dh.writeStringData(dh.SPOTIFY_REFRESH, object.getString("refresh_token"));
                    }
                    Log.e(TAG, response.toString());
                } catch (IOException | JSONException e){
                    Log.e(TAG, "IO Exception caught: ", e);
                }

            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "IO Exception caught: ", e);
            }
        });
    }
}
