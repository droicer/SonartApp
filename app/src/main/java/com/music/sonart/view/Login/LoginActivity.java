package com.music.sonart.view.Login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.music.sonart.R;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;
import com.music.sonart.model.User.UserRequest;
import com.music.sonart.model.User.UserResponse;
import com.music.sonart.view.User.MenuActivity;
import com.music.sonart.view.RecoverPasswordActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleLauncher;

    // UI Components
    private ImageButton btnBack;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextView tvForgotPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnGoogle;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

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

        // Configurar Google Sign-In
        setupGoogleSignIn();

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
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvRegister = findViewById(R.id.tvRegister);
    }

    /**
     * Configura Google Sign-In
     */
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Lanzador de Google Sign-In
        googleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null) {
                        try {
                            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                    .getResult(ApiException.class);
                            if (account != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            Toast.makeText(this, "Error al iniciar con Google: " + e.getStatusCode(),
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Google Sign-In failed", e);
                        }
                    } else {
                        Toast.makeText(this, "Error: no se recibió información de Google Sign-In",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Configura los listeners para todos los elementos interactivos
     */
    private void setupListeners() {
        // Botón volver
        btnBack.setOnClickListener(v -> finish());

        // Botón iniciar sesión
        btnLogin.setOnClickListener(v -> handleLogin());

        // Botón Google
        btnGoogle.setOnClickListener(v -> handleGoogleLogin());

        // Texto olvidaste tu contraseña
        tvForgotPassword.setOnClickListener(v -> navigateToRecoverPassword());

        // Texto registrarse
        tvRegister.setOnClickListener(v -> navigateToRegister());
    }

    /**
     * Maneja el inicio de sesión con email y contraseña
     */
    private void handleLogin() {
        // Obtener valores de los campos
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Resetear errores
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validar campos
        if (!validateInputs(email, password)) {
            return;
        }

        // Deshabilitar botón mientras se procesa
        btnLogin.setEnabled(false);
        btnLogin.setText("Iniciando...");

        // Autenticar con Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Iniciar sesión");

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            navigateToHome();
                        } else {
                            Toast.makeText(this, "Verifica tu correo antes de iniciar sesión.",
                                    Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    /**
     * Valida los campos de entrada
     */
    private boolean validateInputs(String email, String password) {
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

        return isValid;
    }

    /**
     * Maneja los errores de inicio de sesión
     */
    private void handleLoginError(Exception e) {
        if (e instanceof FirebaseAuthException) {
            FirebaseAuthException fae = (FirebaseAuthException) e;
            switch (fae.getErrorCode()) {
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_USER_NOT_FOUND":
                    tilPassword.setError("Usuario o contraseña incorrecta");
                    Toast.makeText(this, "Usuario o contraseña incorrecta", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_INVALID_EMAIL":
                    tilEmail.setError("Correo electrónico inválido");
                    break;
                case "ERROR_USER_DISABLED":
                    Toast.makeText(this, "Esta cuenta ha sido deshabilitada", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_TOO_MANY_REQUESTS":
                    Toast.makeText(this, "Demasiados intentos. Intenta más tarde", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(this, "Error: " + fae.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            Toast.makeText(this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, "Login failed", e);
    }

    /**
     * Maneja el inicio de sesión con Google
     */
    private void handleGoogleLogin() {
        // Cerrar sesión anterior de Google para permitir selección de cuenta
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });
    }

    /**
     * Autenticación Firebase con Google
     */
    private void firebaseAuthWithGoogle(String idToken) {
        btnGoogle.setEnabled(false);
        btnGoogle.setText("Conectando...");

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    btnGoogle.setEnabled(true);
                    btnGoogle.setText("Iniciar con Google");

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Inicio de sesión con Google exitoso",
                                Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            Toast.makeText(this, "Error de autenticación con Google: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Firebase Auth with Google failed", e);
                        }
                    }
                });
    }

    /**
     * Navega a la pantalla de recuperación de contraseña
     */
    private void navigateToRecoverPassword() {
        Intent intent = new Intent(LoginActivity.this, RecoverPasswordActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Navega a la pantalla de registro
     */
    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Navega a la pantalla principal después del login exitoso y sincroniza con la API
     */
    private void navigateToHome() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Preparar datos para enviar a la API
            UserRequest userRequest = new UserRequest(
                    user.getUid(),
                    user.getEmail(),
                    user.getDisplayName(),
                    "listener" // Rol por defecto
            );

            // Obtener el token de Firebase para autenticar la solicitud
            user.getIdToken(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                    syncUserWithApi(userRequest, idToken);
                } else {
                    Toast.makeText(this, "Error al obtener token", Toast.LENGTH_SHORT).show();
                    proceedToMainActivity();
                }
            });
        } else {
            proceedToMainActivity();
        }
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
                    Toast.makeText(LoginActivity.this, "Usuario sincronizado con éxito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Error al sincronizar usuario", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error en sync: " + response.code());
                }
                proceedToMainActivity();
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error en sync", t);
                proceedToMainActivity();
            }
        });
    }

    /**
     * Navega a MainActivity
     */
    private void proceedToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si el usuario ya está logueado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // Usuario ya autenticado, ir directamente a MainActivity
            navigateToHome();
        }
    }
}