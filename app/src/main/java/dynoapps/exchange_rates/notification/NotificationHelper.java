package dynoapps.exchange_rates.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import dynoapps.exchange_rates.LandingActivity;
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

    public static void showAlarmNotification(@NonNull Context context, String message, String category, int id) {

        NotificationHelper.createAlarmChannelIfNotExists(context);
        Intent pushIntent = new Intent(context, LandingActivity.class);
        pushIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                pushIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_ALARM)
                        .setSmallIcon(R.drawable.ic_add_alarm_white_24dp)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(context.getString(R.string.app_name))
                        .setDefaults(Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND
                                | Notification.DEFAULT_VIBRATE | Notification.FLAG_SHOW_LIGHTS);


        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message));

        mBuilder.setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setContentText(message);
        mBuilder.setAutoCancel(true);
        Notification notification = mBuilder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(category, id, notification);
    }
}
