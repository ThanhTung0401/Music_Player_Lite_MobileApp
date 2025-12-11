package com.example.music_player_lite_mobileapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songList;
    private int position = 0;
    private final IBinder mBinder = new LocalBinder();

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREV = "ACTION_PREV";

    public class LocalBinder extends Binder {
        MusicService getService() { return MusicService.this; }
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getStringExtra("action_music_service");
            if (action != null) handleAction(action);
        }
        return START_NOT_STICKY;
    }

    public void setSongList(ArrayList<Song> songs) { this.songList = songs; }

    public void playSong(int pos) {
        if (songList == null || songList.isEmpty()) return;
        position = pos;
        Song song = songList.get(position);
        if (mediaPlayer != null) { mediaPlayer.stop(); mediaPlayer.release(); }

        mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(song.getPath()));
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> nextSong());
        sendNotification(song);
        sendUpdateToActivity();
    }

    public void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            sendNotification(songList.get(position));
            sendUpdateToActivity();
        }
    }

    public void resumeSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            sendNotification(songList.get(position));
            sendUpdateToActivity();
        }
    }

    public void nextSong() {
        position++;
        if (position >= songList.size()) position = 0;
        playSong(position);
    }

    public void prevSong() {
        position--;
        if (position < 0) position = songList.size() - 1;
        playSong(position);
    }

    private void handleAction(String action) {
        switch (action) {
            case ACTION_PLAY: resumeSong(); break;
            case ACTION_PAUSE: pauseSong(); break;
            case ACTION_NEXT: nextSong(); break;
            case ACTION_PREV: prevSong(); break;
        }
    }

    private void sendNotification(Song song) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        boolean isPlaying = mediaPlayer != null && mediaPlayer.isPlaying();
        int playPauseIcon = isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        String playPauseAction = isPlaying ? ACTION_PAUSE : ACTION_PLAY;

        Notification notification = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist())
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_media_previous, "Prev", getPendingIntent(ACTION_PREV))
                .addAction(playPauseIcon, "Play", getPendingIntent(playPauseAction))
                .addAction(android.R.drawable.ic_media_next, "Next", getPendingIntent(ACTION_NEXT))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
                .build();

        startForeground(1, notification);
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void sendUpdateToActivity() {
        sendBroadcast(new Intent("UPDATE_UI"));
    }

    public boolean isPlaying() { return mediaPlayer != null && mediaPlayer.isPlaying(); }
    public Song getCurrentSong() { return (songList != null) ? songList.get(position) : null; }
    public int getCurrentPosition() { return (mediaPlayer != null) ? mediaPlayer.getCurrentPosition() : 0; }
    public int getDuration() { return (mediaPlayer != null) ? mediaPlayer.getDuration() : 0; }
    public void seekTo(int pos) { if (mediaPlayer != null) mediaPlayer.seekTo(pos); }
}