package com.music.sonart.view.Artist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.music.sonart.R;
import com.music.sonart.view.Song.MySongActivity;

public class ArtistActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private CardView cardEditProfile, cardMySong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        // Inicializar vistas
        btnBack = findViewById(R.id.btnBack);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        cardMySong = findViewById(R.id.cardMySong);


        // Configurar botón de retroceso
        btnBack.setOnClickListener(v -> finish());

        // Ir a editar perfil de artista
        cardEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileArtistActivity.class);
            startActivity(intent);
        });

        // Ir a subir canciones
        cardMySong.setOnClickListener(v -> {
            Intent intent = new Intent(this, MySongActivity.class);
            startActivity(intent);
        });


    }


}