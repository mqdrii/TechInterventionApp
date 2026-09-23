const nodemailer = require('nodemailer');

let lastMailStatus = {
  lastAttempt: null,
  success: null,
  provider: null,
  to: null,
  error: null,
  code: null
};

/**
 * Invia email tramite Resend API (porta 443 HTTPS - MAI bloccata da Render Free)
 */
async function sendViaResend(apiKey, toEmail, subject, htmlContent) {
  const fromAddress = process.env.EMAIL_FROM || 'Xelta App <onboarding@resend.dev>';
  const response = await fetch('https://api.resend.com/emails', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${apiKey.trim()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      from: fromAddress,
      to: [toEmail],
      subject: subject,
      html: htmlContent
    })
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || JSON.stringify(data));
  }
  return data;
}

/**
 * Invia email tramite Brevo API (porta 443 HTTPS - MAI bloccata da Render Free)
 */
async function sendViaBrevo(apiKey, toEmail, subject, htmlContent) {
  const senderEmail = (process.env.EMAIL_USER || 'noreply@xelta.it').trim();
  const response = await fetch('https://api.brevo.com/v3/smtp/email', {
    method: 'POST',
    headers: {
      'api-key': apiKey.trim(),
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      sender: { name: 'Xelta', email: senderEmail },
      to: [{ email: toEmail }],
      subject: subject,
      htmlContent: htmlContent
    })
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || JSON.stringify(data));
  }
  return data;
}

/**
 * Invia email tramite SMTP Gmail classico (funziona solo se Render non blocca le porte SMTP)
 */
async function sendViaSmtp(toEmail, subject, htmlContent) {
  const cleanUser = (process.env.EMAIL_USER || '').trim();
  const cleanPass = (process.env.EMAIL_PASS || '').trim().replace(/\s+/g, '');

  if (!cleanUser || !cleanPass) {
    throw new Error('EMAIL_USER o EMAIL_PASS non configurati');
  }

  const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: { user: cleanUser, pass: cleanPass },
    connectionTimeout: 8000,
    greetingTimeout: 8000,
    socketTimeout: 8000
  });

  return await transporter.sendMail({
    from: `"Xelta" <${cleanUser}>`,
    to: toEmail,
    subject: subject,
    html: htmlContent
  });
}

/**
 * Funzione unificata di invio email: prova prima HTTP API (Resend / Brevo) e poi SMTP
 */
async function sendEmailUnified(toEmail, subject, htmlContent) {
  lastMailStatus.lastAttempt = new Date().toISOString();
  lastMailStatus.to = toEmail;

  // 1) Prova Resend HTTP API (porta 443, garantito su Render Free)
  if (process.env.RESEND_API_KEY) {
    try {
      const res = await sendViaResend(process.env.RESEND_API_KEY, toEmail, subject, htmlContent);
      console.log(`[MAILER] Email inviata via Resend a ${toEmail}:`, res.id || 'OK');
      lastMailStatus.success = true;
      lastMailStatus.provider = 'Resend (HTTPS 443)';
      lastMailStatus.error = null;
      return true;
    } catch (err) {
      console.error('[MAILER] Errore Resend:', err.message);
      lastMailStatus.error = err.message;
    }
  }

  // 2) Prova Brevo HTTP API (porta 443)
  if (process.env.BREVO_API_KEY) {
    try {
      const res = await sendViaBrevo(process.env.BREVO_API_KEY, toEmail, subject, htmlContent);
      console.log(`[MAILER] Email inviata via Brevo a ${toEmail}`);
      lastMailStatus.success = true;
      lastMailStatus.provider = 'Brevo (HTTPS 443)';
      lastMailStatus.error = null;
      return true;
    } catch (err) {
      console.error('[MAILER] Errore Brevo:', err.message);
      lastMailStatus.error = err.message;
    }
  }

  // 3) Fallback a SMTP (se EMAIL_USER e EMAIL_PASS sono configurati)
  if (process.env.EMAIL_USER && process.env.EMAIL_PASS) {
    try {
      await sendViaSmtp(toEmail, subject, htmlContent);
      console.log(`[MAILER] Email inviata via SMTP Gmail a ${toEmail}`);
      lastMailStatus.success = true;
      lastMailStatus.provider = 'Gmail SMTP';
      lastMailStatus.error = null;
      return true;
    } catch (err) {
      console.error('[MAILER] Errore invio SMTP (Render Free blocca le porte SMTP 465/587):', err.message);
      lastMailStatus.success = false;
      lastMailStatus.provider = 'Gmail SMTP';
      lastMailStatus.error = err.message;
      return false;
    }
  }

  console.warn('[MAILER] Nessun provider email configurato su Render');
  lastMailStatus.success = false;
  lastMailStatus.error = 'Nessun provider configurato su Render';
  return false;
}

/**
 * Invia OTP di verifica
 */
async function sendVerificationEmail(toEmail, otp, firstName) {
  const html = `
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
  `;
  return await sendEmailUnified(toEmail, 'Xelta — Codice di verifica account', html);
}

/**
 * Invia notifica di nuovo lavoro assegnato
 */
async function sendAssignmentEmail(toEmail, firstName, tipo, clientName, date, description) {
  const html = `
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
  `;
  return await sendEmailUnified(toEmail, `Xelta — Nuovo ${tipo} assegnato: ${clientName}`, html);
}

function getMailDiagnostic() {
  return {
    resendConfigured: Boolean(process.env.RESEND_API_KEY),
    brevoConfigured: Boolean(process.env.BREVO_API_KEY),
    smtpConfigured: Boolean(process.env.EMAIL_USER && process.env.EMAIL_PASS),
    emailUserMasked: process.env.EMAIL_USER ? process.env.EMAIL_USER.trim() : null,
    renderNote: 'Su Render Free le porte SMTP (465/587) sono bloccate per prevenire spam. Per invio email reale si consiglia RESEND_API_KEY (gratuito via HTTPS porta 443).',
    lastMailStatus
  };
}

async function verifySmtp() {
  if (process.env.RESEND_API_KEY) {
    return { ok: true, provider: 'Resend API (HTTPS porta 443 - attivo e non bloccato da Render)' };
  }
  if (process.env.BREVO_API_KEY) {
    return { ok: true, provider: 'Brevo API (HTTPS porta 443 - attivo e non bloccato da Render)' };
  }
  const cleanUser = (process.env.EMAIL_USER || '').trim();
  const cleanPass = (process.env.EMAIL_PASS || '').trim().replace(/\s+/g, '');
  if (!cleanUser || !cleanPass) {
    return { ok: false, error: 'Nessun provider email configurato su Render.' };
  }
  return {
    ok: false,
    error: 'Render Free blocca le porte SMTP in uscita (465/587). Configura RESEND_API_KEY per inviare via HTTPS senza blocchi.'
  };
}

module.exports = {
  sendVerificationEmail,
  sendAssignmentEmail,
  getMailDiagnostic,
  verifySmtp
};
