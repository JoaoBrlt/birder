package pns.si3.ihm.birder.views.notifications;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import java.util.Objects;

public class NotificationApp extends Application {

    public  static final String CHANNEL_ID = "Channel 1";
    private static NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel("Channel 1", "Channel pour les alertes d'oiseaux", NotificationManager.IMPORTANCE_DEFAULT);
    }


    private void createNotificationChannel(String name, String description, int importance){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }

    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }

}