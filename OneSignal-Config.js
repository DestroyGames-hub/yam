// OneSignal SDK yapılandırması
const ONESIGNAL_APP_ID = 'db2947e6-c67f-4d18-b2fd-cf533daf7b68';

window.OneSignalDeferred = window.OneSignalDeferred || [];

function initOneSignal() {
  OneSignalDeferred.push(async function(OneSignal) {
    try {
      await OneSignal.init({
        appId: ONESIGNAL_APP_ID,
        serviceWorkerParam: { scope: '/' },
        serviceWorkerPath: 'OneSignalSDKWorker.js',
        notifyButton: { enable: false },
        allowLocalhostAsSecureOrigin: true,
        // ÖNEMLI: Uygulama kapalı iken push almak için
        promptOptions: {
          slidedown: {
            enabled: true,
            autoPrompt: true,
            timeDelay: 5
          }
        }
      });

      // Push izni otomatik iste (ilk kez ziyaret)
      await OneSignal.Notifications.requestPermission();

      console.log('✅ OneSignal başarıyla başlatıldı');

      // Event listeners
      OneSignal.Notifications.addEventListener('click', function(event) {
        console.log('📬 Notification tıklandı:', event.notification.data);
      });

      OneSignal.Notifications.addEventListener('foreground', function(event) {
        console.log('📢 Notification gösterildi (ön planda):', event.notification.data);
      });

      window.oneSignalLogin = async function(username, email) {
        try {
          await OneSignal.login(email);
          await OneSignal.User.addEmail(email);
          await OneSignal.User.addTag('username', username);
          console.log('✅ OneSignal: kullanıcı bağlandı ->', username);
        } catch(e) {
          console.error('❌ OneSignal login hatası:', e);
        }
      };

      window.oneSignalLogout = async function() {
        try { 
          await OneSignal.logout(); 
          console.log('✅ OneSignal logout yapıldı');
        } catch(e) {
          console.error('❌ OneSignal logout hatası:', e);
        }
      };

      window.requestPushPermission = async function() {
        try {
          const permission = await OneSignal.Notifications.requestPermission();
          console.log('✅ Push izni alındı:', permission);
          return permission;
        } catch(e) {
          console.error('❌ Push izin hatası:', e);
          return false;
        }
      };

      // Push Subscription ID'yi al
      setTimeout(async () => {
        try {
          const subscription = OneSignal.User.PushSubscription;
          console.log('📱 Push Subscription:', subscription);
        } catch(e) {
          console.error('Subscription hatası:', e);
        }
      }, 3000);

    } catch(e) {
      console.error('❌ OneSignal initialization hatası:', e);
    }
  });
}

// Sayfa yüklendiğinde başlat
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initOneSignal);
} else {
  initOneSignal();
}
