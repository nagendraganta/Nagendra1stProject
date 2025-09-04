package com.nagendra.nagendra;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nagendra.nagendra.adapters.BannerAdapter;
import com.nagendra.nagendra.adapters.BannerItem;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    // keep old RecyclerView reference (not removing anything)
    private RecyclerView bannerRecyclerView, contentRecyclerView;

    // new: ViewPager2 + DotsIndicator
    private ViewPager2 bannerViewPager;
    private DotsIndicator dotsIndicator;

    private BannerAdapter bannerAdapter;
    private ContentAdapter contentAdapter;
    private final List<BannerItem> bannerList = new ArrayList<>();
    private final List<ContentItem> contentList = new ArrayList<>();

    private BottomNavigationView bottomNavigation;

    // auto-slide handler
    private final Handler sliderHandler = new Handler();
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (bannerAdapter != null && bannerAdapter.getItemCount() > 0) {
                int current = bannerViewPager.getCurrentItem();
                int next = (current + 1) % bannerAdapter.getItemCount();
                bannerViewPager.setCurrentItem(next, true);
                sliderHandler.postDelayed(this, 3000); // every 3 sec
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Banners
        bannerViewPager = findViewById(R.id.bannerViewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);

        bannerAdapter = new BannerAdapter(this, bannerList);
        bannerViewPager.setAdapter(bannerAdapter);
        dotsIndicator.attachTo(bannerViewPager);

        loadBannersFromServer();

        //  Content (PDFs)
        contentRecyclerView = findViewById(R.id.contentRecyclerView);
        contentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contentAdapter = new ContentAdapter(this, contentList);
        contentRecyclerView.setAdapter(contentAdapter);
        loadContentFromServer();

        // Bottom Navigation
        bottomNavigation = findViewById(R.id.bottom_navigation);

        Map<Integer, Runnable> navActions = new HashMap<>();
        navActions.put(R.id.Wallet, () -> {
            Intent intent = new Intent(MenuActivity.this, WalletActivity.class);
            startActivity(intent);
        });
        navActions.put(R.id.settings, () -> {
            Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        navActions.put(R.id.rewards, () -> {
            Intent intent = new Intent(MenuActivity.this, RewardsActivity.class);
            startActivity(intent);
        });
        navActions.put(R.id.history, () -> {
            Intent intent = new Intent(MenuActivity.this, MyRewardsActivity.class);
            startActivity(intent);
        });
        bottomNavigation.setOnItemSelectedListener(item -> {
            Runnable action = navActions.get(item.getItemId());
            if (action != null) {
                action.run();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start auto-slide
        sliderHandler.postDelayed(sliderRunnable, 3000);

        // Refresh banners and content
        loadBannersFromServer();
        loadContentFromServer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    // Load Banners
    private void loadBannersFromServer() {
        String url = "http://54.87.73.6/banners.php";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        bannerList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            String title = obj.getString("title");
                            String image = obj.getString("image");
                            String details = obj.getString("details");

                            BannerItem item = new BannerItem(title, image, details);
                            bannerList.add(item);
                        }
                        bannerAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Banner Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Banner Error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    //  Load Content (PDFs)
    private void loadContentFromServer() {
        String url = "http://54.87.73.6/get_contents.php";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        contentList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            String title = obj.getString("title");
                            String type = obj.getString("type");
                            String fileUrl = obj.getString("url");

                            ContentItem item = new ContentItem(title, type, fileUrl);
                            contentList.add(item);
                        }
                        contentAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Content Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Content Error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    // Content Item & Adapter
    public static class ContentItem {
        public String title, type, url;

        public ContentItem(String title, String type, String url) {
            this.title = title;
            this.type = type;
            this.url = url;
        }
    }

    public static class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {
        private final List<ContentItem> items;
        private final Context context;

        public ContentAdapter(Context context, List<ContentItem> items) {
            this.context = context;
            this.items = items;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView title, type;
            android.widget.Button downloadBtn;

            public ViewHolder(android.view.View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                type = itemView.findViewById(R.id.type);
                downloadBtn = itemView.findViewById(R.id.downloadBtn);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(context)
                    .inflate(R.layout.content_item_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ContentItem item = items.get(position);
            holder.title.setText(item.title);
            holder.type.setText(item.type);

            holder.downloadBtn.setOnClickListener(v -> {
                DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(item.url);
                DownloadManager.Request req = new DownloadManager.Request(uri);
                req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, item.title + "." + item.type);
                dm.enqueue(req);
                Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
