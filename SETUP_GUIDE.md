# YAM Chat - Android 24/7 Arka Planda Çalışma Rehberi

## 📋 Dosya Yapısı

```
android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/yam/chat/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── receivers/
│   │   │   │   │   ├── BootReceiver.java
│   │   │   │   │   └── NetworkReceiver.java
│   │   │   │   └── services/
│   │   │   │       ├── ForegroundMessagingService.java
│   │   │   │       ├── BackgroundSyncWorker.java
│   │   │   │       └── YAMFirebaseMessagingService.java
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   ├── colors.xml
│   │   │       │   └── styles.xml
│   │   │       └── drawable/
│   │   │           ├── ic_launcher.xml
│   │   │           └── ic_launcher_round.xml
│   └── build.gradle
└── build.gradle (project)
```

---

## 🔧 Adım 1: AndroidManifest.xml Kurulumu

### Yapılması Gerekenler:

1. **OneSignal App ID Güncelle**
   ```xml
   <meta-data
       android:name="com.onesignal.app_id"
       android:value="ONESIGNAL_APP_ID_HERE" />
   ```
   ➜ `ONESIGNAL_APP_ID_HERE` yerine gerçek App ID'yi koy

2. **Package Name Değiştir**
   ```xml
   package="com.yam.chat"
   ```
   ➜ Kendi paket adını koy (örn: `com.example.yamchat`)

3. **URL Scheme Güncelle**
   ```xml
   <data android:scheme="https" android:host="yourdomain.com" />
   ```
   ➜ `yourdomain.com` yerine kendi domain'ini yaz

---

## ⚙️ Adım 2: build.gradle (App Level) Konfigürasyonu

```gradle
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.yam.chat"  // Paket adı
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    // Foreground Service Türleri (Android 14+)
    packagingOptions {
        resources {
            pickFirsts += ['META-INF/versions/9/OSGI-INF/MANIFEST.MF']
        }
    }
}

dependencies {
    // AndroidX
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.work:work-runtime:2.8.1'
    
    // OneSignal (En yeni sürüm)
    implementation 'com.onesignal:OneSignal:[5, 6)'
    
    // Firebase (Opsiyonel - Push Notifications için)
    implementation 'com.google.firebase:firebase-messaging:23.2.1'
    
    // Supabase (Gerçek zamanlı mesajlaşma)
    implementation 'io.github.supabase:gotrue-kt:1.0.0'
    implementation 'io.github.supabase:realtime-kt:1.0.0'
}
```

---

## 📱 Adım 3: OneSignal Entegrasyonu

### OneSignal Web Panelinde:

1. **App Oluştur**
   - https://onesignal.com/apps adresine git
   - "Create New App" tıkla
   - Platform seç: **Google Android**

2. **Gerekli Bilgileri Al**
   - OneSignal App ID
   - OneSignal REST API Key

3. **Firebase Project Oluştur** (OneSignal gerekli kılıyor)
   - https://console.firebase.google.com
   - Yeni proje oluştur
   - Android uygulaması ekle
   - `google-services.json` indir

### Android Projesinde:

```gradle
// project level build.gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.3.15'
    }
}

// app level build.gradle
plugins {
    id 'com.google.gms.google-services'
}
```

---

## 🚀 Adım 4: Arka Planda 24/7 Çalışma Mekanizmaları

### 1. **Foreground Service** (Görek Bildirimi ile)
   - Bildirim çubuğunda görsün
   - Sistem tarafından kolay kolay kapatılamaz
   - **Avantaj**: En güvenilir yöntem

### 2. **WorkManager** (15 dakika aralıklı)
   - Sistem gücü yönetimi
   - Otomatik yeniden zamanlanma
   - **Avantaj**: Pil tasarrufu

### 3. **Boot Receiver** (Cihaz açılışında)
   - Telefon yeniden açıldığında servisi başlat
   - **Avantaj**: Kalıcı çalışma

### 4. **Network Receiver** (İnternet geri geldiğinde)
   - Bağlantı kesilirse, geri geldiğinde senkronize et
   - **Avantaj**: Veri tutarlılığı

---

## 🛡️ Adım 5: İzinler ve Güvenlik

### İzinler Açıklaması:

| İzin | Amaç |
|------|------|
| `INTERNET` | Web istekleri |
| `ACCESS_NETWORK_STATE` | İnternet durumu kontrolü |
| `RECEIVE_BOOT_COMPLETED` | Cihaz açılışında servisi başlat |
| `WAKE_LOCK` | Ekran uyku modunda da çalış |
| `POST_NOTIFICATIONS` | Bildirim göster (Android 13+) |
| `VIBRATE` | Bildirim titreşimi |

### Runtime İzinleri (Android 6.0+):

```java
// MainActivity'de
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(
        this, 
        Manifest.permission.POST_NOTIFICATIONS
    ) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            this,
            new String[]{Manifest.permission.POST_NOTIFICATIONS},
            NOTIFICATION_PERMISSION_CODE
        );
    }
}
```

---

## 💡 Adım 6: WebView Entegrasyonu (Opsiyonel)

PWA yerine Android WebView kullanmak istersen:

```java
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        
        webView.loadUrl("https://yourdomain.com");
    }
}
```

---

## 📝 Adım 7: strings.xml Konfigürasyonu

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">YAM Chat</string>
    <string name="notification_title">YAM Chat</string>
    <string name="notification_message">Mesajları alma hazır</string>
    <string name="channel_name">YAM Chat Bildirimleri</string>
    <string name="channel_description">YAM Chat mesaj bildirimleri</string>
</resources>
```

---

## 🔌 Adım 8: Supabase Entegrasyonu

```java
import io.github.supabase.SupabaseClient

public class SupabaseManager {
    private static SupabaseClient supabase;

    public static void init(String url, String key) {
        supabase = new SupabaseClient(url, key);
    }

    public static void subscribeToMessages(String userId) {
        supabase.getRealtimeClient()
            .channel("messages:user_id=eq." + userId)
            .on("*", payload -> {
                // Yeni mesaj aldı
                showNotification(payload);
            })
            .subscribe();
    }

    private static void showNotification(String message) {
        // OneSignal ile bildirim göster
        OneSignal.sendTag("message_received", message);
    }
}
```

---

## 🧪 Test Etme

### 1. Arka Plan Servisi Çalışıyor mu?
```bash
adb shell dumpsys activity services | grep com.yam.chat
```

### 2. Bildirimler Geliyor mu?
- OneSignal panelinden test bildirimi gönder
- Ekran kapalı durumdayken test et

### 3. Cihaz Açılışında Çalışıyor mu?
```bash
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
```

### 4. Ağ Kesintisinden Sonra?
- WiFi'yi kapat/aç
- Mobil veriyi kapat/aç
- Senkronizasyonun başladığını kontrol et

---

## ⚡ Performans İyileştirmeleri

### 1. **Pil Tasarrufu**
```java
// WorkManager öncelikleri
.setBackoffCriteria(
    BackoffPolicy.EXPONENTIAL,
    5,
    TimeUnit.MINUTES
)
```

### 2. **Veri Tasarrufu**
```java
// Compress edilmiş veri akışı
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new GzipRequestInterceptor())
    .build();
```

### 3. **Bellek Optimizasyonu**
```java
// Zayıf referans kullan
WeakReference<Context> contextRef = new WeakReference<>(context);
```

---

## 🐛 Sık Karşılaşılan Sorunlar

| Sorun | Çözüm |
|-------|-------|
| Servis kapanıyor | `START_STICKY` ve Foreground Service kullan |
| Bildirimler gelmiyor | OneSignal izinlerini kontrol et |
| Pil çok hızlı tükeniyor | İş sıklığını artır (15 min → 30 min) |
| Veri senkronize olmuyor | NetworkReceiver düzgün çalışıyor mu kontrol et |

---

## 📦 APK Oluşturma

### Debug APK:
```bash
./gradlew assembleDebug
# app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (İmzalı):
```bash
./gradlew assembleRelease
# app/build/outputs/apk/release/app-release.apk
```

---

## 🔐 Güvenlik Kontrol Listesi

- [ ] OneSignal App ID gizli (strings.xml'de değil)
- [ ] Supabase anahtarlarını environment variable'a koy
- [ ] Firebase Security Rules konfigüre et
- [ ] SSL/TLS zorunlu (`usesCleartextTraffic="false"`)
- [ ] Debuggable flag `false` (production'da)
- [ ] ProGuard/R8 obfuscation aktif

---

## 🚀 Deployment

1. **Google Play'e Yükle**
   - Signed APK oluştur
   - Google Play Console'da app oluştur
   - Beta → Production sırası ile yayınla

2. **Xiaomi/Huawei Ön Izin**
   - Bu cihazlarda auto-start izni gerekebilir
   - Kullanıcıdan el ile aktif ettirilmesi gerekebilir

3. **iOS Alternatifu**
   - PWA olarak dağıt (iOS 16.4+)
   - Native Swift uygulaması yaz

---

## 📞 Destek

- OneSignal Docs: https://documentation.onesignal.com/docs/android-sdk-setup
- Firebase Cloud Messaging: https://firebase.google.com/docs/cloud-messaging
- WorkManager: https://developer.android.com/guide/background/persistent-work
- Supabase Realtime: https://supabase.com/docs/guides/realtime

---

**Son Güncelleme**: 2024
**Versiyon**: 1.0
