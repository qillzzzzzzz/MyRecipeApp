package com.example.myrecipeneww;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

public class MakeNewActivity extends AppCompatActivity {

    private ImageView ivPreview, iconCamera;
    private FrameLayout btnTambahFoto;
    private EditText etTitle, etRingkasan, etAlat, etBahan, etLangkah;
    private Button btnSave;
    private Uri imageUri;
    private FirebaseHelper firebaseHelper;

    private final ActivityResultLauncher<Intent> customCropLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri croppedUri = result.getData().getParcelableExtra("croppedUri");
                    if (croppedUri != null) {
                        imageUri = croppedUri;
                        ivPreview.setVisibility(View.VISIBLE);
                        Glide.with(this).load(imageUri).into(ivPreview);

                        TextView tvTambahFoto = findViewById(R.id.tvTambahFoto);
                        if (tvTambahFoto != null) tvTambahFoto.setVisibility(View.GONE);
                        if (iconCamera != null) iconCamera.setVisibility(View.GONE);
                    }
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Intent intent = new Intent(this, CustomCropActivity.class);
                    intent.putExtra("imageUri", uri);
                    customCropLauncher.launch(intent);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_new);

        firebaseHelper = new FirebaseHelper();

        ivPreview = findViewById(R.id.ivPreview);
        btnTambahFoto = findViewById(R.id.btnTambahFoto);
        iconCamera = findViewById(R.id.iconCamera);
        etTitle = findViewById(R.id.etTitle);
        btnSave = findViewById(R.id.btnSave);

        LinearLayout expandableContainer = findViewById(R.id.expandableContainer2);
        LayoutInflater inflater = LayoutInflater.from(this);
        Map<String, EditText> sections = new HashMap<>();

        String[] titles = {"Ringkasan", "Alat", "Bahan", "Langkah"};
        for (String t : titles) {
            View item = inflater.inflate(R.layout.item_expandable_card, expandableContainer, false);
            Button btnHeader = item.findViewById(R.id.btnHeader);
            LinearLayout layoutContent = item.findViewById(R.id.layoutContent);
            EditText etIsi = item.findViewById(R.id.etIsi);

            btnHeader.setText(t);
            layoutContent.setVisibility(View.GONE);
            expandableContainer.addView(item);
            sections.put(t, etIsi);

            btnHeader.setOnClickListener(v ->
                    layoutContent.setVisibility(
                            layoutContent.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
                    ));
        }

        etRingkasan = sections.get("Ringkasan");
        etAlat = sections.get("Alat");
        etBahan = sections.get("Bahan");
        etLangkah = sections.get("Langkah");

        btnTambahFoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        iconCamera.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveRecipe());
    }

    private void saveRecipe() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Isi judul resep terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setRingkasan(etRingkasan.getText().toString().trim());
        recipe.setAlat(etAlat.getText().toString().trim());
        recipe.setBahan(etBahan.getText().toString().trim());
        recipe.setLangkah(etLangkah.getText().toString().trim());
        recipe.setCreatedAt(System.currentTimeMillis());
        recipe.setFavorite(false);

        // Simpan di node users/<uid>/recipes
        firebaseHelper.saveNewRecipeForCurrentUser(imageUri, recipe, new FirebaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(MakeNewActivity.this, "Resep berhasil disimpan!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MakeNewActivity.this, "Gagal menyimpan resep: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
