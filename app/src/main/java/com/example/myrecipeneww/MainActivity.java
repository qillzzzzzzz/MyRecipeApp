package com.example.myrecipeneww;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem; // Impor MenuItem jika belum ada
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        if (bottomNav == null) {
            Log.e(TAG, "bottomNav is null — periksa id di activity_main.xml");
            return;
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId(); // Ambil ID sekali

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.nav_favorite) {
                selectedFragment = new FavoriteFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else {
                Log.w(TAG, "ID menu tidak diketahui: " + itemId);
            }
            // -----------------------------

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true; // Mengembalikan true karena item telah ditangani
            }

            return false; // Mengembalikan false jika item tidak ditangani
        });

        // Set fragment awal saat aplikasi pertama kali dibuka
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            // Perbarui juga UI bottom navigation agar item home terpilih
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}
