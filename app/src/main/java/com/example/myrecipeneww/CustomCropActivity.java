package com.example.myrecipeneww;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.canhub.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

public class CustomCropActivity extends AppCompatActivity {

    private CropImageView cropImageView;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_crop);

        cropImageView = findViewById(R.id.customCropImageView);
        Button btnBatal = findViewById(R.id.btnBatal);
        Button btnPilih = findViewById(R.id.btnPilih);

        imageUri = getIntent().getParcelableExtra("imageUri");
        if (imageUri != null) {
            cropImageView.setImageUriAsync(imageUri);
        }

        cropImageView.setGuidelines(CropImageView.Guidelines.ON);
        cropImageView.setFixedAspectRatio(true);
        cropImageView.setAspectRatio(16, 9);

        // Tombol Batal
        btnBatal.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        // Tombol Pilih
        btnPilih.setOnClickListener(v -> {
            Bitmap croppedBitmap = cropImageView.getCroppedImage();
            if (croppedBitmap != null) {
                Uri croppedUri = bitmapToUri(croppedBitmap);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("croppedUri", croppedUri);
                setResult(Activity.RESULT_OK, resultIntent);
            } else {
                setResult(Activity.RESULT_CANCELED);
            }
            finish();
        });
    }

    private Uri bitmapToUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(
                getContentResolver(), bitmap, "cropped_" + System.currentTimeMillis(), null);

        return Uri.parse(path);
    }
}
