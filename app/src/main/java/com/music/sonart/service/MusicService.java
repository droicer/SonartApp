// MusicService.java → COPIA TODO
package com.music.sonart.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.music.sonart.R;
import com.music.sonart.model.Song;
import com.music.sonart.model.player.PlayerManager;
import com.music.sonart.view.Song.PlaySongActivity;

import java.io.IOException;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "SonartMusicChannel";

    // ACCIONES
    public static final String ACTION_PLAY = "com.music.sonart.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.music.sonart.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.music.sonart.ACTION_STOP";
    public static final String ACTION_SEEK = "com.music.sonart.ACTION_SEEK";
    public static final String ACTION_SEEK_OFFSET = "com.music.sonart.ACTION_SEEK_OFFSET";
    public static final String ACTION_STATE_CHANGED = "com.music.sonart.ACTION_STATE_CHANGED";
    public static final String ACTION_REQUEST_UPDATE = "com.music.sonart.ACTION_REQUEST_UPDATE";
    public static final String ACTION_NEXT_SONG = "com.music.sonart.ACTION_NEXT_SONG";
    public static final String ACTION_FORCE_NEW_SONG = "FORCE_NEW_SONG";

    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;

        String action = intent.getAction();
        Song current = PlayerManager.getInstance().getCurrentSong();

        if (ACTION_PLAY.equals(action)) {
            if (mediaPlayer != null && !mediaPlayer.isPlaying() && current != null) {
                mediaPlayer.start();
                PlayerManager.getInstance().setPlaying(true);
                sendStateBroadcast();
            }
        } else if (ACTION_PAUSE.equals(action)) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                PlayerManager.getInstance().setPlaying(false);
                sendStateBroadcast();
            }
        } else if (ACTION_STOP.equals(action)) {
            stopSelf();
        } else if (ACTION_SEEK.equals(action)) {
            int progress = intent.getIntExtra("progress", 0);
            if (mediaPlayer != null) {
                int pos = progress * mediaPlayer.getDuration() / 100;
                mediaPlayer.seekTo(pos);
                sendStateBroadcast();
            }
        } else if (ACTION_SEEK_OFFSET.equals(action)) {
            int offset = intent.getIntExtra("offset", 0);
            if (mediaPlayer != null) {
                int pos = mediaPlayer.getCurrentPosition() + offset;
                pos = Math.max(0, Math.min(pos, mediaPlayer.getDuration()));
                mediaPlayer.seekTo(pos);
                sendStateBroadcast();
            }
        } else if (ACTION_REQUEST_UPDATE.equals(action)) {
            sendStateBroadcast();
        } else if (ACTION_FORCE_NEW_SONG.equals(action)) {
            Song song = (Song) intent.getSerializableExtra("song");
            if (song != null && !song.getId().equals(PlayerManager.getInstance().getCurrentSong().getId())) {
                playSong(song);
                showNotification(song);
            }
        } else {
            Song song = (Song) intent.getSerializableExtra("song");
            if (song != null) {
                playSong(song);
                showNotification(song);
            }
        }
        return START_STICKY;
    }

    private void playSong(Song song) {
        try {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getFileUrl());
            mediaPlayer.prepare();
            mediaPlayer.start();

            PlayerManager.getInstance().setCurrentSong(song);
            PlayerManager.getInstance().setPlaying(true);
            sendStateBroadcast();

            mediaPlayer.setOnCompletionListener(mp -> {
                sendBroadcast(new Intent(ACTION_NEXT_SONG));
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // NOTIFICACIÓN CON BOTONES COMO SPOTIFY
    private void showNotification(Song song) {
        createNotificationChannel();

        // BOTONES
        Intent prevIntent = new Intent(this, MusicService.class)
                .setAction(ACTION_SEEK_OFFSET).putExtra("offset", -10000);
        Intent playIntent = new Intent(this, MusicService.class).setAction(ACTION_PLAY);
        Intent pauseIntent = new Intent(this, MusicService.class).setAction(ACTION_PAUSE);
        Intent nextIntent = new Intent(this, MusicService.class).setAction(ACTION_NEXT_SONG);

        PendingIntent pPrev = PendingIntent.getService(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pPlay = PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pPause = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pNext = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent openApp = new Intent(this, PlaySongActivity.class).putExtra("song", song);
        PendingIntent pOpen = PendingIntent.getActivity(this, 0, openApp, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtistName())
                .setSmallIcon(R.drawable.ic_headphones)
                .setContentIntent(pOpen)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Anterior", pPrev)
                .addAction(PlayerManager.getInstance().isPlaying()
                                ? android.R.drawable.ic_media_pause
                                : android.R.drawable.ic_media_play,
                        "Play", PlayerManager.getInstance().isPlaying() ? pPause : pPlay)
                .addAction(android.R.drawable.ic_media_next, "Siguiente", pNext)
                .setStyle(new MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))
                .build();

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Sonart Music",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void sendStateBroadcast() {
        Intent broadcast = new Intent(ACTION_STATE_CHANGED);
        broadcast.putExtra("isPlaying", PlayerManager.getInstance().isPlaying());
        broadcast.putExtra("currentPosition", mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0);
        broadcast.putExtra("duration", mediaPlayer != null ? mediaPlayer.getDuration() : 0);
        broadcast.putExtra("currentSong", PlayerManager.getInstance().getCurrentSong());
        sendBroadcast(broadcast);

        // ACTUALIZA NOTIFICACIÓN CADA SEGUNDO
        Song song = PlayerManager.getInstance().getCurrentSong();
        if (song != null) showNotification(song);
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        PlayerManager.getInstance().clear();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}