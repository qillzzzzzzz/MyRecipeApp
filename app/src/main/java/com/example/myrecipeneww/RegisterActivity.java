package com.example.myrecipeneww;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText etName, etEmail, etPassword, etConfirm;
    private Button btnRegister;
    private TextView tvLogin, tvGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // findViewById
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tv_login);
        tvGoogle = findViewById(R.id.tvGoogle);

        // tombol register untuk preview JSON
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                try {
                    attemptRegisterPureUX();
                } catch (Exception e) {
                    Log.e(TAG, "Error on register click", e);
                    Toast.makeText(RegisterActivity.this, "Terjadi error saat proses registrasi (cek log).", Toast.LENGTH_LONG).show();
                }
            });
        }

        // Pindah ke LoginActivity
        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // menutup activity register
            });
        }

        // Google sign in (masih UX-only)
        if (tvGoogle != null) {
            tvGoogle.setOnClickListener(v ->
                    Toast.makeText(this, "Login with Google: ditunda (UX-only mode).", Toast.LENGTH_SHORT).show());
        }
    }

    private void attemptRegisterPureUX() {
        if (etName == null || etEmail == null || etPassword == null || etConfirm == null) {
            Toast.makeText(this, "Form tidak lengkap (temukan error di logcat).", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();

        // validasi dasar
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@")) {
            etEmail.setError("Email tidak valid");
            return;
        }

        if (!pass.equals(confirm)) {
            etConfirm.setError("Kata sandi tidak cocok");
            return;
        }

        try {
            // buat JSON preview (ini hanya preview — tidak disimpan)
            JSONObject userJson = new JSONObject();
            userJson.put("fullName", name);
            userJson.put("email", email.toLowerCase());
            // untuk UX: tampilkan password yang sudah di-hash (agar terlihat 'seperti' backend)
            String hashed = hashPassword(pass);
            userJson.put("passwordHash", hashed);
            userJson.put("profilePictureUrl", JSONObject.NULL);
            userJson.put("createdAt", new Date().getTime()); // epoch ms

            showJsonPreview(userJson.toString(4)); // pretty print indent 4
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Hashing error", e);
            Toast.makeText(this, "Terjadi error hashing (cek log).", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
            Toast.makeText(this, "Terjadi error membentuk JSON.", Toast.LENGTH_SHORT).show();
        }
    }

    // tampilkan dialog berisi JSON dan tombol copy
    private void showJsonPreview(String prettyJson) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Preview JSON (UX-only)");

        // custom view supaya scrollable dan rapi
        LayoutInflater inflater = LayoutInflater.from(this);
        TextView tv = (TextView) inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
        tv.setText(prettyJson);
        tv.setPadding(24, 24, 24, 24);

        builder.setView(tv);
        builder.setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Copy JSON", (dialog, which) -> {
            copyToClipboard(prettyJson);
            Toast.makeText(this, "JSON disalin ke clipboard", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    private void copyToClipboard(String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("user_json", text);
            if (clipboard != null) clipboard.setPrimaryClip(clip);
        } catch (Exception e) {
            Log.w(TAG, "Gagal copy ke clipboard", e);
        }
    }

    // hash SHA-256 dan encode base64 (hanya untuk tampilan/UX)
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }
}