package com.chonbonstudios.smartplaylists.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;


public class TransferService extends IntentService {

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    public TransferService() {
        super("TransferService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final Handler handler = intent.getParcelableExtra("handler");

        // TODO Background Task

        Message msg = new Message();
        msg.obj = "Sending message to UI after completion of background task!";
        msg.what = STATUS_FINISHED;
        handler.sendMessage(msg);

    }

}
