package com.edupapers.app.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.edupapers.app.R;
import com.edupapers.app.activities.HomeActivity;
import com.edupapers.app.activities.MidtermActivity;
import com.edupapers.app.activities.FinalTermActivity;
import com.edupapers.app.utils.PreferenceManager;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "edu_papers_channel";
    private static final String CHANNEL_NAME = "EduPapers Notifications";
    private static final String CHANNEL_DESC = "Notifications for new papers and updates";

    // Notification Types
    private static final String TYPE_NEW_PAPER = "new_paper";
    private static final String TYPE_EXAM_REMINDER = "exam_reminder";
    private static final String TYPE_APP_UPDATE = "app_update";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            handleDataMessage(remoteMessage.getData());
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            handleNotificationMessage(remoteMessage.getNotification());
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");
        if (type == null) return;

        switch (type) {
            case TYPE_NEW_PAPER:
                handleNewPaper(data);
                break;
            case TYPE_EXAM_REMINDER:
                handleExamReminder(data);
                break;
            case TYPE_APP_UPDATE:
                handleAppUpdate(data);
                break;
        }
    }

    private void handleNewPaper(Map<String, String> data) {
        String paperType = data.get("paper_type"); // "midterm" or "finalterm"
        String courseCode = data.get("course_code");
        String title = data.get("title");

        Intent intent;
        if ("midterm".equals(paperType)) {
            intent = new Intent(this, MidtermActivity.class);
        } else {
            intent = new Intent(this, FinalTermActivity.class);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("course_code", courseCode);

        showNotification(
            "New Paper Available",
            String.format("%s: %s", courseCode, title),
            intent
        );
    }

    private void handleExamReminder(Map<String, String> data) {
        String examType = data.get("exam_type");
        String courseCode = data.get("course_code");
        String date = data.get("date");

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showNotification(
            "Exam Reminder",
            String.format("%s %s Exam on %s", courseCode, examType, date),
            intent
        );
    }

    private void handleAppUpdate(Map<String, String> data) {
        String version = data.get("version");
        String message = data.get("message");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + getPackageName()));

        showNotification(
            "App Update Available",
            String.format("Version %s: %s", version, message),
            intent
        );
    }

    private void handleNotificationMessage(RemoteMessage.Notification notification) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showNotification(
            notification.getTitle(),
            notification.getBody(),
            intent
        );
    }

    private void showNotification(String title, String message, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Save the token to SharedPreferences
        PreferenceManager preferenceManager = new PreferenceManager(this);
        preferenceManager.setString("fcm_token", token);
        
        // TODO: Send token to your server
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Example implementation of sending the token to your server
        // This could be an API call to your backend service
        // For demonstration purposes, we will just log the token
        Log.d("FCM Token", "Token sent to server: " + token);
        
        // Here you would typically use Retrofit or another HTTP client to send the token
        // Example:
        // ApiService apiService = ApiClient.getClient().create(ApiService.class);
        // Call<Void> call = apiService.sendFcmToken(token);
        // call.enqueue(new Callback<Void>() {
        //     @Override
        //     public void onResponse(Call<Void> call, Response<Void> response) {
        //         // Handle success
        //     }
        //     @Override
        //     public void onFailure(Call<Void> call, Throwable t) {
        //         // Handle failure
        //     }
        // });
    }
}
