package com.example.myrecipeneww;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ImageView whisk, bowl, logoText, splashBlob;
    private View overlayFill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        whisk = findViewById(R.id.whisk);
        bowl = findViewById(R.id.bowl);
        logoText = findViewById(R.id.logoText);
        splashBlob = findViewById(R.id.splashBlob);
        overlayFill = findViewById(R.id.overlayFill);

        // pastikan whisk di atas secara visual
        whisk.bringToFront();
        // tambahkan sedikit elevation biar pasti di atas pada beberapa device
        whisk.setTranslationZ(20f);
        bowl.setTranslationZ(10f);
        logoText.setTranslationZ(15f);

        playAnimationSequence();
    }

    private void playAnimationSequence() {
        // 1. logoText fade in
        ObjectAnimator logoFade = ObjectAnimator.ofFloat(logoText, View.ALPHA, 0f, 1f);
        logoFade.setDuration(450);
        logoFade.setInterpolator(new AccelerateDecelerateInterpolator());

        // 2. bowl subtle scale
        ObjectAnimator bowlScaleX = ObjectAnimator.ofFloat(bowl, View.SCALE_X, 0.98f, 1f);
        ObjectAnimator bowlScaleY = ObjectAnimator.ofFloat(bowl, View.SCALE_Y, 0.98f, 1f);
        bowlScaleX.setDuration(300);
        bowlScaleY.setDuration(300);

        // 3. whisk rotate (ngaduk)
        ObjectAnimator whiskRotate = ObjectAnimator.ofFloat(whisk, View.ROTATION, 0f, 28f, -22f, 12f, -6f, 0f);
        whiskRotate.setDuration(900);
        whiskRotate.setInterpolator(new LinearInterpolator());
        whiskRotate.setRepeatCount(1);

        // 4. splashBlob muncul (tekstur) dan overlayFill untuk bold full-screen
        // splashBlob scale + alpha
        splashBlob.setScaleX(0.8f);
        splashBlob.setScaleY(0.8f);
        ObjectAnimator blobAlpha = ObjectAnimator.ofFloat(splashBlob, View.ALPHA, 0f, 1f);
        ObjectAnimator blobScaleX = ObjectAnimator.ofFloat(splashBlob, View.SCALE_X, 0.8f, 1.6f);
        ObjectAnimator blobScaleY = ObjectAnimator.ofFloat(splashBlob, View.SCALE_Y, 0.8f, 1.6f);
        blobAlpha.setDuration(350);
        blobScaleX.setDuration(500);
        blobScaleY.setDuration(500);

        // overlay fill: ini yang bikin warna jadi tebal (bold)
        overlayFill.setScaleX(0.6f);
        overlayFill.setScaleY(0.6f);
        overlayFill.setAlpha(0f);
        ObjectAnimator overlayAlpha = ObjectAnimator.ofFloat(overlayFill, View.ALPHA, 0f, 1f);
        ObjectAnimator overlayScaleX = ObjectAnimator.ofFloat(overlayFill, View.SCALE_X, 0.6f, 2.2f);
        ObjectAnimator overlayScaleY = ObjectAnimator.ofFloat(overlayFill, View.SCALE_Y, 0.6f, 2.2f);
        overlayAlpha.setDuration(300);
        overlayScaleX.setDuration(600);
        overlayScaleY.setDuration(600);

        overlayScaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        overlayScaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        overlayAlpha.setInterpolator(new AccelerateDecelerateInterpolator());

        // sequence: logoFade + bowlScale -> whiskRotate -> blob+overlay -> next
        AnimatorSet first = new AnimatorSet();
        first.playTogether(logoFade, bowlScaleX, bowlScaleY);

        AnimatorSet mid = new AnimatorSet();
        mid.play(whiskRotate);

        AnimatorSet finalBlob = new AnimatorSet();
        finalBlob.playTogether(blobAlpha, blobScaleX, blobScaleY, overlayAlpha, overlayScaleX, overlayScaleY);

        AnimatorSet total = new AnimatorSet();
        total.playSequentially(first, mid, finalBlob);

        total.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // delay singkat supaya overlay terasa, lalu pergi ke login
                overlayFill.postDelayed(() -> goToLogin(), 280);
            }
        });

        total.start();
    }

    private void goToLogin() {
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }
}
