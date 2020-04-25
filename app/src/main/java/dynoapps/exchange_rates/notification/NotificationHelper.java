package dynoapps.exchange_rates.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.R;

public class NotificationHelper {
    public static final String CHANNEL_ID_CONNECTION = "connection";
    public static final String CHANNEL_ID_ALARM = "alarm";

    private static boolean isOreoAndAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static void createConnectionChannelIfNotExists(@NonNull Context context) {
        if (isOreoAndAbove()) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager == null) return;
            if (notificationManager.getNotificationChannel(CHANNEL_ID_CONNECTION) != null)
                return;

            CharSequence name = context.getString(R.string.connection);
            String description = context.getString(R.string.connection);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_CONNECTION, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(false);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void createAlarmChannelIfNotExists(@NonNull Context context) {
        if (isOreoAndAbove()) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager == null || notificationManager.getNotificationChannel(CHANNEL_ID_ALARM) != null)
                return;

            CharSequence name = context.getString(R.string.alarms);
            String description = context.getString(R.string.alarm_notifications);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_ALARM, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                    .build());
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setGroup("alarms");

            notificationManager.createNotificationChannel(channel);
        }
    }
}
