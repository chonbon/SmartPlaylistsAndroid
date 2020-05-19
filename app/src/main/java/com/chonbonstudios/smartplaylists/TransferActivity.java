package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.chonbonstudios.smartplaylists.Adapters.ListOfServicesAdapter;
import com.chonbonstudios.smartplaylists.ModelData.StreamingServices;

import java.util.ArrayList;

public class TransferActivity extends AppCompatActivity {

    private RecyclerView listOfServices;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<StreamingServices> servicesList;

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

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        listOfServices.addItemDecoration(itemDecoration);

        //fill out arraylist
        createServiceList();

        // specify an adapter
        mAdapter = new ListOfServicesAdapter(servicesList);
        listOfServices.setAdapter(mAdapter);
    }



    public void createServiceList(){
        servicesList = new ArrayList<>();
        StreamingServices temp = new StreamingServices("Spotify", true);
        servicesList.add(temp);
        temp = new StreamingServices("Apple Music", false);
        servicesList.add(temp);
    }
}
