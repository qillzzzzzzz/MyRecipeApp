package com.example.myrecipeneww;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;

public class FavoriteFragment extends Fragment {

    private RecyclerView rvFavorites;
    private RecipeAdapterFavorite adapter;
    private ArrayList<RecipeFavorite> favoriteList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        rvFavorites = view.findViewById(R.id.rvFavorites);

        rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        favoriteList = new ArrayList<>();
        adapter = new RecipeAdapterFavorite(getContext(), favoriteList);
        rvFavorites.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference recipesRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("recipes");

            recipesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    favoriteList.clear();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Recipe r = snap.getValue(Recipe.class);
                        if (r != null && r.isFavorite()) {
                            String recipeId = snap.getKey();
                            favoriteList.add(new RecipeFavorite(
                                    recipeId,
                                    r.getTitle(),
                                    r.getRingkasan(),
                                    r.getImageUrl(),
                                    true
                            ));
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }

        return view;
    }
}
