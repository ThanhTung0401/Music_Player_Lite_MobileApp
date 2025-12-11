package com.example.music_player_lite_mobileapp;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<Song> songList = new ArrayList<>();
    MusicService musicService;
    boolean isServiceBound = false;
    private Handler handler = new Handler();
    private Runnable updateTimeRunnable;


    TextView txtSongInfo,txtSongDuration,txtSongCurrentTime;
    ImageButton btnPlay, btnNext, btnPrev;
    SeekBar seekBar;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
            musicService.setSongList(songList);
        }
        @Override public void onServiceDisconnected(ComponentName name) { isServiceBound = false; }
    };

    private BroadcastReceiver uiReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) { updateMiniPlayer(); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        txtSongInfo = findViewById(R.id.txtSongInfo);
        txtSongDuration = findViewById(R.id.txtTotalTime);

        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        seekBar = findViewById(R.id.seekBar);

        btnPlay.setOnClickListener(v -> {
            if (isServiceBound) {
                if (musicService.isPlaying())
                {
                    musicService.pauseSong();
                    btnPlay.setImageResource(R.drawable.ic_play);
                }
                else
                {
                    musicService.resumeSong();
                    btnPlay.setImageResource(R.drawable.ic_pause);
                }
            }
        });
        btnNext.setOnClickListener(v -> { if (isServiceBound) musicService.nextSong(); });
        btnPrev.setOnClickListener(v -> { if (isServiceBound) musicService.prevSong(); });

        // Kéo thanh SeekBar để tua nhạc (Yêu cầu A3)
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isServiceBound) musicService.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        checkPermission();
        registerReceiver(uiReceiver, new IntentFilter("UPDATE_UI"), Context.RECEIVER_NOT_EXPORTED);

        // Logic cập nhật SeekBar mỗi giây (Yêu cầu A3)
        handler.post(new Runnable() {
            @Override public void run() {
                if (isServiceBound && musicService.isPlaying()) {
                    seekBar.setMax(musicService.getDuration());
                    seekBar.setProgress(musicService.getCurrentPosition());
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void checkPermission() {
        String permission = Build.VERSION.SDK_INT >= 33 ? Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission, Manifest.permission.POST_NOTIFICATIONS}, 123);
        } else {
            loadSongs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) loadSongs();
    }

    private void loadSongs() {
        // Quét nhạc (Yêu cầu A1)
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int pathCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int totalTimeCol= cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            do {
                String title = cursor.getString(titleCol);
                String artist = cursor.getString(artistCol);
                String path = cursor.getString(pathCol);
                Long totalTime= cursor.getLong(totalTimeCol);
                songList.add(new Song(title, artist, path, totalTime));
            } while (cursor.moveToNext());
            cursor.close();
        }

        SongAdapter adapter = new SongAdapter(this, songList);
        recyclerView.setAdapter(adapter);

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        startService(intent);

        adapter.setOnItemClickListener(position -> {
            if (isServiceBound)
            {
                musicService.playSong(position);

                TextView songName=findViewById(R.id.txtSongInfo);
                songName.setText(songList.get(position).getTitle());

                TextView songDuration=findViewById(R.id.txtTotalTime);
                songDuration.setText(songList.get(position).getDuration());

                TextView songCurrentTime=findViewById(R.id.txtCurrentTime);

                ImageView playButton=findViewById(R.id.btnPlay);
                playButton.setImageResource(R.drawable.ic_pause);

                updateTimeRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null && musicService.isPlaying()) {
                            int currentPos = musicService.getCurrentPosition();
                            String currentTime = songList.get(position).getCurrentTime(currentPos);
                            songCurrentTime.setText(currentTime);
                        }

                        handler.postDelayed(this, 1000); // 0.5s cập nhật 1 lần
                    }
                };

                handler.post(updateTimeRunnable);
            }
        });
    }

    private void updateMiniPlayer() {
        if (isServiceBound && musicService.getCurrentSong() != null) {
            txtSongInfo.setText(musicService.getCurrentSong().getTitle());
            if (musicService.isPlaying()) {
                btnPlay.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) { unbindService(serviceConnection); isServiceBound = false; }
        unregisterReceiver(uiReceiver);
    }
}