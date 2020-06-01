package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.chonbonstudios.smartplaylists.ModelData.DataHandler;

public class ResultActivity extends AppCompatActivity {

    private DataHandler dh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
    }
}