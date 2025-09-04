package com.nagendra.nagendra;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.HashMap;
import java.util.Map;

public class OTPVerificationActivity extends AppCompatActivity {

    EditText otpInput;
    Button verifyButton;
    String verificationId, username, email, password, phone;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        otpInput = findViewById(R.id.otpInput);
        verifyButton = findViewById(R.id.verifyButton);

        mAuth = FirebaseAuth.getInstance();

        // Get data from intent
        verificationId = getIntent().getStringExtra("verificationId");
        username = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        phone = getIntent().getStringExtra("phone");

        verifyButton.setOnClickListener(v -> {
            String code = otpInput.getText().toString().trim();
            if (code.length() == 6) {
                verifyOTP(code);
            } else {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOTP(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // 🔐 Sign in with credential to activate Firebase UID
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {
                            String firebaseUid = firebaseUser.getUid();
                            registerUser(firebaseUid);
                        } else {
                            Toast.makeText(this, "Firebase UID is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "OTP verification failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser(String firebaseUid) {
        String url = "http://54.87.73.6/insert_otp.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OTPVerificationActivity.this, MainActivity.class));
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
                params.put("password", password);
                params.put("phone", phone);
                params.put("firebase_uid", firebaseUid);   // ✅ UID added
                params.put("photo_url", "");               // optional
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(OTPVerificationActivity.this);
        queue.add(request);
    }
}
