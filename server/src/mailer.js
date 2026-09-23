const nodemailer = require('nodemailer');

let lastMailStatus = {
  lastAttempt: null,
  success: null,
  to: null,
  error: null,
  code: null,
  response: null
};

function getTransporter() {
  const user = (process.env.EMAIL_USER || '').trim();
  const pass = (process.env.EMAIL_PASS || '').trim().replace(/\s+/g, '');
  return nodemailer.createTransport({
    service: 'gmail',
    auth: { user, pass }
  });
}

function getMailDiagnostic() {
  const rawUser = process.env.EMAIL_USER || '';
  const rawPass = process.env.EMAIL_PASS || '';
  const cleanUser = rawUser.trim();
  const cleanPass = rawPass.trim().replace(/\s+/g, '');

  return {
    emailUserConfigured: Boolean(cleanUser),
    emailUserMasked: cleanUser
      ? cleanUser.replace(/^(.{2})(.*)(@.*)$/, (_, a, b, c) => a + '*'.repeat(Math.max(1, Math.min(b.length, 6))) + c)
      : null,
    emailPassConfigured: Boolean(cleanPass),
    emailPassLength: cleanPass.length,
    rawPassLength: rawPass.length,
    hadSpacesRemoved: rawPass.length !== cleanPass.length,
    lastMailStatus
  };
}

async function verifySmtp() {
  const cleanUser = (process.env.EMAIL_USER || '').trim();
  const cleanPass = (process.env.EMAIL_PASS || '').trim().replace(/\s+/g, '');

  if (!cleanUser || !cleanPass) {
    return {
      ok: false,
      error: 'EMAIL_USER o EMAIL_PASS non configurati su Render (Environment Variables).'
    };
  }

  try {
    const transporter = getTransporter();
    await transporter.verify();
    return { ok: true, message: 'Autenticazione SMTP con Gmail avvenuta con SUCCESSO!' };
  } catch (err) {
    return {
      ok: false,
      error: err.message,
      code: err.code || 'UNKNOWN',
      response: err.response || null,
      responseCode: err.responseCode || null
    };
  }
}

/**
 * Invia OTP di verifica email
 */
async function sendVerificationEmail(toEmail, otp, firstName) {
  const cleanUser = (process.env.EMAIL_USER || '').trim();
  const cleanPass = (process.env.EMAIL_PASS || '').trim().replace(/\s+/g, '');

  lastMailStatus.lastAttempt = new Date().toISOString();
  lastMailStatus.to = toEmail;

  if (!cleanUser || !cleanPass) {
    console.warn('[MAILER] EMAIL_USER o EMAIL_PASS non configurati — email NON inviata');
    lastMailStatus.success = false;
    lastMailStatus.error = 'EMAIL_USER/EMAIL_PASS mancanti su Render';
    return false;
  }

  const transporter = getTransporter();

  const mailOptions = {
    from: `"Xelta" <${cleanUser}>`,
    to: toEmail,
    subject: 'Xelta — Codice di verifica account',
    html: `
      <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 480px; margin: 0 auto; background: #f4f5f7; padding: 32px 16px;">
        <div style="background: #1f1f1f; border-radius: 16px; padding: 28px 32px; text-align: center; margin-bottom: 24px;">
          <h1 style="color: #ffffff; font-size: 24px; margin: 0; letter-spacing: -0.5px;">Xelta</h1>
          <p style="color: #ffffff80; font-size: 13px; margin: 4px 0 0;">Gestione Interventi e Appuntamenti</p>
        </div>
        <div style="background: #ffffff; border-radius: 16px; padding: 28px 32px;">
          <p style="color: #1f1f1f; font-size: 16px; margin: 0 0 8px;">Ciao <strong>${firstName}</strong>,</p>
          <p style="color: #1f1f1f99; font-size: 14px; margin: 0 0 24px;">Usa questo codice per verificare il tuo account Xelta. Scade tra <strong>10 minuti</strong>.</p>
          <div style="background: #FCF0F0; border-radius: 12px; padding: 20px; text-align: center; margin-bottom: 24px;">
            <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #E24C4A;">${otp}</span>
          </div>
          <p style="color: #1f1f1f59; font-size: 12px; margin: 0;">Se non hai richiesto questo codice, ignora questa email.</p>
        </div>
      </div>
    `
  };

  try {
    const info = await transporter.sendMail(mailOptions);
    console.log(`[MAILER] OTP inviato con successo a ${toEmail} (MessageId: ${info.messageId})`);
    lastMailStatus.success = true;
    lastMailStatus.error = null;
    lastMailStatus.response = info.response;
    return true;
  } catch (err) {
    console.error(`[MAILER] Errore invio email a ${toEmail}:`, err.message);
    lastMailStatus.success = false;
    lastMailStatus.error = err.message;
    lastMailStatus.code = err.code;
    lastMailStatus.response = err.response;
    return false;
  }
}

/**
 * Invia notifica di nuovo lavoro assegnato
 */
async function sendAssignmentEmail(toEmail, firstName, tipo, clientName, date, description) {
  const cleanUser = (process.env.EMAIL_USER || '').trim();
  const cleanPass = (process.env.EMAIL_PASS || '').trim().replace(/\s+/g, '');

  if (!cleanUser || !cleanPass) {
    console.warn('[MAILER] EMAIL_USER/EMAIL_PASS non configurati — notifica email non inviata');
    return false;
  }

  const transporter = getTransporter();

  const mailOptions = {
    from: `"Xelta" <${cleanUser}>`,
    to: toEmail,
    subject: `Xelta — Nuovo ${tipo} assegnato: ${clientName}`,
    html: `
      <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 480px; margin: 0 auto; background: #f4f5f7; padding: 32px 16px;">
        <div style="background: #1f1f1f; border-radius: 16px; padding: 28px 32px; text-align: center; margin-bottom: 24px;">
          <h1 style="color: #ffffff; font-size: 24px; margin: 0;">Xelta</h1>
        </div>
        <div style="background: #ffffff; border-radius: 16px; padding: 28px 32px;">
          <p style="color: #1f1f1f; font-size: 16px; margin: 0 0 8px;">Ciao <strong>${firstName}</strong>,</p>
          <p style="color: #1f1f1f99; font-size: 14px; margin: 0 0 20px;">Ti è stato assegnato un nuovo <strong>${tipo}</strong>:</p>
          <div style="background: #f4f5f7; border-radius: 12px; padding: 16px 20px; margin-bottom: 20px; border-left: 4px solid #E24C4A;">
            <p style="margin: 0 0 6px; font-size: 15px; font-weight: bold; color: #1f1f1f;">${clientName}</p>
            <p style="margin: 0 0 4px; font-size: 13px; color: #1f1f1f80;">📅 ${date}</p>
            <p style="margin: 0; font-size: 13px; color: #1f1f1f80;">📝 ${description}</p>
          </div>
          <p style="color: #1f1f1f59; font-size: 12px; margin: 0;">Apri l'app Xelta per vedere i dettagli.</p>
        </div>
      </div>
    `
  };

  try {
    const info = await transporter.sendMail(mailOptions);
    console.log(`[MAILER] Notifica ${tipo} inviata a ${toEmail} (MessageId: ${info.messageId})`);
    return true;
  } catch (err) {
    console.error(`[MAILER] Errore invio notifica ${tipo} a ${toEmail}:`, err.message);
    return false;
  }
}

module.exports = {
  sendVerificationEmail,
  sendAssignmentEmail,
  verifySmtp,
  getMailDiagnostic
};
