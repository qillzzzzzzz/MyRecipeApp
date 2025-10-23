package com.example.myrecipeneww;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import java.util.HashMap;
import java.util.Map;

public class EditRecipeActivity extends AppCompatActivity {

    private static final int REQ_PICK_IMAGE = 1002;

    private EditText etTitle, etRingkasan, etAlat, etBahan, etLangkah;
    private ImageView ivPreview, btnTambahFoto;
    private Button btnSave;
    private LinearLayout expandableContainer;

    private Uri selectedImageUri = null;
    private FirebaseHelper firebaseHelper;
    private Recipe currentRecipe;

    private final Map<String, View> expandableSections = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_new); // pakai layout yang sama seperti tambah resep

        firebaseHelper = new FirebaseHelper();

        // Inisialisasi view
        etTitle = findViewById(R.id.etTitle);
        ivPreview = findViewById(R.id.ivPreview);
        btnTambahFoto = findViewById(R.id.btnTambahFoto);
        btnSave = findViewById(R.id.btnSave);
        expandableContainer = findViewById(R.id.expandableContainer2);

        // Pastikan container tidak null
        if (expandableContainer == null) {
            Toast.makeText(this, "Layout tidak cocok: pastikan ID expandableContainer2 ada di XML", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Buat ulang bagian expandable (Ringkasan, Alat, Bahan, Langkah)
        initExpandableSections();

        // Ambil EditText dari tiap section
        etRingkasan = expandableSections.get("Ringkasan").findViewById(R.id.etIsi);
        etAlat = expandableSections.get("Alat").findViewById(R.id.etIsi);
        etBahan = expandableSections.get("Bahan").findViewById(R.id.etIsi);
        etLangkah = expandableSections.get("Langkah").findViewById(R.id.etIsi);

        // Ambil data dari intent
        Intent intent = getIntent();
        String recipeId = intent.getStringExtra("recipeId");
        if (recipeId == null) {
            Toast.makeText(this, "ID resep tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Data sementara dari intent
        String titleExtra = intent.getStringExtra("title");
        String ringkasanExtra = intent.getStringExtra("ringkasan");
        String alatExtra = intent.getStringExtra("alat");
        String bahanExtra = intent.getStringExtra("bahan");
        String langkahExtra = intent.getStringExtra("langkah");
        String imageExtra = intent.getStringExtra("imageUrl");

        // Tampilkan data awal
        if (titleExtra != null) etTitle.setText(titleExtra);
        if (ringkasanExtra != null) etRingkasan.setText(ringkasanExtra);
        if (alatExtra != null) etAlat.setText(alatExtra);
        if (bahanExtra != null) etBahan.setText(bahanExtra);
        if (langkahExtra != null) etLangkah.setText(langkahExtra);
        if (imageExtra != null && !imageExtra.isEmpty()) {
            Glide.with(this).load(imageExtra).into(ivPreview);
        }

        // Ambil data terbaru dari Firebase
        firebaseHelper.getRecipeForCurrentUser(recipeId, new FirebaseHelper.OneRecipeCallback() {
            @Override
            public void onFound(Recipe recipe) {
                currentRecipe = recipe;
                currentRecipe.setId(recipeId);

                etTitle.setText(recipe.getTitle());
                etRingkasan.setText(recipe.getRingkasan());
                etAlat.setText(recipe.getAlat());
                etBahan.setText(recipe.getBahan());
                etLangkah.setText(recipe.getLangkah());

                if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                    Glide.with(EditRecipeActivity.this)
                            .load(recipe.getImageUrl())
                            .into(ivPreview);
                }
            }

            @Override
            public void onNotFound() {
                Toast.makeText(EditRecipeActivity.this, "Resep tidak ditemukan", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(EditRecipeActivity.this, "Gagal memuat resep: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Pilih gambar baru
        btnTambahFoto.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pick, REQ_PICK_IMAGE);
        });

        // Ubah teks tombol
        btnSave.setText("Perbarui Resep");

        // Simpan perubahan
        btnSave.setOnClickListener(v -> {
            if (currentRecipe == null) {
                currentRecipe = new Recipe();
                currentRecipe.setId(recipeId);
            }

            currentRecipe.setTitle(etTitle.getText().toString().trim());
            currentRecipe.setRingkasan(etRingkasan.getText().toString().trim());
            currentRecipe.setAlat(etAlat.getText().toString().trim());
            currentRecipe.setBahan(etBahan.getText().toString().trim());
            currentRecipe.setLangkah(etLangkah.getText().toString().trim());

            btnSave.setEnabled(false);

            if (selectedImageUri != null) {
                firebaseHelper.uploadImage(selectedImageUri, new FirebaseHelper.UrlCallback() {
                    @Override
                    public void onUrl(String url) {
                        currentRecipe.setImageUrl(url);
                        updateRecipe();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(EditRecipeActivity.this, "Gagal upload gambar", Toast.LENGTH_SHORT).show();
                        updateRecipe();
                    }
                });
            } else {
                updateRecipe();
            }
        });
    }

    private void updateRecipe() {
        firebaseHelper.updateRecipeForCurrentUser(currentRecipe, new FirebaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditRecipeActivity.this, "Resep berhasil diperbarui", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(EditRecipeActivity.this, "Gagal memperbarui resep: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
            }
        });
    }

    private void initExpandableSections() {
        String[] titles = {"Ringkasan", "Alat", "Bahan", "Langkah"};
        LayoutInflater inflater = LayoutInflater.from(this);
        int colorTextCollapsed = ContextCompat.getColor(this, R.color.cooki_brown);

        for (String title : titles) {
            View item = inflater.inflate(R.layout.item_expandable_card, expandableContainer, false);

            Button btnHeader = item.findViewById(R.id.btnHeader);
            LinearLayout layoutContent = item.findViewById(R.id.layoutContent);

            btnHeader.setText(title);
            btnHeader.setTextColor(colorTextCollapsed);
            layoutContent.setVisibility(View.VISIBLE); // langsung terbuka

            expandableSections.put(title, item);
            expandableContainer.addView(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Glide.with(this).load(selectedImageUri).into(ivPreview);
            }
        }
    }
}
