package com.example.musicplayer;

import static com.example.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.example.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.example.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musicplayer.ApplicationClass.ACTION_SEEK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context, PlayerService.class);
        if(actionName != null){
            switch(actionName) {
                case ACTION_PLAY -> {
                    serviceIntent.putExtra("ActionName", "playPause");
                    context.startService(serviceIntent);
                }
                case ACTION_NEXT -> {
                    serviceIntent.putExtra("ActionName", "next");
                    context.startService(serviceIntent);
                }
                case ACTION_PREVIOUS -> {
                    serviceIntent.putExtra("ActionName", "previous");
                    context.startService(serviceIntent);
                }
            }
        }
    }
}
