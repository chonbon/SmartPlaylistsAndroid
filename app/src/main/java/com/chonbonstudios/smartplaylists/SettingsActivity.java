package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import okhttp3.OkHttpClient;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.apple.android.sdk.authentication.AuthIntentBuilder;
import com.apple.android.sdk.authentication.AuthenticationFactory;
import com.apple.android.sdk.authentication.AuthenticationManager;
import com.apple.android.sdk.authentication.TokenError;
import com.apple.android.sdk.authentication.TokenResult;
import com.chonbonstudios.smartplaylists.ModelData.DataHandler;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = SettingsActivity.class.getSimpleName();
    private static final int REQUESTCODE_APPLEMUSIC_AUTH = 3456;
    DataHandler dh;
    TextView appleStatus,spotifyStatus,userStatus;
    OkHttpClient client;
    WebView mWebView;
    private AuthenticationManager authenticationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dh = new DataHandler(this);

        appleStatus = findViewById(R.id.txtAppleStatus);
        spotifyStatus = findViewById(R.id.txtSpotifyStatus);
        userStatus = findViewById(R.id.txtUserPaidStatus);

        //init views and http client
        mWebView = findViewById(R.id.webViewSettings);
        client = new OkHttpClient();

        // apple music sdk auth manager init
        if (authenticationManager == null) {
            authenticationManager = AuthenticationFactory.createAuthenticationManager(this);
        }

        setStatus();
    }


    public void setStatus(){
        if(dh.getAppleMusicUserToken() != ""){
            appleStatus.setText("Logged In!");
            appleStatus.setTextColor(ContextCompat.getColor(this,R.color.lightGreen));
        }
        if(dh.getSpotifyUserToken() != ""){
            spotifyStatus.setText("Logged In!");
            spotifyStatus.setTextColor(ContextCompat.getColor(this,R.color.lightGreen));
        }
        if(dh.getUserStatus() != "Free"){
            userStatus.setText("Pro");
        }
    }

    public void onClickApple(View view){
        if(dh.getAppleMusicUserToken() == ""){
            apiLoginApple();
        }
    }

    public void onClickSpotify(View view){
        if(dh.getSpotifyUserToken() == ""){
            mWebView.loadUrl(getString(R.string.api_spotify_login));
        }
    }

    // Api call to apple music to login
    public void apiLoginApple() {
        AuthIntentBuilder authIntentBuilder = new AuthIntentBuilder(this,getString(R.string.apple_dev_token));
        Intent intent = authIntentBuilder
                .setHideStartScreen(false)
                .setStartScreenMessage("Authorize to start transfering playlists!")
                .build();
        startActivityForResult(intent,REQUESTCODE_APPLEMUSIC_AUTH);
    }

    // on activity result, mainly checking from apple music redirect
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult: " + requestCode + ", data = " + data);

        if (requestCode == REQUESTCODE_APPLEMUSIC_AUTH) {
            TokenResult tokenResult = authenticationManager.handleTokenResult(data);
            if (!tokenResult.isError()) {
                String musicUserToken = tokenResult.getMusicUserToken();
                Log.v(TAG,"Apple Music User token = " + musicUserToken);
                dh.writeStringData(dh.APPLE_MUSIC,musicUserToken);
                setStatus();
            } else {
                TokenError error = tokenResult.getError();
                Log.e(TAG, "Apple music error" + error);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
