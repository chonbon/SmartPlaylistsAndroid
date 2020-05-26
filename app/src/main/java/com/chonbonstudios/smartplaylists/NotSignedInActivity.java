package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// This activity is for initial launch or the user is signed out of all streaming services,
// this will handle logging in to a single service to allow user to then initiate transfer of playlists
public class NotSignedInActivity extends AppCompatActivity {
    OkHttpClient client;
    WebView mWebView;
    public static final String TAG = NotSignedInActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_signed_in);

        mWebView = findViewById(R.id.webView);

        client = new OkHttpClient();


    }

    // User clicks to log in to spotify
    public void loginSpotify(View view) {
        //Toast.makeText(this, "Clicked Spotify Login", Toast.LENGTH_SHORT).show();
        //startActivity(new Intent(NotSignedInActivity.this, MainActivity.class));
        //apiLoginSpotify();
        mWebView.loadUrl(getString(R.string.api_spotify_login));
    }

    // User clicks to log in to Apple Music
    public void loginAppleMusic(View view) {
        //Toast.makeText(this, "Clicked Apple Music Login", Toast.LENGTH_SHORT).show();

    }


    // Api call to spotify login
    public void apiLoginSpotify(){
        Request request = new Request.Builder()
                .url(getString(R.string.api_spotify_login))
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback(){

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if(response.isSuccessful()){
                        Log.v(TAG, response.body().string());
                    }
                } catch (IOException e){
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
