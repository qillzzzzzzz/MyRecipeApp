package com.example.myrecipeneww;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class CookRecipeActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView ivPreview;
    private LinearLayout expandableContainer3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cook_recipe_activity);

        tvTitle = findViewById(R.id.tvTitle2);
        ivPreview = findViewById(R.id.ivPreview2);
        expandableContainer3 = findViewById(R.id.expandableContainer3);

        String recipeId = getIntent().getStringExtra("recipeId");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("recipes")
                .child(recipeId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe r = snapshot.getValue(Recipe.class);
                if (r != null) {
                    tvTitle.setText(r.getTitle() != null ? r.getTitle() : "Tanpa Judul");

                    if (r.getImageUrl() != null && !r.getImageUrl().isEmpty()) {
                        Glide.with(CookRecipeActivity.this).load(r.getImageUrl()).into(ivPreview);
                    } else {
                        ivPreview.setImageResource(R.drawable.placeholder_image);
                    }

                    addExpandableSection("Ringkasan", r.getRingkasan());
                    addExpandableSection("Alat", r.getAlat());
                    addExpandableSection("Bahan", r.getBahan());
                    addExpandableSection("Langkah", r.getLangkah());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CookRecipeActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
}
