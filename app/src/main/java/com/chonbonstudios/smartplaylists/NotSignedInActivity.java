package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import com.apple.android.sdk.authentication.AuthIntentBuilder;
import com.apple.android.sdk.authentication.AuthenticationFactory;
import com.apple.android.sdk.authentication.AuthenticationManager;
import com.apple.android.sdk.authentication.TokenError;
import com.apple.android.sdk.authentication.TokenResult;
import com.chonbonstudios.smartplaylists.ModelData.DataHandler;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// This activity is for initial launch or the user is signed out of all streaming services,
// this will handle logging in to a single service to allow user to then initiate transfer of playlists
public class NotSignedInActivity extends AppCompatActivity {
    public static final String TAG = NotSignedInActivity.class.getSimpleName();
    private static final int REQUESTCODE_APPLEMUSIC_AUTH = 3456;

    private AuthenticationManager authenticationManager;
    private DataHandler dh;
    OkHttpClient client;
    WebView mWebView;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_signed_in);
        // init DataHandler
        dh = new DataHandler(this);

        //init views and http client
        mWebView = findViewById(R.id.webView);
        client = new OkHttpClient();

        Uri uri = getIntent().getData();
        Log.v(TAG, ""+ uri);

        if(uri != null)parseToken(uri);

        // apple music sdk auth manager init
        if (authenticationManager == null) {
            authenticationManager = AuthenticationFactory.createAuthenticationManager(this);
        }

    }

    // User clicks to log in to spotify
    public void loginSpotify(View view) {
        //Toast.makeText(this, "Clicked Spotify Login", Toast.LENGTH_SHORT).show();
        //startActivity(new Intent(NotSignedInActivity.this, MainActivity.class));
        //apiLoginSpotify();
        mWebView.loadUrl(getString(R.string.api_spotify_login));
        //finish();
    }

    // User clicks to log in to Apple Music
    public void loginAppleMusic(View view) {
        //Toast.makeText(this, "Clicked Apple Music Login", Toast.LENGTH_SHORT).show();
        apiLoginApple();
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
                        startActivity(new Intent(NotSignedInActivity.this, MainActivity.class));
                        finish();
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
                startActivity(new Intent(NotSignedInActivity.this,
                        MainActivity.class).putExtra("AppleMusicToken", musicUserToken));
                finish();
            } else {
                TokenError error = tokenResult.getError();
                Log.e(TAG, "Apple music error" + error);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
