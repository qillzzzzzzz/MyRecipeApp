package com.example.myrecipeneww;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Tombol Make New
        ImageButton btnMakeNew = view.findViewById(R.id.btn_makenew);
        btnMakeNew.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MakeNewActivity.class);
            startActivity(intent);
        });

        // Tombol Recipe → pindah ke MyRecipeActivity
        ImageButton btnRecipe = view.findViewById(R.id.btn_recipe);
        btnRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyRecipeActivity.class);
            startActivity(intent);
        });

        // Update username
        TextView tvUser = view.findViewById(R.id.tv_user);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName();
            if (username == null || username.isEmpty()) {
                username = "User";
            }
            tvUser.setText(username + "!");
        }

        return view;
    }
}

