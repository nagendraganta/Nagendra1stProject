package com.nagendra.nagendra;

public class Reward {
    private String title;
    private String amount;
    private String link;
    private String logoUrl;

    // ======= NEW CODE START =======
    private String description;  // added description field

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    // ======= NEW CODE END =======

    public Reward() {
        // Required empty constructor
    }

    public Reward(String title, String amount, String link, String logoUrl) {
        this.title = title;
        this.amount = amount;
        this.link = link;
        this.logoUrl = logoUrl;
    }

    public String getTitle() { return title; }
    public String getAmount() { return amount; }
    public String getLink() { return link; }
    public String getLogoUrl() { return logoUrl; }
}
