package com.music.sonart.view.Login;

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
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.music.sonart.R;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;
import com.music.sonart.model.User.UserRequest;
import com.music.sonart.model.User.UserResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // Firebase
    private FirebaseAuth mAuth;

    // UI Components
    private ImageButton btnBack;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

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
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    /**
     * Configura los listeners para todos los elementos interactivos
     */
    private void setupListeners() {
        // Botón volver
        btnBack.setOnClickListener(v -> finish());

        // Botón crear cuenta
        btnRegister.setOnClickListener(v -> handleRegister());

        // Texto iniciar sesión
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    /**
     * Maneja el registro de usuario
     */
    private void handleRegister() {
        // Obtener valores de los campos
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Resetear errores
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Validar campos
        if (!validateInputs(email, password, confirmPassword)) {
            return;
        }

        // Deshabilitar botón mientras se procesa
        btnRegister.setEnabled(false);
        btnRegister.setText("Creando cuenta...");

        // Crear usuario en Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Crear cuenta");

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            sendVerificationEmail(user);
                        }
                    } else {
                        handleRegisterError(task.getException());
                    }
                });
    }

    /**
     * Valida los campos de entrada
     */
    private boolean validateInputs(String email, String password, String confirmPassword) {
        boolean isValid = true;

        // Validar email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("El correo electrónico es requerido");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Ingresa un correo electrónico válido");
            isValid = false;
        }

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("La contraseña es requerida");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("La contraseña debe tener al menos 6 caracteres");
            isValid = false;
        }

        // Validar confirmación de contraseña
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Confirma tu contraseña");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Las contraseñas no coinciden");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Envía el correo de verificación al usuario y sincroniza con la API
     */
    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(verifyTask -> {
                    if (verifyTask.isSuccessful()) {
                        // Preparar datos para enviar a la API
                        UserRequest userRequest = new UserRequest(
                                user.getUid(),
                                user.getEmail(),
                                user.getDisplayName(),
                                "listener" // Rol por defecto
                        );

                        // Obtener el token de Firebase
                        user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful()) {
                                String idToken = tokenTask.getResult().getToken();
                                syncUserWithApi(userRequest, idToken);
                            } else {
                                showErrorDialog("Error", "No se pudo obtener el token de autenticación");
                            }
                        });

                        showSuccessDialog(user.getEmail());
                    } else {
                        showErrorDialog(
                                "Error al enviar correo",
                                "No se pudo enviar el correo de verificación. Intenta más tarde."
                        );
                        Log.e(TAG, "Error sending verification email", verifyTask.getException());
                    }
                });
    }

    /**
     * Sincroniza el usuario con la API de Laravel
     */
    private void syncUserWithApi(UserRequest userRequest, String idToken) {
        ApiService apiService = ApiClient.getApiService(idToken);
        Call<UserResponse> call = apiService.syncUser(userRequest);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Usuario sincronizado con éxito");
                } else {
                    Log.e(TAG, "Error en sync: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "Error en sync", t);
            }
        });
    }

    /**
     * Maneja los errores de registro
     */
    private void handleRegisterError(Exception e) {
        if (e instanceof FirebaseAuthException) {
            FirebaseAuthException fae = (FirebaseAuthException) e;
            switch (fae.getErrorCode()) {
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    tilEmail.setError("Este correo ya está registrado");
                    showErrorDialog(
                            "Correo en uso",
                            "El correo " + etEmail.getText().toString() +
                                    " ya está registrado.\n\nPor favor, usa otro correo o inicia sesión."
                    );
                    break;
                case "ERROR_INVALID_EMAIL":
                    tilEmail.setError("Correo electrónico inválido");
                    break;
                case "ERROR_WEAK_PASSWORD":
                    tilPassword.setError("Contraseña muy débil");
                    showErrorDialog(
                            "Contraseña débil",
                            "La contraseña debe tener al menos 6 caracteres."
                    );
                    break;
                default:
                    showErrorDialog(
                            "Error en el registro",
                            "Ocurrió un error: " + fae.getMessage()
                    );
                    break;
            }
        } else {
            showErrorDialog(
                    "Error inesperado",
                    "Ocurrió un error inesperado: " + e.getMessage()
            );
        }
        Log.e(TAG, "Registration failed", e);
    }

    /**
     * Muestra el diálogo de registro exitoso
     */
    private void showSuccessDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Verifica tu correo")
                .setMessage("Te hemos enviado un correo de verificación a:\n\n" +
                        email + "\n\n" +
                        "Por favor revisa tu bandeja de entrada antes de iniciar sesión.")
                .setCancelable(false)
                .setPositiveButton("Entendido", (dialog, which) -> {
                    dialog.dismiss();
                    finish(); // Volver al login
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
        finish(); // Simplemente cierra esta actividad y vuelve al Login
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}