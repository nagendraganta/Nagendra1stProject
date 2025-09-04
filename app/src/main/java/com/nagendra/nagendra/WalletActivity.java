package com.nagendra.nagendra;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class WalletActivity extends AppCompatActivity {

    TextView tvWalletBalance, tvStatus;
    Button btnCreateWallet, btnWithdraw;
    EditText etWithdrawAmount;
    Spinner spinnerWithdrawType;
    ProgressBar progressBar;
    LinearLayout layoutContent, withdrawHistoryLayout;

    int userId;
    String selectedWithdrawType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        // UI Components
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvStatus = findViewById(R.id.tvStatus);
        btnCreateWallet = findViewById(R.id.btnCreateWallet);
        progressBar = findViewById(R.id.progressBar);
        layoutContent = findViewById(R.id.layoutContent);

        etWithdrawAmount = findViewById(R.id.etWithdrawAmount);
        btnWithdraw = findViewById(R.id.btnWithdraw);
        withdrawHistoryLayout = findViewById(R.id.withdrawHistoryLayout);
        spinnerWithdrawType = findViewById(R.id.spinnerWithdrawType);

        // Spinner values
        String[] types = {"Select Type", "Amazon Gift Card", "PhonePe Gift Card", "Google Play Gift Card"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWithdrawType.setAdapter(adapter);

        spinnerWithdrawType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWithdrawType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedWithdrawType = "";
            }
        });

        // Get user_id from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userId = sharedPref.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User session missing. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initial load
        fetchWalletBalance(true);
        loadWithdrawHistory();

        btnCreateWallet.setOnClickListener(v -> createWallet());
        btnWithdraw.setOnClickListener(v -> submitWithdrawRequest());
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchWalletBalance(false);
        loadWithdrawHistory();
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        layoutContent.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void createWallet() {
        String url = "http://54.87.73.6/create_wallet.php";
        btnCreateWallet.setEnabled(false);
        showLoading(true);

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", userId);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        btnCreateWallet.setEnabled(true);
                        showLoading(false);
                        String message = response.optString("message", "Wallet created.");
                        tvStatus.setText(message);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        fetchWalletBalance(false);
                    },
                    error -> {
                        btnCreateWallet.setEnabled(true);
                        showLoading(false);
                        tvStatus.setText("Server error while creating wallet");
                        error.printStackTrace();
                    });

            request.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            btnCreateWallet.setEnabled(true);
            showLoading(false);
            tvStatus.setText("Error creating wallet request");
            e.printStackTrace();
        }
    }

    private void fetchWalletBalance(boolean showLoader) {
        if(showLoader) showLoading(true);

        String url = "http://54.87.73.6/get_wallet_balance.php";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", userId);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        if(showLoader) showLoading(false);
                        try {
                            if (response.getBoolean("success")) {
                                String balance = response.getString("wallet_balance");
                                tvWalletBalance.setText("Wallet: ₹" + balance);
                                tvStatus.setText(" ");
                                btnCreateWallet.setVisibility(View.GONE);
                            } else {
                                tvWalletBalance.setText("Wallet: ₹0.00");
                                tvStatus.setText(response.optString("message", "Wallet not found"));
                                btnCreateWallet.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            tvWalletBalance.setText("Wallet: ₹0.00");
                            tvStatus.setText("Error reading wallet data");
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        if(showLoader) showLoading(false);
                        tvWalletBalance.setText("Wallet: ₹0.00");
                        tvStatus.setText("Server error while fetching balance");
                        error.printStackTrace();
                    });

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            if(showLoader) showLoading(false);
            tvWalletBalance.setText("Wallet: ₹0.00");
            tvStatus.setText("Error creating balance request");
            e.printStackTrace();
        }
    }

    private void submitWithdrawRequest() {
        String amountStr = etWithdrawAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedWithdrawType.equals("Select Type") || selectedWithdrawType.isEmpty()) {
            Toast.makeText(this, "Select a withdrawal type", Toast.LENGTH_SHORT).show();
            return;
        }

        int amount = Integer.parseInt(amountStr);

        if (amount < 100 || amount > 2000) {
            Toast.makeText(this, "Enter amount between ₹100 and ₹2000", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://54.87.73.6/withdraw_requestt.php";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", userId);
            jsonBody.put("amount", amount);
            jsonBody.put("withdraw_type", selectedWithdrawType);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        String message = response.optString("message", "Request submitted.");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        etWithdrawAmount.setText("");
                        fetchWalletBalance(false);
                        loadWithdrawHistory();
                    },
                    error -> {
                        error.printStackTrace();
                        tvStatus.setText("Server error while submitting withdraw");
                    });

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            Toast.makeText(this, "Error submitting request", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // ===== Updated withdrawal history =====
    private void loadWithdrawHistory() {
        withdrawHistoryLayout.removeAllViews();
        String url = "http://54.87.73.6/get_withdraw_history.php";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", userId);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                JSONArray history = response.getJSONArray("data");

                                for (int i = 0; i < history.length(); i++) {
                                    JSONObject item = history.getJSONObject(i);
                                    String amount = item.getString("amount");
                                    String status = item.getString("status");
                                    String date = item.getString("created_at");
                                    String type = item.optString("withdraw_type", "");

                                    // Create CardView dynamically
                                    LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    );
                                    cardParams.setMargins(0, 0, 0, 16);

                                    CardView card = new CardView(this);
                                    card.setLayoutParams(cardParams);
                                    card.setRadius(12f);
                                    card.setCardElevation(4f);
                                    card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

                                    LinearLayout innerLayout = new LinearLayout(this);
                                    innerLayout.setOrientation(LinearLayout.VERTICAL);
                                    innerLayout.setPadding(32, 32, 32, 32);

                                    TextView tvAmount = new TextView(this);
                                    tvAmount.setText("₹" + amount);
                                    tvAmount.setTextSize(16f);
                                    tvAmount.setTextColor(Color.parseColor("#1E88E5"));
                                    tvAmount.setTypeface(tvAmount.getTypeface(), android.graphics.Typeface.BOLD);

                                    TextView tvType = new TextView(this);
                                    tvType.setText(type);
                                    tvType.setTextSize(12f);
                                    tvType.setTextColor(Color.parseColor("#888888"));
                                    tvType.setPadding(0, 4, 0, 0);

                                    TextView tvStatus = new TextView(this);
                                    tvStatus.setText(status.toUpperCase());
                                    tvStatus.setTextSize(14f);
                                    tvStatus.setPadding(0, 8, 0, 0);

                                    switch (status.toLowerCase()) {
                                        case "approved":
                                            tvStatus.setText("APPROVED");
                                            tvStatus.setTextColor(Color.parseColor("#43A047"));
                                            break;
                                        case "pending":
                                            tvStatus.setText("PENDING");
                                            tvStatus.setTextColor(Color.parseColor("#FFA000"));
                                            break;
                                        case "rejected":
                                            tvStatus.setText("REJECTED");
                                            tvStatus.setTextColor(Color.parseColor("#D32F2F"));
                                            break;
                                        default:
                                            tvStatus.setTextColor(Color.BLACK);
                                    }

                                    TextView tvDate = new TextView(this);
                                    tvDate.setText(date);
                                    tvDate.setTextSize(12f);
                                    tvDate.setTextColor(Color.parseColor("#888888"));
                                    tvDate.setPadding(0, 4, 0, 0);

                                    innerLayout.addView(tvAmount);
                                    innerLayout.addView(tvType);
                                    innerLayout.addView(tvStatus);
                                    innerLayout.addView(tvDate);

                                    card.addView(innerLayout);
                                    withdrawHistoryLayout.addView(card);
                                }
                            } else {
                                TextView tv = new TextView(this);
                                tv.setText("No withdrawal history.");
                                withdrawHistoryLayout.addView(tv);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> error.printStackTrace());

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
