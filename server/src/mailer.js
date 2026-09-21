const nodemailer = require('nodemailer');

// Transporter Gmail — credenziali da variabili ambiente Render
const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.EMAIL_USER || '',
    pass: process.env.EMAIL_PASS || ''
  }
});

/**
 * Invia OTP di verifica email
 * @param {string} toEmail - indirizzo destinatario
 * @param {string} otp - codice 6 cifre
 * @param {string} firstName - nome utente
 */
async function sendVerificationEmail(toEmail, otp, firstName) {
  if (!process.env.EMAIL_USER || !process.env.EMAIL_PASS) {
    console.warn('[MAILER] EMAIL_USER/EMAIL_PASS non configurati — email non inviata');
    return false;
  }

  const mailOptions = {
    from: `"Xelta App" <${process.env.EMAIL_USER}>`,
    to: toEmail,
    subject: 'Xelta — Codice di verifica account',
    html: `
      <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; background: #f4f5f7; padding: 32px 16px;">
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
    await transporter.sendMail(mailOptions);
    console.log(`[MAILER] OTP inviato a ${toEmail}`);
    return true;
  } catch (err) {
    console.error('[MAILER] Errore invio email:', err.message);
    return false;
  }
}

/**
 * Invia notifica di nuovo lavoro assegnato
 * @param {string} toEmail - tecnico destinatario
 * @param {string} firstName - nome tecnico
 * @param {string} tipo - 'Appuntamento' o 'Intervento'
 * @param {string} clientName - nome cliente
 * @param {string} date - data/ora
 * @param {string} description - descrizione
 */
async function sendAssignmentEmail(toEmail, firstName, tipo, clientName, date, description) {
  if (!process.env.EMAIL_USER || !process.env.EMAIL_PASS) {
    console.warn('[MAILER] EMAIL_USER/EMAIL_PASS non configurati — notifica email non inviata');
    return false;
  }

  const mailOptions = {
    from: `"Xelta App" <${process.env.EMAIL_USER}>`,
    to: toEmail,
    subject: `Xelta — Nuovo ${tipo} assegnato: ${clientName}`,
    html: `
      <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; background: #f4f5f7; padding: 32px 16px;">
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
    await transporter.sendMail(mailOptions);
    console.log(`[MAILER] Notifica ${tipo} inviata a ${toEmail}`);
    return true;
  } catch (err) {
    console.error('[MAILER] Errore invio notifica:', err.message);
    return false;
  }
}

module.exports = { sendVerificationEmail, sendAssignmentEmail };
