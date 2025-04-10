package com.edupapers.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edupapers.app.R;
import com.edupapers.app.utils.PreferenceManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2 seconds
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        preferenceManager = new PreferenceManager(this);

        // Initialize views
        ImageView logoImage = findViewById(R.id.splash_logo);
        TextView titleText = findViewById(R.id.splash_title);
        TextView subtitleText = findViewById(R.id.splash_subtitle);

        // Create fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);

        // Set animation listener
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Start delayed transition to HomeActivity
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startHomeActivity();
                }, SPLASH_DELAY);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // Apply animations
        logoImage.startAnimation(fadeIn);
        titleText.startAnimation(fadeIn);
        subtitleText.startAnimation(fadeIn);

        // Initialize app data if needed
        initializeAppData();
    }

    private void initializeAppData() {
        // Check if it's first launch
        if (preferenceManager.getBoolean("first_launch", true)) {
            // Perform first launch operations
            preferenceManager.setBoolean("first_launch", false);
            
            // Set default theme
            if (!preferenceManager.contains("theme_mode")) {
                preferenceManager.setInt("theme_mode", 
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        }
    }

    private void startHomeActivity() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close splash activity
        
        // Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        // Disable back button during splash screen
    }
}
