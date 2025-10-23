package com.example.myrecipeneww;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText etSearch;
    private RecyclerView rvSearch;
    private RecipeAdapterSearch recipeAdapter;
    private final List<RecipeSearch> recipeList = new ArrayList<>();
    private DatabaseReference recipesRef;
    private String currentUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        etSearch = view.findViewById(R.id.etSearch);
        rvSearch = view.findViewById(R.id.rvSearch);

        // ✅ Adapter tanpa listener (karena sudah auto buka RecipeActivity di adapter)
        recipeAdapter = new RecipeAdapterSearch(recipeList);

        rvSearch.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvSearch.setAdapter(recipeAdapter);

        // Ambil UID user login
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return view;
        }

        currentUid = auth.getCurrentUser().getUid();
        recipesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUid)
                .child("recipes");

        // Load semua resep
        loadAllRecipes();

        // 🔹 Setup search dengan tombol Enter
        etSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        etSearch.setSingleLine(true);
        etSearch.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String keyword = etSearch.getText().toString().trim();
                if (TextUtils.isEmpty(keyword)) {
                    loadAllRecipes();
                } else {
                    searchRecipes(keyword);
                }
                return true;
            }
            return false;
        });

        return view;
    }

    // 🔹 Load semua resep user
    private void loadAllRecipes() {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();

                for (DataSnapshot recipeSnap : snapshot.getChildren()) {
                    RecipeSearch recipe = recipeSnap.getValue(RecipeSearch.class);
                    if (recipe == null) continue;

                    String title = recipeSnap.child("title").getValue(String.class);
                    if (title != null) recipe.setName(title);

                    recipe.setId(recipeSnap.getKey());
                    recipeList.add(recipe);
                }

                recipeAdapter.updateRecipeList(recipeList);

                if (recipeList.isEmpty()) {
                    Toast.makeText(getContext(), "Belum ada resep yang kamu buat", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Gagal load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Search resep user berdasarkan judul
    private void searchRecipes(String keyword) {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                String lowerKeyword = keyword.toLowerCase();

                for (DataSnapshot recipeSnap : snapshot.getChildren()) {
                    String title = recipeSnap.child("title").getValue(String.class);

                    if (title != null && title.toLowerCase().contains(lowerKeyword)) {
                        RecipeSearch recipe = recipeSnap.getValue(RecipeSearch.class);
                        if (recipe != null) {
                            recipe.setName(title);
                            recipe.setId(recipeSnap.getKey());
                            recipeList.add(recipe);
                        }
                    }
                }

                recipeAdapter.updateRecipeList(recipeList);

                if (recipeList.isEmpty()) {
                    Toast.makeText(getContext(), "Resep tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Gagal mencari data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
