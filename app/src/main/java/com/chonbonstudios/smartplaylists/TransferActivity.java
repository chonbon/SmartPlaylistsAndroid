package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.chonbonstudios.smartplaylists.listModels.ListOfServicesAdapter;

public class TransferActivity extends AppCompatActivity {

    private RecyclerView listOfServices;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        listOfServices = findViewById(R.id.listStreamingServices);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        listOfServices.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        listOfServices.setLayoutManager(layoutManager);

        // specify an adapter
        //mAdapter = new ListOfServicesAdapter(myDataSet);
        //listOfServices.setAdapter(mAdapter);
    }
}
