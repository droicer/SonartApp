package com.music.sonart.view.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.music.sonart.R;
import com.music.sonart.view.User.MenuActivity; // Asegúrate de importar esta clase

public class MainActivity extends AppCompatActivity {

    private ImageView ivLogo;
    private TextView tvAppName;
    private TextView tvAppSubtitle;
    private MaterialButton btnLogin;
    private MaterialButton btnRegister;
    private FirebaseAuth auth; // 🔸 Añadimos FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializamos Firebase Auth
        auth = FirebaseAuth.getInstance();

        // 🔹 Verificar sesión antes de mostrar animaciones o botones
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Si el usuario ya está autenticado, ir directo a MenuActivity
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            startActivity(intent);
            finish(); // Evita volver atrás a esta pantalla
            return;
        }

        // Ajustar padding para system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas y configurar UI
        initViews();
        setupAnimations();
        setupButtonListeners();
    }

    private void initViews() {
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvAppSubtitle = findViewById(R.id.tvAppSubtitle);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(800);
        ivLogo.startAnimation(fadeIn);

        tvAppName.setAlpha(0f);
        tvAppName.animate().alpha(1f).setDuration(1000).setStartDelay(300).start();

        tvAppSubtitle.setAlpha(0f);
        tvAppSubtitle.animate().alpha(1f).setDuration(1000).setStartDelay(500).start();

        btnLogin.setAlpha(0f);
        btnLogin.setTranslationY(50f);
        btnLogin.animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(700).start();

        btnRegister.setAlpha(0f);
        btnRegister.setTranslationY(50f);
        btnRegister.animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(900).start();
    }

    private void setupButtonListeners() {
        btnLogin.setOnClickListener(v -> navigateToLogin());
        btnRegister.setOnClickListener(v -> navigateToRegister());
    }

    private void navigateToLogin() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToRegister() {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
