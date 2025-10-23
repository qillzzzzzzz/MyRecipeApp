package com.example.myrecipeneww;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private TabLayout tabLayout;
    private RecyclerView rvRecipes;
    private Button btnShowAll, btnEditProfile;
    private RecipeAdapterProfile adapter;
    private ArrayList<RecipeFavorite> favoriteList;
    private TextView tvRecipeCount, tvEmptyMessage;

    private DatabaseReference dbRef, usersRef;
    private FirebaseAuth auth;
    private ImageView imgProfile;
    private TextView tvUsername, tvEmail;

    public ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Views
        tabLayout = view.findViewById(R.id.tabLayout);
        rvRecipes = view.findViewById(R.id.rvRecipes);
        btnShowAll = view.findViewById(R.id.btnShowAllMyRecipes);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        imgProfile = view.findViewById(R.id.imgProfile);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvRecipeCount = view.findViewById(R.id.tvRecipeCount);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        btnShowAll.setVisibility(View.GONE);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            tvUsername.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "-");

            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(imgProfile);
            }

            usersRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.child("profileImage").getValue(String.class) != null) {
                        String imageUrl = snapshot.child("profileImage").getValue(String.class);
                        Glide.with(ProfileFragment.this).load(imageUrl).circleCrop().into(imgProfile);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        // Edit profile
        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(getActivity(), EditProfileActivity.class)));

        // RecyclerView setup
        favoriteList = new ArrayList<>();
        adapter = new RecipeAdapterProfile(getContext(), favoriteList);
        rvRecipes.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRecipes.setAdapter(adapter);

        // Database reference untuk resep user
        String uid = user != null ? user.getUid() : "";
        dbRef = FirebaseDatabase.getInstance().getReference("recipes").child(uid);

        // Tab listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) { // My Recipes
                    btnShowAll.setVisibility(View.VISIBLE);
                    loadAllRecipes();
                } else { // Favorites
                    btnShowAll.setVisibility(View.GONE);
                    loadFavoritesOnly();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnShowAll.setOnClickListener(v -> startActivity(new Intent(getActivity(), RecipeActivity.class)));

        // Default tab
        if (tabLayout.getTabCount() > 0) {
            TabLayout.Tab t = tabLayout.getTabAt(0);
            if (t != null) t.select();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && getView() != null) {
            tvUsername.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "-");
            if (user.getPhotoUrl() != null)
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(imgProfile);
        }
    }

    // 🔹 Load semua resep user
    private void loadAllRecipes() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteList.clear();
                int count = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        RecipeFavorite r = ds.getValue(RecipeFavorite.class);
                        if (r != null) {
                            favoriteList.add(r);
                            count++;
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                tvRecipeCount.setText(count + "\nRecipe");

                // tampilkan pesan jika kosong
                if (count == 0) {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    rvRecipes.setVisibility(View.GONE);
                } else {
                    tvEmptyMessage.setVisibility(View.GONE);
                    rvRecipes.setVisibility(View.VISIBLE);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // 🔹 Load hanya resep favorit
    private void loadFavoritesOnly() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteList.clear();
                int favCount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    RecipeFavorite r = ds.getValue(RecipeFavorite.class);
                    if (r != null && r.isFavorite()) {
                        favoriteList.add(r);
                        favCount++;
                    }
                }

                adapter.notifyDataSetChanged();
                tvRecipeCount.setText(favCount + "\nFavorite");

                // tampilkan pesan jika kosong
                if (favCount == 0) {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    rvRecipes.setVisibility(View.GONE);
                } else {
                    tvEmptyMessage.setVisibility(View.GONE);
                    rvRecipes.setVisibility(View.VISIBLE);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
