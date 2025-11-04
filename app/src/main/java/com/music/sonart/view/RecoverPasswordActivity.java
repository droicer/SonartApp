package com.music.sonart.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.music.sonart.R;
import com.music.sonart.view.Login.LoginActivity;

public class RecoverPasswordActivity extends AppCompatActivity {

    private static final String TAG = "RecoverPasswordActivity";

    // Firebase
    private FirebaseAuth mAuth;

    // UI Components
    private ImageButton btnBack;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnRecoverPassword;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recover_password);

        // Ajustar padding para system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();
    }

    /**
     * Inicializa todas las vistas del layout
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        btnRecoverPassword = findViewById(R.id.btnRecoverPassword);
        tvLogin = findViewById(R.id.tvLogin);
    }

    /**
     * Configura los listeners para todos los elementos interactivos
     */
    private void setupListeners() {
        // Botón volver
        btnBack.setOnClickListener(v -> finish());

        // Botón recuperar contraseña
        btnRecoverPassword.setOnClickListener(v -> handlePasswordRecovery());

        // Texto iniciar sesión
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    /**
     * Maneja el proceso de recuperación de contraseña
     */
    private void handlePasswordRecovery() {
        // Obtener email
        String email = etEmail.getText().toString().trim();

        // Resetear error
        tilEmail.setError(null);

        // Validar email
        if (!validateEmail(email)) {
            return;
        }

        // Deshabilitar botón mientras se procesa
        btnRecoverPassword.setEnabled(false);
        btnRecoverPassword.setText("Enviando...");

        // Enviar enlace de recuperación
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnRecoverPassword.setEnabled(true);
                    btnRecoverPassword.setText("Enviar enlace de recuperación");

                    if (task.isSuccessful()) {
                        showSuccessDialog(email);
                    } else {
                        handleRecoveryError(task.getException(), email);
                    }
                });
    }

    /**
     * Valida el campo de email
     */
    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("El correo electrónico es requerido");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Ingresa un correo electrónico válido");
            return false;
        }
        return true;
    }

    /**
     * Maneja los errores de recuperación
     */
    private void handleRecoveryError(Exception e, String email) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            tilEmail.setError("Este correo no está registrado");
            showErrorDialog(
                    "Usuario no encontrado",
                    "No existe ninguna cuenta registrada con el correo:\n\n" + email
            );
        } else {
            showErrorDialog(
                    "Error",
                    "No se pudo enviar el correo.\n\nDetalles: " +
                            (e != null ? e.getMessage() : "Error desconocido")
            );
        }
        Log.e(TAG, "Password recovery failed", e);
    }

    /**
     * Muestra el diálogo de éxito
     */
    private void showSuccessDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Correo enviado")
                .setMessage("Te hemos enviado un enlace para restablecer tu contraseña a:\n\n" +
                        email + "\n\n" +
                        "Revisa tu bandeja de entrada o la carpeta de SPAM.")
                .setCancelable(false)
                .setPositiveButton("Entendido", (dialog, which) -> {
                    dialog.dismiss();
                    navigateToLogin();
                })
                .show();
    }

    /**
     * Muestra un diálogo de error
     */
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Navega a la pantalla de login
     */
    private void navigateToLogin() {
        Intent intent = new Intent(RecoverPasswordActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}