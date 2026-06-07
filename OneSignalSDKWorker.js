// OneSignal Service Worker
importScripts("https://cdn.onesignal.com/sdks/web/v16/OneSignalSDK.sw.js");

// Hata yönetimi
self.addEventListener('error', function(event) {
  console.error('Service Worker Error:', event.error);
});

self.addEventListener('unhandledrejection', function(event) {
  console.error('Unhandled Rejection:', event.reason);
});

// OneSignal Push Notification event listener
OneSignal = OneSignal || {};
OneSignal.push = OneSignal.push || [];

OneSignal.push(function() {
  // Notification click handler
  OneSignal.Notifications.addEventListener('click', function(event) {
    console.log('Notification clicked:', event.notification);
    event.notification.close();
  });

  // Notification display handler
  OneSignal.Notifications.addEventListener('foreground', function(event) {
    console.log('Notification displayed (foreground):', event.notification);
  });

  // Notification received when app closed
  OneSignal.Notifications.addEventListener('dismiss', function(event) {
    console.log('Notification dismissed:', event.notification);
  });
});
