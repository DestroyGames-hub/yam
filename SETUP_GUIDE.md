# OneSignal Push Notifications - Sorun Giderme Kılavuzu

## 🔴 Teşhis Edilen Sorunlar

### 1. **manifest.json'da gcm_sender_id eksik**
- Chrome/Firefox offline modda push almak için GCM sender ID gerekli
- **Çözüm:** `manifest.json`'a `"gcm_sender_id": "103953800507"` satırını ekledik

### 2. **Service Worker hata yönetimi yetersiz**
- `OneSignalSDKWorker.js` basit import scriptiydi
- Hata oluştuğunda sessizce başarısız oluyordu
- **Çözüm:** Event listeners ve error handling eklendi

### 3. **Push izni otomatik talep edilmiyor**
- Uygulama ilk açıldığında push izni sorulmalı
- **Çözüm:** `promptOptions` ve `requestPermission()` eklendi

---

## 📋 Uygulanması Gereken Adımlar

### **Adım 1: Dosyaları Güncelle**

1. `manifest.json` dosyasını kök dizine koyun (public folder)
   ```
   public/manifest.json
   ```

2. `OneSignalSDKWorker.js` dosyasını kök dizine koyun
   ```
   public/OneSignalSDKWorker.js
   ```

### **Adım 2: HTML'de OneSignal Script'ini Güncelle**

**HEAD bölümünde:**
```html
<script src="https://cdn.onesignal.com/sdks/web/v16/OneSignalSDK.page.js" defer></script>
<link rel="manifest" href="/manifest.json">
```

**BODY sonunda (closing tag'den önce):**
Sağlanan `OneSignal-Config.js` dosyasındaki kodları `<script>` tagına alarak ekleyin.

### **Adım 3: OneSignal Dashboard'da Ayarlar**

1. [OneSignal Dashboard](https://app.onesignal.com)
2. Uygulamanızı seçin
3. **Settings → Configuration**
4. **Web Push → Configurator**
5. "Chrome & Edge" sectionda HTTPS site URL'nizi girin
6. Android credentials (GCM) eklenmiş olduğundan emin olun

---

## ✅ Test Etme

### **1. Uygulamayı açtığınızda:**
- "Bildirim almasına izin ver?" promptu görülmeli
- Tarayıcı console'da `✅ OneSignal başarıyla başlatıldı` yazmalı

### **2. Uygulamayı kapatıp test push gönderin:**
- OneSignal Dashboard'dan test notification gönderin
- Tarayıcı kapalı olsa bile notification görülmeli
- (Chrome/Edge Settings → Apps → Notifications'da kilidi açık olduğundan emin olun)

### **3. Console kontrolleri:**
```javascript
// Tarayıcı console'da:
window.oneSignalLogin('testuser', 'test@example.com');
window.requestPushPermission();

// Player ID'yi görüntüle:
OneSignal.User.PushSubscription.id
```

---

## 🛠️ Hata Ayıklama

### **Problem: Push izni sorulmuyor**
**Çözüm:**
```javascript
// Console'da çalıştırın:
OneSignal.Notifications.requestPermission();
```

### **Problem: Notification alınmıyor (app kapalı)**
1. Browser'ı tamamen kapat (arka planda çalışmayan)
2. OneSignal Dashboard'dan test notification gönder
3. Tarayıcı (Chrome) açılırken tray'i kontrol et

### **Problem: "Service Worker Registration Failed"**
1. `OneSignalSDKWorker.js` dosyasının public folderda olup olmadığını kontrol et
2. `serviceWorkerPath: 'OneSignalSDKWorker.js'` yolunun doğru olduğundan emin ol
3. Network tab'ında 404 hatası varsa path yanlış demektir

### **Problem: OneSignal undefined**
- SDK yüklenmesini bekle:
```javascript
if (typeof OneSignal === 'undefined') {
  console.log('OneSignal SDK henüz yüklenmedi');
}
```

---

## 📊 OneSignal Yapılandırması Kontrol Listesi

- [ ] `manifest.json` public folderda
- [ ] `gcm_sender_id` manifest.json'da mevcut
- [ ] `OneSignalSDKWorker.js` public folderda
- [ ] HTML'de SDK script yüklü (`defer` ile)
- [ ] OneSignal init() HTTPS/localhost üzerinde çalışıyor
- [ ] OneSignal Dashboard'da Web Push kurulu
- [ ] Push izni prompt'u gösterilip kaydediliyor
- [ ] Service Worker registration başarılı

---

## 📝 Uygulama Kapalıyken Push Alabilmek İçin

Push notifications sadece 2 şekilde çalışır:
1. **Uygulama açıkken**: Foreground listener tetiklenir
2. **Uygulama kapılıyken**: Service Worker ve Push API temel alınır

**Uygulama tamamen kapalıyken push almak için:**
- ✅ Service Worker kayıtlı olmalı
- ✅ Browser'ın push permission vermiş olması gerekir
- ✅ gcm_sender_id manifest.json'da olmalı
- ✅ OneSignal backend push event göndermelidir

---

## 🚀 İleri Ayarlar (Opsiyonel)

### **Custom Notification Handling (app kapalı)**
```javascript
OneSignal.push(function() {
  OneSignal.Notifications.addEventListener('click', function(event) {
    // Notification tıklandığında yapılacaklar
    console.log('User clicked notification:', event.notification);
  });
});
```

### **User Login sonrasında Push ID kayıt**
```javascript
async function loginAndSavePlayerId(email, username) {
  await oneSignalLogin(username, email);
  
  // 1 saniye bekle
  await new Promise(r => setTimeout(r, 1000));
  
  const playerId = await OneSignal.User.PushSubscription.id;
  console.log('Player ID:', playerId);
  
  // Supabase'e kaydet
  const {error} = await db.from('profiles')
    .update({onesignal_player_id: playerId})
    .eq('id', myUser.id);
}
```

---

## 📚 Kaynaklar

- [OneSignal Web SDK Docs](https://documentation.onesignal.com/docs/web-sdk)
- [Web Push API](https://developer.mozilla.org/en-US/docs/Web/API/Push_API)
- [Service Worker](https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API)
