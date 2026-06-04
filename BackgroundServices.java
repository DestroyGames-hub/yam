/* ===== MainActivity.java ===== */
package com.yam.chat;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.onesignal.OneSignal;
import com.yam.chat.receivers.NetworkReceiver;
import com.yam.chat.services.BackgroundSyncWorker;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // OneSignal başlangıç konfigürasyonu
        OneSignal.initWithContext(this);
        OneSignal.setAppId("YOUR_ONESIGNAL_APP_ID");
        OneSignal.setNotificationOpenedHandler(
            notificationResponse -> {
                // Bildirim tıklanınca yapılacak işlemler
                JSONObject data = notificationResponse.getNotification().getAdditionalData();
                if (data != null) {
                    String url = data.optString("url", null);
                    if (url != null) {
                        loadWebPage(url);
                    }
                }
            }
        );

        // Arka plan senkronizasyonu planla
        scheduleBackgroundSync();

        // Ağ değişiklikleri için receiver kaydı
        registerNetworkReceiver();

        // Foreground Service başlat
        startForegroundMessagingService();
    }

    private void scheduleBackgroundSync() {
        // Her 15 dakikada bir arka plan senkronizasyonu
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
            BackgroundSyncWorker.class,
            15,
            TimeUnit.MINUTES
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "yam_chat_sync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        );
    }

    private void registerNetworkReceiver() {
        networkReceiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(networkReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(networkReceiver, filter);
        }
    }

    private void startForegroundMessagingService() {
        Intent serviceIntent = new Intent(this, ForegroundMessagingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }

    private void loadWebPage(String url) {
        // Web sayfasını yükle (WebView kullanılabilir)
    }
}

/* ===== BackgroundSyncWorker.java ===== */
package com.yam.chat.services;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.yam.chat.api.SupabaseClient;

public class BackgroundSyncWorker extends Worker {

    public BackgroundSyncWorker(
        @NonNull Context context,
        @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Mesajları senkronize et
            syncMessages();
            
            // Bildirim durumunu kontrol et
            checkNotificationStatus();
            
            // Veritabanı güncelle
            syncDatabase();
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            // Başarısız olursa 5 dakika sonra tekrar dene
            return Result.retry();
        }
    }

    private void syncMessages() {
        // Supabase ile senkronizasyon
        SupabaseClient.getInstance().syncMessages(new Callback() {
            @Override
            public void onSuccess(Response response) {
                // Senkronizasyon başarılı
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void checkNotificationStatus() {
        // OneSignal bildirim durumunu kontrol et
    }

    private void syncDatabase() {
        // Yerel veritabanını güncelle
    }
}

/* ===== ForegroundMessagingService.java ===== */
package com.yam.chat.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.yam.chat.MainActivity;
import com.yam.chat.R;

public class ForegroundMessagingService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "yam_chat_channel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY; // Servis öldürülürse otomatik yeniden başlat
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "YAM Chat",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("YAM Chat uygulaması arka planda çalışıyor");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("YAM Chat")
            .setContentText("Mesajları alma hazır")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Bildirim iptal edilemez
            .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Servis durdurulsa bile, Android tarafından otomatik yeniden başlatılır
        // START_STICKY sayesinde
    }
}

/* ===== BootReceiver.java ===== */
package com.yam.chat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.yam.chat.services.ForegroundMessagingService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Cihaz açıldığında servisleri başlat
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
            intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON")) {

            Intent serviceIntent = new Intent(context, ForegroundMessagingService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}

/* ===== NetworkReceiver.java ===== */
package com.yam.chat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import com.yam.chat.services.BackgroundSyncWorker;

public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager cm = (ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                
                if (isConnected) {
                    // İnternet bağlantısı geri geldi, senkronizasyon başlat
                    triggerSync(context);
                }
            }
        }
    }

    private void triggerSync(Context context) {
        // Hemen senkronizasyon çalıştır
        androidx.work.WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "yam_chat_sync_now",
                androidx.work.ExistingWorkPolicy.REPLACE,
                new androidx.work.OneTimeWorkRequest.Builder(BackgroundSyncWorker.class)
                    .build()
            );
    }
}

/* ===== build.gradle (Dependencies) ===== */
/*
dependencies {
    // AndroidX
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.work:work-runtime:2.8.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

    // OneSignal
    implementation 'com.onesignal:OneSignal:[5, 6)'

    // Supabase
    implementation 'io.github.supabase:gotrue-kt:1.0.0'
    implementation 'io.github.supabase:realtime-kt:1.0.0'

    // Firebase (opsiyonel)
    implementation 'com.google.firebase:firebase-messaging:23.2.1'

    // Networking
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'

    // JSON
    implementation 'com.google.code.gson:gson:2.10.1'
}
*/
