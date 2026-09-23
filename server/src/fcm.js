const admin = require('firebase-admin');
const db = require('./db');

let firebaseInitialized = false;

function initFirebase() {
  if (firebaseInitialized) return true;

  try {
    // 1) Cerca file json locale
    const path = require('path');
    const fs = require('fs');
    const localKeyPath = path.resolve(__dirname, '../../xelta-app-81e4e-firebase-adminsdk-fbsvc-6f6557b118.json');

    if (process.env.FIREBASE_SERVICE_ACCOUNT) {
      const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
      });
      firebaseInitialized = true;
      console.log('[FCM] Firebase Admin inizializzato da variabile FIREBASE_SERVICE_ACCOUNT');
      return true;
    } else if (fs.existsSync(localKeyPath)) {
      const serviceAccount = require(localKeyPath);
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
      });
      firebaseInitialized = true;
      console.log('[FCM] Firebase Admin inizializzato da file service account locale');
      return true;
    } else {
      console.warn('[FCM] Nessuna configurazione Firebase Admin trovata (FIREBASE_SERVICE_ACCOUNT non impostato)');
      return false;
    }
  } catch (err) {
    console.error('[FCM] Errore inizializzazione Firebase Admin:', err.message);
    return false;
  }
}

/**
 * Invia notifica push FCM a un singolo utente o a tutti i tecnici di un reparto
 */
async function sendPushNotification({ userId, department, title, body, data = {} }) {
  if (!initFirebase()) {
    console.warn('[FCM] Push non inviato: Firebase non inizializzato');
    return;
  }

  let tokens = [];

  if (userId && userId > 0) {
    // Cerca token dell'utente specifico
    tokens = await new Promise((resolve) => {
      db.get('SELECT fcm_token FROM users WHERE id = ?', [userId], (err, row) => {
        if (err || !row || !row.fcm_token) resolve([]);
        else resolve([row.fcm_token]);
      });
    });
  } else if (department) {
    // Cerca token di tutti i tecnici del reparto
    tokens = await new Promise((resolve) => {
      db.all(
        'SELECT fcm_token FROM users WHERE (department = ? OR role = "ADMIN") AND fcm_token IS NOT NULL',
        [department],
        (err, rows) => {
          if (err || !rows) resolve([]);
          else resolve(rows.map(r => r.fcm_token).filter(Boolean));
        }
      );
    });
  }

  if (tokens.length === 0) {
    console.log('[FCM] Nessun token FCM registrato per il destinatario (userId:', userId, 'dept:', department, ')');
    return;
  }

  const payload = {
    notification: {
      title: title,
      body: body
    },
    data: {
      ...data,
      title: title,
      body: body
    },
    android: {
      priority: 'high',
      notification: {
        channelId: 'xelta_assignments_channel',
        priority: 'max',
        defaultSound: true,
        defaultVibrateTimings: true
      }
    }
  };

  for (const token of tokens) {
    try {
      const res = await admin.messaging().send({
        ...payload,
        token: token
      });
      console.log(`[FCM] Notifica push inviata con successo a token ${token.substring(0, 15)}...: ID ${res}`);
    } catch (err) {
      console.error('[FCM] Errore invio notifica a token:', err.message);
    }
  }
}

module.exports = {
  initFirebase,
  sendPushNotification
};
