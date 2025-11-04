package com.music.sonart.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.music.sonart.R;
import com.music.sonart.model.Song;

import com.music.sonart.model.player.PlayerManager;
import com.music.sonart.service.MusicService;
import com.music.sonart.view.Song.PlaySongActivity;

public class MiniPlayerFragment extends Fragment {

    private View root;

    // SOLUCIÓN: BroadcastReceiver CLÁSICO (NO lambda)
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            actualizar();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_mini_player, container, false);

        // ABRIR PANTALLA COMPLETA
        root.setOnClickListener(v -> {
            Song s = PlayerManager.getInstance().getCurrentSong();
            if (s != null) {
                startActivity(new Intent(requireContext(), PlaySongActivity.class).putExtra("song", s));
            }
        });

        // PLAY/PAUSE
        root.findViewById(R.id.ivMiniPlayPause).setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), MusicService.class);
            i.setAction(PlayerManager.getInstance().isPlaying()
                    ? MusicService.ACTION_PAUSE
                    : MusicService.ACTION_PLAY);
            requireContext().startService(i);
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MusicService.ACTION_STATE_CHANGED);
        requireContext().registerReceiver(receiver, filter);
        actualizar(); // Forzamos que aparezca
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(receiver);
    }

    private void actualizar() {
        Song cancion = PlayerManager.getInstance().getCurrentSong();
        if (cancion == null) {
            root.setVisibility(View.GONE);
            return;
        }

        root.setVisibility(View.VISIBLE);

        TextView titulo = root.findViewById(R.id.tvMiniTitle);
        TextView artista = root.findViewById(R.id.tvMiniArtist);
        ImageView cover = root.findViewById(R.id.ivMiniCover);
        ImageView playPause = root.findViewById(R.id.ivMiniPlayPause);

        titulo.setText(cancion.getTitle());
        artista.setText(cancion.getArtistName());
        Glide.with(this).load(cancion.getCoverUrl()).into(cover);
        playPause.setImageResource(PlayerManager.getInstance().isPlaying()
                ? android.R.drawable.ic_media_pause
                : android.R.drawable.ic_media_play);
    }
}