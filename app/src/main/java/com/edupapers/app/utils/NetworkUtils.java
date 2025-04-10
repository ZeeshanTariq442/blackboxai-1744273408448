package com.edupapers.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NetworkUtils {
    private static NetworkUtils instance;
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final List<NetworkCallback> callbacks;
    private boolean isConnected;

    public interface NetworkCallback {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    private NetworkUtils(Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.callbacks = new ArrayList<>();
        this.isConnected = checkInitialConnection();
        registerNetworkCallback();
    }

    public static synchronized NetworkUtils getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkUtils(context);
        }
        return instance;
    }

    private boolean checkInitialConnection() {
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                     capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                     capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }

    private void registerNetworkCallback() {
        if (connectivityManager == null) return;

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build();

        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                isConnected = true;
                notifyNetworkAvailable();
            }

            @Override
            public void onLost(@NonNull Network network) {
                isConnected = false;
                notifyNetworkLost();
            }

            @Override
            public void onUnavailable() {
                isConnected = false;
                notifyNetworkLost();
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    public void addCallback(NetworkCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
            // Immediately notify the new callback of current state
            if (isConnected) {
                callback.onNetworkAvailable();
            } else {
                callback.onNetworkLost();
            }
        }
    }

    public void removeCallback(NetworkCallback callback) {
        callbacks.remove(callback);
    }

    private void notifyNetworkAvailable() {
        for (NetworkCallback callback : callbacks) {
            callback.onNetworkAvailable();
        }
    }

    private void notifyNetworkLost() {
        for (NetworkCallback callback : callbacks) {
            callback.onNetworkLost();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getConnectionType() {
        if (connectivityManager == null) return "No Connection";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return "No Connection";

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) return "No Connection";

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return "WiFi";
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return "Mobile Data";
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return "Ethernet";
            }
        } else {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                switch (activeNetwork.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        return "WiFi";
                    case ConnectivityManager.TYPE_MOBILE:
                        return "Mobile Data";
                    case ConnectivityManager.TYPE_ETHERNET:
                        return "Ethernet";
                }
            }
        }

        return "No Connection";
    }

    public void cleanup() {
        callbacks.clear();
    }
}
