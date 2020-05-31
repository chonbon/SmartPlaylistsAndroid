package com.chonbonstudios.smartplaylists.ModelData;


import android.os.Handler;
import android.os.Message;

public class MessageHandler extends Handler {

    private AppReceiver appReceiver;

    public MessageHandler(AppReceiver receiver){
        appReceiver = receiver;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        appReceiver.onReceiveResult(msg);
    }

    public interface AppReceiver {
        void onReceiveResult(Message message);
    }
}
