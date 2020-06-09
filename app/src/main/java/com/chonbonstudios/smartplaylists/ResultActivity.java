package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chonbonstudios.smartplaylists.ModelData.DataHandler;
import com.chonbonstudios.smartplaylists.ModelData.MessageHandler;
import com.chonbonstudios.smartplaylists.Services.TransferService;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultActivity extends AppCompatActivity implements MessageHandler.AppReceiver {
    private static final String TAG = ResultActivity.class.getSimpleName();

    private DataHandler dh;
    private MessageHandler mh;
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    private int songsTotal = 0;
    private int songsMatched = 0;
    private int songsNotMatched = 0;
    private double songsMatchRate = 0.00;

    private TextView totalPlaylists, totalSongs, txtSongsMatched, txtSongsNotMatched, txtMatchRate;
    private TextView txtPlaylistsCreated;

    private ProgressBar stageOne, stageTwo, stageThree;
    private ImageView stageOneCheck, stageTwoCheck, stageThreeCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        totalPlaylists = findViewById(R.id.txtFetchingPlaylistTotal);
        totalSongs = findViewById(R.id.txtTotalSongs);
        txtSongsMatched = findViewById(R.id.txtSongsMatched);
        txtSongsNotMatched = findViewById(R.id.txtUnmatchedSongs);
        txtMatchRate = findViewById(R.id.txtMatchRate);
        txtPlaylistsCreated = findViewById(R.id.txtPlaylistsCreated);

        stageOne = findViewById(R.id.progressStageOne);
        stageTwo = findViewById(R.id.progressStageTwo);
        stageThree = findViewById(R.id.progressStageThree);

        stageOneCheck = findViewById(R.id.checkStageOne);
        stageTwoCheck = findViewById(R.id.checkStageTwo);
        stageThreeCheck = findViewById(R.id.checkStageThree);

        dh = new DataHandler(this);

        totalPlaylists.setText(dh.getPlaylistToTransfer().size() + " Playlists");

        registerService();
    }

    private void registerService(){
        Intent intent = new Intent(getApplicationContext(), TransferService.class);

        mh = new MessageHandler(this);
        intent.putExtra("handler", new Messenger(mh));
        startService(intent);
    }

    @Override
    public void onReceiveResult(Message message) {
        switch(message.what){
            case STATUS_RUNNING: updateProgress(message); break;
            case STATUS_FINISHED: stageThree.setVisibility(View.INVISIBLE); stageThreeCheck.setVisibility(View.VISIBLE);//todo
                break;
            case STATUS_ERROR:
                Log.e(TAG, "Service Error:" + message.obj.toString());
                break;
            default: break;
        }
    }

    private void updateProgress(Message message){
        Log.v(TAG, "Service update: " + message.obj.toString());

        try {
            JSONObject msgObject = new JSONObject(message.obj.toString());
            switch(msgObject.getInt("stage")){
                case 1:stageOne.setVisibility(View.VISIBLE); break;
                case 2:stageOne.setVisibility(View.INVISIBLE);stageTwo.setVisibility(View.VISIBLE);stageOneCheck.setVisibility(View.VISIBLE); break;
                case 3:stageTwo.setVisibility(View.INVISIBLE);stageThree.setVisibility(View.VISIBLE);stageTwoCheck.setVisibility(View.VISIBLE);  break;
            }
            songsMatched = msgObject.getInt("songsMatched");
            songsNotMatched = (msgObject.getInt("songsNotMatched"));
            if(songsMatched != 0 || songsNotMatched != 0) {
                songsMatchRate = songsMatched * 100 / (songsMatched + songsNotMatched);
            }

            totalSongs.setText(msgObject.getInt("totalSongs") + " Songs");
            txtSongsMatched.setText(songsMatched+ " Songs Matched");
            txtSongsNotMatched.setText(songsNotMatched + " Songs Not Matched");
            txtMatchRate.setText((int) Math.ceil(songsMatchRate) + "% Match Rate");
            txtPlaylistsCreated.setText(msgObject.getInt("playlistsCreated") + "/" +
                    dh.getPlaylistToTransfer().size() + " Playlists Created");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}