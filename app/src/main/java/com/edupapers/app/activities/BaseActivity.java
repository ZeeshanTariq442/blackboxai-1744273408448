package com.edupapers.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.edupapers.app.R;
import com.edupapers.app.utils.PreferenceManager;

public abstract class BaseActivity extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    protected FrameLayout contentFrame;
    protected PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        
        preferenceManager = new PreferenceManager(this);
        setupViews();
        setupDrawer();
        setupCurrentTheme();
    }

    private void setupViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        contentFrame = findViewById(R.id.content_frame);
        
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupCurrentTheme() {
        int currentTheme = preferenceManager.getThemeMode();
        AppCompatDelegate.setDefaultNightMode(currentTheme);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home && !(this instanceof HomeActivity)) {
            startActivity(new Intent(this, HomeActivity.class));
        } else if (id == R.id.nav_midterm && !(this instanceof MidtermActivity)) {
            startActivity(new Intent(this, MidtermActivity.class));
        } else if (id == R.id.nav_finalterm && !(this instanceof FinalTermActivity)) {
            startActivity(new Intent(this, FinalTermActivity.class));
        } else if (id == R.id.nav_downloads && !(this instanceof DownloadsActivity)) {
            startActivity(new Intent(this, DownloadsActivity.class));
        } else if (id == R.id.nav_theme) {
            toggleTheme();
        } else if (id == R.id.nav_rate) {
            openPlayStore();
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_feedback) {
            sendFeedback();
        } else if (id == R.id.nav_privacy && !(this instanceof PrivacyPolicyActivity)) {
            startActivity(new Intent(this, PrivacyPolicyActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void toggleTheme() {
        int newTheme;
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            newTheme = AppCompatDelegate.MODE_NIGHT_NO;
            showMessage(getString(R.string.theme_light));
        } else {
            newTheme = AppCompatDelegate.MODE_NIGHT_YES;
            showMessage(getString(R.string.theme_dark));
        }
        preferenceManager.setThemeMode(newTheme);
        AppCompatDelegate.setDefaultNightMode(newTheme);
        recreate();
    }

    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, 
                "Check out " + getString(R.string.app_name) + " on Play Store: " +
                "https://play.google.com/store/apps/details?id=" + getPackageName());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.settings_share)));
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:feedback@edupapers.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Feedback");
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.settings_feedback)));
        } catch (android.content.ActivityNotFoundException e) {
            showMessage(getString(R.string.msg_no_email_app));
        }
    }

    protected void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    protected void showError(String error) {
        Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.error, getTheme()))
                .setTextColor(getResources().getColor(R.color.white, getTheme()))
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void setContentView(@LayoutRes int layoutResID) {
        if (layoutResID == R.layout.activity_base) {
            super.setContentView(layoutResID);
        } else {
            contentFrame.removeAllViews();
            getLayoutInflater().inflate(layoutResID, contentFrame, true);
        }
    }
}
