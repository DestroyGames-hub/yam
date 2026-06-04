# YAM Chat Android - Hızlı Başlangıç Rehberi ⚡

## 🎯 Hedef: 24/7 Arka Planda Çalışan Android Uygulaması

Oluşturulan **YAM Chat** PWA uygulamanızı Android'de 24/7 arka planda çalışan native uygulamaya dönüştürüyoruz.

---

## 📥 Dosyalar Açıklaması

| Dosya | Amaç |
|-------|------|
| **AndroidManifest.xml** | Uygulama yapılandırması, izinler, servisler |
| **BackgroundServices.java** | Arka plan servisleri ve Worker'lar |
| **ResourceFiles.xml** | UI stilleri, renkler, dizeleri |
| **SETUP_GUIDE.md** | Detaylı kurulum rehberi |

---

## ⚡ 5 Dakika Kurulum

### 1️⃣ Android Studio Açı
```bash
# Android Studio indir ve aç
# File → New → New Project
# Minimum SDK: API 24 (Android 7.0) seç
```

### 2️⃣ AndroidManifest.xml Kopyala
```bash
# Dosyanız:
android/app/src/main/AndroidManifest.xml
# ↓ Yapılacak düzenlemeler:
# 1. package="com.yam.chat" → kendi paket adını koy
# 2. ONESIGNAL_APP_ID_HERE → gerçek App ID
# 3. yourdomain.com → kendi domainini
```

### 3️⃣ Java Dosyalarını Oluştur
```
app/src/main/java/com/yam/chat/
├── MainActivity.java
├── receivers/
│   ├── BootReceiver.java
│   └── NetworkReceiver.java
└── services/
    ├── BackgroundSyncWorker.java
    └── ForegroundMessagingService.java
```

**Not**: `BackgroundServices.java`'daki kodu yukarıdaki dosyalara böl.

### 4️⃣ Dependencies Ekle

**app/build.gradle** (app-level):

```gradle
dependencies {
    implementation 'androidx.work:work-runtime:2.8.1'
    implementation 'com.onesignal:OneSignal:[5, 6)'
}

android {
    compileSdk 34
    defaultConfig {
        minSdk 24
        targetSdk 34
    }
}
```

### 5️⃣ OneSignal Konfigüre Et

https://onesignal.com/apps adresine git:
1. Yeni app oluştur → **Android** seç
2. Firebase projesi bağla
3. App ID ve API Key al
4. AndroidManifest.xml'e koy

---

## 🔄 24/7 Arka Planda Çalışma Mekanizması

```
┌─────────────────────────────────────────┐
│   Uygulama Açılışında (onCreate)        │
├─────────────────────────────────────────┤
│ 1. Foreground Service başlat            │
│    ↳ Bildirim çubuğunda görün           │
│    ↳ Sistem tarafından katil edilemez   │
│                                         │
│ 2. WorkManager ile zamanla (15 dk)      │
│    ↳ Otomatik mesaj senkronizasyonu    │
│    ↳ Ağ kesintisinde otomatik yeniden  │
│                                         │
│ 3. NetworkReceiver kaydet               │
│    ↳ Ağ kesintisini dinle              │
│    ↳ Bağlantı geri gelince senkronize │
└─────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────┐
│   Cihaz Açılışında (Boot Receiver)      │
├─────────────────────────────────────────┤
│ • Otomatik Foreground Service başlat    │
│ • Uygulamaya gerek olmadan açılsın      │
└─────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────┐
│   OneSignal Push Bildirimleri           │
├─────────────────────────────────────────┤
│ • Mesaj geldiğinde bildir               │
│ • Ekran kapalıyken de göster            │
│ • Tıklanınca uygulamayı aç              │
└─────────────────────────────────────────┘
```

---

## 🧪 Test Checklist

```
☐ Uygulamayı aç
☐ OneSignal API'den test bildirimi gönder
☐ Arka planda çalışıyor mu kontrol et (Settings → Apps → YAM Chat)
☐ Uygulamayı kaybdet, bildirim gelip gelmediğini kontrol et
☐ Cihazı yeniden başlat, uygulaması çalışıyor mu kontrol et
☐ WiFi kapat/aç, mesajlar senkronize mi kontrol et
☐ Aeroplane Mode açıp kapat, bağlantı geri geldiğinde senkronize mi oldu
```

---

## 🔑 Önemli Konfigürasyonlar

### 1. Package Name
```xml
<!-- AndroidManifest.xml -->
<manifest package="com.example.yamchat">
    <!-- Tüm receiver ve service sınıfları buna göre başlayacak -->
</manifest>
```

### 2. OneSignal App ID
```java
// MainActivity.java
OneSignal.setAppId("XXXXX-XXXXX-XXXXX-XXXXX");
```

### 3. WebView URL (Opsiyonel)
```java
// MainActivity.java
webView.loadUrl("https://yourdomain.com");
```

### 4. WorkManager Sıklığı
```java
// Daha sık çalışması için (pil tüketir)
15 minutes → 5 minutes

// Daha nadir çalışması için (pil tasarrufu)
15 minutes → 30 minutes
```

---

## 🚨 Sık Hatalar ve Çözümleri

### ❌ "Class not found: MainActivity"
**Çözüm**: Package name'i doğru kopyalamasız. AndroidManifest.xml'deki paket adı ile Java paket adını eşleştir.

### ❌ "OneSignal not initialized"
**Çözüm**: `OneSignal.setAppId()` MainActivity'de onCreate içinde çağır.

### ❌ "Permission denied"
**Çözüm**: 
```java
// Android 13+ için
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    requestPermissions(new String[]{
        Manifest.permission.POST_NOTIFICATIONS
    }, 1);
}
```

### ❌ "Service keeps stopping"
**Çözüm**: `START_STICKY` kullan ve bildirim ekranında görünmesini sağla (Foreground Service).

### ❌ "Bildirimler gelmiyor"
**Çözüm**:
- OneSignal app ID doğru mu?
- Firebase projesi bağlı mı?
- Bildirim izni veriliş mi?
- OneSignal panelinde device registere olmuş mu?

---

## 📊 Dosya Ağacı (Tam)

```
MyYAMChat/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml ⭐ (kopyala)
│   │   │   ├── java/com/yam/chat/
│   │   │   │   ├── MainActivity.java ⭐ (yeni)
│   │   │   │   ├── receivers/
│   │   │   │   │   ├── BootReceiver.java ⭐ (yeni)
│   │   │   │   │   └── NetworkReceiver.java ⭐ (yeni)
│   │   │   │   └── services/
│   │   │   │       ├── BackgroundSyncWorker.java ⭐ (yeni)
│   │   │   │       ├── ForegroundMessagingService.java ⭐ (yeni)
│   │   │   │       └── YAMFirebaseMessagingService.java ⭐ (yeni)
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml ⭐ (kopyala)
│   │   │       │   ├── colors.xml ⭐ (kopyala)
│   │   │       │   ├── styles.xml ⭐ (kopyala)
│   │   │       │   ├── dimens.xml ⭐ (kopyala)
│   │   │       │   └── arrays.xml (opsiyonel)
│   │   │       ├── values-night/
│   │   │       │   ├── colors.xml (Dark Mode)
│   │   │       │   └── styles.xml (Dark Mode)
│   │   │       ├── drawable/
│   │   │       │   ├── ic_launcher.xml ⭐ (kendi ikonunu koy)
│   │   │       │   └── ic_launcher_round.xml
│   │   │       ├── layout/
│   │   │       │   └── activity_main.xml (WebView ile)
│   │   │       └── font/
│   │   │           └── plus_jakarta_sans.ttf
│   │   └── test/
│   └── build.gradle ⭐ (dependencies ekle)
├── build.gradle (project-level)
├── settings.gradle
└── google-services.json ⭐ (Firebase'den indir)
```

---

## 🔐 Güvenlik Kontrol Listesi

```
☐ Debuggable = false (production'da)
☐ OneSignal App ID gizli
☐ Supabase anahtarları environment variable'da
☐ SSL/TLS zorunlu (android:usesCleartextTraffic="false")
☐ ProGuard/R8 aktif
☐ İzinler minimum seviyede
```

---

## 📦 APK Derleme

### Debug (Test)
```bash
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

### Release (İmzalı)
```bash
./gradlew assembleRelease
# → app/build/outputs/apk/release/app-release.apk
```

Cihaza kur:
```bash
adb install app-debug.apk
```

---

## 🚀 Deployment Adımları

1. **Local Test**
   - Android Studio'dan çalıştır
   - Emülatör veya cihazda test et

2. **Google Play Console**
   - https://play.google.com/console aç
   - Yeni uygulama oluştur
   - Release APK yükle
   - Beta test başlat

3. **Xiaomi/Huawei Özel İzin**
   ```
   Settings → Apps → Special access → Battery → Battery saving allowlist
   YAM Chat'ı ekle
   ```

---

## 💡 Performans İpuçları

| Ayar | İyi | Kötü |
|------|------|------|
| **WorkManager Sıklığı** | 15-30 dk | 1-5 dk |
| **Veri sıkıştırması** | Gzip açık | Açık değil |
| **Bildirim sıklığı** | Sınırlı | Sınırsız |
| **Batch işlemler** | Toplu gönder | Teker teker |

---

## 📞 Destek Kaynakları

- **Android Docs**: https://developer.android.com
- **OneSignal**: https://documentation.onesignal.com
- **Firebase**: https://firebase.google.com/docs
- **Supabase**: https://supabase.com/docs
- **WorkManager**: https://developer.android.com/guide/background

---

## ✅ Başarısı Ölçütleri

✓ Uygulama kapalı olsa bile bildirimler geliyor
✓ Cihaz açılışında otomatik başlıyor
✓ İnternet kesilince mesajlar bufferleniyor
✓ İnternet geri gelince senkronize oluyor
✓ Sistem görev yöneticisinde görünüyor
✓ Pil tüketimi makul seviyede

---

**Başarılar! 🎉**

Herhangi bir sorunla karşılaşırsan, SETUP_GUIDE.md dosyasındaki detaylı açıklamalara bak.
