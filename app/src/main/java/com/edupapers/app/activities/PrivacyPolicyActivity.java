package com.edupapers.app.activities;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.edupapers.app.R;
import com.edupapers.app.utils.NetworkUtils;

public class PrivacyPolicyActivity extends BaseActivity implements NetworkUtils.NetworkCallback {

    private WebView webView;
    private ProgressBar progressBar;
    private View errorView;
    private NetworkUtils networkUtils;

    // Demo privacy policy URL - replace with your actual privacy policy URL
    private static final String PRIVACY_POLICY_URL = "https://example.com/privacy-policy";
    
    // Fallback HTML content in case the URL is not accessible
    private static final String FALLBACK_PRIVACY_POLICY = "<!DOCTYPE html>" +
            "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; padding: 16px; }" +
            "h1 { color: #1976D2; }" +
            "h2 { color: #2196F3; margin-top: 24px; }" +
            "</style></head><body>" +
            "<h1>Privacy Policy</h1>" +
            "<p>Last updated: May 2023</p>" +
            "<h2>Information Collection and Use</h2>" +
            "<p>We collect information to provide better services to our users. The types of information we collect include:</p>" +
            "<ul>" +
            "<li>Device information</li>" +
            "<li>Log information</li>" +
            "<li>Location information</li>" +
            "</ul>" +
            "<h2>Data Storage</h2>" +
            "<p>We store downloaded papers locally on your device. These files can be managed through the Downloads section.</p>" +
            "<h2>Information Security</h2>" +
            "<p>We work hard to protect our users from unauthorized access to or unauthorized alteration, disclosure, or destruction of information we hold.</p>" +
            "<h2>Changes</h2>" +
            "<p>Our Privacy Policy may change from time to time. We will post any privacy policy changes on this page.</p>" +
            "</body></html>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        // Set toolbar title
        getSupportActionBar().setTitle(R.string.settings_privacy);

        initializeViews();
        setupWebView();
        setupNetworkCallback();
        loadPrivacyPolicy();
    }

    private void initializeViews() {
        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_bar);
        errorView = findViewById(R.id.error_view);
        networkUtils = NetworkUtils.getInstance(this);
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                loadFallbackContent();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });
    }

    private void setupNetworkCallback() {
        networkUtils.addCallback(this);
    }

    private void loadPrivacyPolicy() {
        progressBar.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);

        if (networkUtils.isConnected()) {
            webView.loadUrl(PRIVACY_POLICY_URL);
        } else {
            loadFallbackContent();
        }
    }

    private void loadFallbackContent() {
        webView.loadDataWithBaseURL(null, FALLBACK_PRIVACY_POLICY, "text/html", "UTF-8", null);
    }

    @Override
    public void onNetworkAvailable() {
        // Optionally reload the online version when network becomes available
    }

    @Override
    public void onNetworkLost() {
        showError(getString(R.string.msg_no_internet));
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        networkUtils.removeCallback(this);
        super.onDestroy();
    }
}
