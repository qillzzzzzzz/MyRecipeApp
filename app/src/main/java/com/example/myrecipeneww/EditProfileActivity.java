package com.example.myrecipeneww;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etUsername, etPassword, etPhone;
    private Button btnSaveChange;
    private ImageView imgEditProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;

    private static final int PICK_IMAGE = 100;
    private static final int PERMISSION_REQUEST = 101;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsernm);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhn);
        btnSaveChange = findViewById(R.id.btnSaveChanges);
        imgEditProfile = findViewById(R.id.imgEditProfile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User belum login!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("profile");

        requestGalleryPermission();

        etEmail.setText(currentUser.getEmail());
        etName.setText(currentUser.getDisplayName());

        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(imgEditProfile);
        }

        // 🔹 Load data dari database
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("displayName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String profileImage = snapshot.child("profileImage").getValue(String.class);

                    if (name != null) etName.setText(name);
                    if (email != null) etEmail.setText(email);
                    if (username != null) etUsername.setText(username);
                    if (phone != null) etPhone.setText(phone);
                    if (profileImage != null) {
                        Glide.with(EditProfileActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .circleCrop()
                                .into(imgEditProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Gagal load data!", Toast.LENGTH_SHORT).show();
            }
        });

        imgEditProfile.setOnClickListener(v -> openGallery());
        btnSaveChange.setOnClickListener(v -> saveProfileChanges());
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            if (selectedImageUri != null) {
                Log.d("URI_DEBUG", "Selected image URI: " + selectedImageUri);
                Glide.with(this)
                        .load(selectedImageUri)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(imgEditProfile);
            } else {
                Toast.makeText(this, "URI gambar tidak valid!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfileChanges() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid!", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("displayName", name);
        updates.put("email", email);
        updates.put("username", username);
        updates.put("phone", phone);

        if (selectedImageUri != null) {
            uploadProfileImage(selectedImageUri, uri -> {
                updates.put("profileImage", uri.toString());
                usersRef.setValue(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateAuthProfile(name, email, password, uri);
                        Toast.makeText(this, "Foto & data profil tersimpan!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Gagal update data ke Database.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } else {
            usersRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updateAuthProfile(name, email, password, null);
                } else {
                    Toast.makeText(this, "Gagal update DB!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadProfileImage(Uri imageUri, OnUploadSuccessListener listener) {
        if (imageUri == null) {
            Toast.makeText(this, "Pilih gambar dulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("profile_images")
                .child(uid + "_" + System.currentTimeMillis() + ".jpg");

        Log.d("UPLOAD_DEBUG", "Uploading to path: " + storageRef.getPath());

        UploadTask uploadTask = storageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Log.d("UPLOAD_DEBUG", "File uploaded. URL: " + uri);
                        listener.onSuccess(uri);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal ambil URL gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload gambar gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("UPLOAD_ERROR", "Upload gagal", e);
        });
    }

    private void updateAuthProfile(String name, String email, String password, Uri photoUri) {
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                .setDisplayName(name);
        if (photoUri != null) builder.setPhotoUri(photoUri);

        currentUser.updateProfile(builder.build()).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Gagal update display name/photo di Auth", Toast.LENGTH_SHORT).show();
            }
            updateEmailAndPassword(email, password);
        });
    }

    private void updateEmailAndPassword(String newEmail, String newPassword) {
        if (currentUser == null) return;
        String currentAuthEmail = currentUser.getEmail();

        if (currentAuthEmail == null || !currentAuthEmail.equals(newEmail)) {
            currentUser.updateEmail(newEmail);
        }

        if (!TextUtils.isEmpty(newPassword) && newPassword.length() >= 6) {
            currentUser.updatePassword(newPassword);
        }

        Toast.makeText(this, "Profil berhasil diperbarui.", Toast.LENGTH_SHORT).show();
    }

    interface OnUploadSuccessListener {
        void onSuccess(Uri uri);
    }
}
