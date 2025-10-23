package com.example.myrecipeneww;

public class Recipe {
    private String id;
    private String title;
    private String ringkasan;
    private String alat;
    private String bahan;
    private String langkah;
    private String imageUrl;
    private boolean isFavorite;
    private long createdAt;

    // Konstruktor kosong wajib untuk Firebase
    public Recipe() {}

    public Recipe(String id, String title, String ringkasan, String alat, String bahan,
                  String langkah, String imageUrl, boolean isFavorite, long createdAt) {
        this.id = id;
        this.title = title;
        this.ringkasan = ringkasan;
        this.alat = alat;
        this.bahan = bahan;
        this.langkah = langkah;
        this.imageUrl = imageUrl;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRingkasan() { return ringkasan; }
    public void setRingkasan(String ringkasan) { this.ringkasan = ringkasan; }

    public String getAlat() { return alat; }
    public void setAlat(String alat) { this.alat = alat; }

    public String getBahan() { return bahan; }
    public void setBahan(String bahan) { this.bahan = bahan; }

    public String getLangkah() { return langkah; }
    public void setLangkah(String langkah) { this.langkah = langkah; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
