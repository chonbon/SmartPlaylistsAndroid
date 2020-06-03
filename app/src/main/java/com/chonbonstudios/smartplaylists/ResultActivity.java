package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.chonbonstudios.smartplaylists.ModelData.DataHandler;
import com.chonbonstudios.smartplaylists.ModelData.MessageHandler;
import com.chonbonstudios.smartplaylists.Services.TransferService;

public class ResultActivity extends AppCompatActivity implements MessageHandler.AppReceiver {
    private static final String TAG = ResultActivity.class.getSimpleName();

    private DataHandler dh;
    private MessageHandler mh;
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

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
            case STATUS_FINISHED: //todo
                break;
            case STATUS_ERROR:
                Log.e(TAG, "Service Error:" + message.obj.toString());
                break;
            default: break;
        }
    }

    private void updateProgress(Message message){
        Log.v(TAG, "Service update: " + message.obj.toString());
    }
}