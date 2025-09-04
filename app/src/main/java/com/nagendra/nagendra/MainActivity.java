package com.nagendra.nagendra;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    TextInputEditText emailInput, passwordInput;
    SignInButton googleSignInBtn;
    TextView signupText, forgotPasswordText;
    com.google.android.material.button.MaterialButton loginButton;

    ProgressBar progressBar;

    private static final String TAG = "MainActivity";

    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;

    ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int savedUserId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("user_id", -1);
        if (savedUserId != -1) {
            startActivity(new Intent(MainActivity.this, MenuActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        googleSignInBtn = findViewById(R.id.googleSignInBtn);
        signupText = findViewById(R.id.signupText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("356064184484-icis2ugknin1freqclduv9b8n0bjtkee.apps.googleusercontent.com") // Replace with your ID
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Log.e(TAG, "Google Sign-In failed: " + e.getStatusCode(), e);
                            Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        googleSignInBtn.setSize(SignInButton.SIZE_WIDE);
        googleSignInBtn.setColorScheme(SignInButton.COLOR_DARK);

        googleSignInBtn.setOnClickListener(v -> {
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                FirebaseAuth.getInstance().signOut();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });

        loginButton.setOnClickListener(this::onClick);

        signupText.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignUpActivity.class)));
        forgotPasswordText.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class)));
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void onClick(View v) {
        String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordInput.getText()).toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        showLoading(true);

        String url = "http://54.87.73.6/login_user.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    showLoading(false);
                    Log.d(TAG, "🔥 Login API Raw Response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            int userId = jsonResponse.getInt("user_id");
                            getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putInt("user_id", userId)
                                    .apply();

                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MenuActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        showLoading(false);
                        Log.e(TAG, "❌ JSON parsing error (Login): " + response, e);
                        Toast.makeText(this, "Login response error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, "Login Server Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showLoading(true);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String username = user.getDisplayName();
                            String email = user.getEmail();
                            String uid = user.getUid();
                            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

                            sendGoogleUserToServer(username, email, uid, photoUrl);
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Firebase Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendGoogleUserToServer(String username, String email, String uid, String photoUrl) {
        String url = "http://54.87.73.6/google_signup.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    showLoading(false);
                    Log.d("GOOGLE_RESPONSE", "🔥 Raw response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        Log.d("GOOGLE_RESPONSE", "✅ Parsed: " + jsonResponse.toString());

                        if (jsonResponse.getString("status").equals("success")) {
                            int userId = jsonResponse.getInt("user_id");

                            getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putInt("user_id", userId)
                                    .apply();

                            Toast.makeText(this, "Google Sign-In Success", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MenuActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        showLoading(false);
                        Log.e("GOOGLE_RESPONSE", "❌ JSON parse error: " + response, e);
                        Toast.makeText(this, "Google response error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    showLoading(false);
                    Log.e("GOOGLE_RESPONSE", "❌ Volley error", error);
                    Toast.makeText(this, "Server error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
                params.put("firebase_uid", uid);
                params.put("photo_url", photoUrl);
                params.put("login_type", "google");
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
