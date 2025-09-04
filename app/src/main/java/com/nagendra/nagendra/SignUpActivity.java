package com.nagendra.nagendra;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity {

    EditText usernameInput, emailInput, passwordInput, confirmPasswordInput, phoneInput;
    Button signUpButton;


    FirebaseAuth mAuth;

    String username, email, password, phone;
    ProgressDialog progressDialog; // 🔄 Progress dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        phoneInput = findViewById(R.id.phoneInput);
        signUpButton = findViewById(R.id.signUpButton);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup Progress Dialog
        //noinspection deprecation
        progressDialog = new ProgressDialog(this);
        //noinspection deprecation
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);

        signUpButton.setOnClickListener(v -> {
            username = usernameInput.getText().toString().trim();
            email = emailInput.getText().toString().trim();
            password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            phone = phoneInput.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                    || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(phone)) {
                Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(SignUpActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.show(); // 🔄 Show loading spinner
            sendOTP("+91" + phone);
        });
    }

    private void sendOTP(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull com.google.firebase.auth.PhoneAuthCredential credential) {
                        progressDialog.dismiss(); // Hide progress if auto-completed
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressDialog.dismiss(); // ❌ Hide loading
                        Toast.makeText(SignUpActivity.this, "OTP Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        progressDialog.dismiss(); // ✅ Hide loading once OTP is sent

                        Intent intent = new Intent(SignUpActivity.this, OTPVerificationActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("username", username);
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        intent.putExtra("phone", phone);
                        startActivity(intent);
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}
