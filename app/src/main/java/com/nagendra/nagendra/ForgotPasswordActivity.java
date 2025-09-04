package com.nagendra.nagendra;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final int OTP_WAIT_TIME = 2 * 60 * 1000; // 2 minutes in milliseconds
    private static final int OTP_INTERVAL = 1000; // 1 second in milliseconds

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Setup Toolbar with Back Arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
            getSupportActionBar().setTitle("");
        }

        // Initialize Views
        EditText emailInput = findViewById(R.id.emailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        EditText confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        EditText otpInput = findViewById(R.id.otpInput);
        Button sendOtpButton = findViewById(R.id.sendOtpButton);
        Button submitOtpButton = findViewById(R.id.submitOtpButton);

        // Initially hide OTP input and Submit button
        otpInput.setVisibility(View.GONE);
        submitOtpButton.setVisibility(View.GONE);

        // TextWatcher to enable the Send OTP button when all fields are filled
        TextWatcher formWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isFormFilled = !emailInput.getText().toString().trim().isEmpty() &&
                        !passwordInput.getText().toString().trim().isEmpty() &&
                        !confirmPasswordInput.getText().toString().trim().isEmpty();

                sendOtpButton.setEnabled(isFormFilled);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        // Attach the TextWatcher to the inputs
        emailInput.addTextChangedListener(formWatcher);
        passwordInput.addTextChangedListener(formWatcher);
        confirmPasswordInput.addTextChangedListener(formWatcher);

        // Send OTP Button Click Listener
        sendOtpButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            // Check if any field is empty
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(ForgotPasswordActivity.this, "Fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email
            if (!isValidEmail(email)) {
                Toast.makeText(ForgotPasswordActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate passwords
            if (!password.equals(confirmPassword)) {
                Toast.makeText(ForgotPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Simulate sending OTP
            Toast.makeText(ForgotPasswordActivity.this, "OTP sent to " + email, Toast.LENGTH_SHORT).show();

            // Show OTP input and Submit button
            otpInput.setVisibility(View.VISIBLE);
            submitOtpButton.setVisibility(View.VISIBLE);

            // Disable Send OTP button and start countdown timer
            sendOtpButton.setEnabled(false);
            startOtpCountdown(sendOtpButton);
        });


        // Submit OTP Button Click Listener
        submitOtpButton.setOnClickListener(v -> {
            String otp = otpInput.getText().toString().trim();

            if (otp.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            // Simulate OTP verification
            if (otp.equals("123456")) { // Example OTP for testing
                Toast.makeText(ForgotPasswordActivity.this, "Password changed successfully!", Toast.LENGTH_SHORT).show();

                // Navigate back to Login Activity
                Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ForgotPasswordActivity.this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to validate email
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Helper method to start countdown timer
    private void startOtpCountdown(Button sendOtpButton) {
        countDownTimer = new CountDownTimer(OTP_WAIT_TIME, OTP_INTERVAL) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                sendOtpButton.setText("Wait " + secondsRemaining + "s for new OTP");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                sendOtpButton.setText("Send OTP");
                sendOtpButton.setEnabled(true);
            }
        };
        countDownTimer.start();
    }

    // Handle Toolbar Back Arrow Click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to Login Activity
            Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
