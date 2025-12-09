package com.example.music_player_lite_mobileapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            Intent serviceIntent = new Intent(context, MusicService.class);
            serviceIntent.putExtra("action_music_service", action);
            context.startService(serviceIntent);
        }
    }
}