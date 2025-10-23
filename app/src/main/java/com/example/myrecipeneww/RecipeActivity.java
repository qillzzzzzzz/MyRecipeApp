package com.example.myrecipeneww;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class RecipeActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView ivPreview;
    private LinearLayout expandableContainer3;

    private FirebaseHelper firebaseHelper;
    private String recipeId;
    private Recipe currentRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cook_recipe_activity);

        // 🔹 Inisialisasi view
        tvTitle = findViewById(R.id.tvTitle2);
        ivPreview = findViewById(R.id.ivPreview2);
        expandableContainer3 = findViewById(R.id.expandableContainer3);
        firebaseHelper = new FirebaseHelper();

        // 🔹 Ambil data dari intent
        recipeId = getIntent().getStringExtra("recipeId");
        String titleExtra = getIntent().getStringExtra("title");
        String imageExtra = getIntent().getStringExtra("imageUrl");

        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "ID resep tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔹 Tampilkan data sementara (supaya user tidak lihat layar kosong)
        tvTitle.setText(titleExtra != null ? titleExtra : "Tanpa Judul");
        if (imageExtra != null && !imageExtra.isEmpty()) {
            Glide.with(this)
                    .load(imageExtra)
                    .placeholder(R.drawable.placeholder_image)
                    .into(ivPreview);
        } else {
            ivPreview.setImageResource(R.drawable.placeholder_image);
        }

        // 🔹 Ambil UID user login
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        // 🔹 Ambil data lengkap dari Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("recipes")
                .child(recipeId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe r = snapshot.getValue(Recipe.class);
                if (r == null) {
                    Toast.makeText(RecipeActivity.this, "Resep tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                currentRecipe = r;
                currentRecipe.setId(recipeId);

                // 🔹 Update UI
                tvTitle.setText(r.getTitle() != null ? r.getTitle() : "Tanpa Judul");

                if (r.getImageUrl() != null && !r.getImageUrl().isEmpty()) {
                    Glide.with(RecipeActivity.this)
                            .load(r.getImageUrl())
                            .placeholder(R.drawable.placeholder_image)
                            .into(ivPreview);
                } else {
                    ivPreview.setImageResource(R.drawable.placeholder_image);
                }

                // 🔹 Hapus konten lama sebelum tambah baru
                expandableContainer3.removeAllViews();

                addExpandableSection("Ringkasan", r.getRingkasan());
                addExpandableSection("Alat", r.getAlat());
                addExpandableSection("Bahan", r.getBahan());
                addExpandableSection("Langkah", r.getLangkah());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Tambahkan bagian expandable
    private void addExpandableSection(String header, String isi) {
        View section = getLayoutInflater().inflate(R.layout.item_cook, expandableContainer3, false);

        Button btnHeader = section.findViewById(R.id.btnHeader);
        LinearLayout layoutContent = section.findViewById(R.id.layoutContent);
        TextView tvIsi = section.findViewById(R.id.tvIsi);

        btnHeader.setText(header);
        tvIsi.setText(isi != null && !isi.isEmpty() ? isi : "-");

        btnHeader.setOnClickListener(v -> {
            layoutContent.setVisibility(
                    layoutContent.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        });

        expandableContainer3.addView(section);
    }

    // 🔹 Tambahkan helper static supaya bisa dipanggil dari SearchFragment / Adapter
    public static void open(Context context, String id, String title, String imageUrl) {
        Intent intent = new Intent(context, RecipeActivity.class);
        intent.putExtra("recipeId", id);
        intent.putExtra("title", title);
        intent.putExtra("imageUrl", imageUrl);
        context.startActivity(intent);
    }
}
