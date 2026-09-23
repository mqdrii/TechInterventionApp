const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const db = require('../db');
const { sendVerificationEmail } = require('../mailer');

const JWT_SECRET = process.env.JWT_SECRET || 'tech_intervention_jwt_secret_2026';

// ─── REGISTER — con verifica email OTP ───────────────────────────────────────
router.post('/register', async (req, res) => {
  const { email, password, firstName, lastName, role, department } = req.body;

  if (!email || !password || !firstName || !lastName) {
    return res.status(400).json({ error: 'Tutti i campi obbligatori devono essere compilati.' });
  }

  // Validazione formato email
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email.trim())) {
    return res.status(400).json({ error: 'Formato email non valido.' });
  }

  if (password.length < 6) {
    return res.status(400).json({ error: 'La password deve contenere almeno 6 caratteri.' });
  }

  const assignedRole = (role === 'ADMIN') ? 'ADMIN' : 'TECHNICIAN';
  const assignedDept = (department && department.trim()) ? department.trim() : 'Generale';
  const hash = bcrypt.hashSync(password, 10);
  const emailLower = email.toLowerCase().trim();

  // Genera OTP 6 cifre
  const otp = String(Math.floor(100000 + Math.random() * 900000));
  const expiresAt = new Date(Date.now() + 10 * 60 * 1000).toISOString(); // 10 min

  db.run(
    `INSERT INTO users (email, password_hash, first_name, last_name, role, department, is_verified)
     VALUES (?, ?, ?, ?, ?, ?, 0)`,
    [emailLower, hash, firstName.trim(), lastName.trim(), assignedRole, assignedDept],
    function(err) {
      if (err) {
        if (err.message.includes('UNIQUE')) {
          return res.status(409).json({ error: 'Questa email è già registrata nel sistema.' });
        }
        return res.status(500).json({ error: err.message });
      }

      const userId = this.lastID;

      // Salva OTP
      db.run(
        `INSERT OR REPLACE INTO email_verifications (user_id, otp, expires_at) VALUES (?, ?, ?)`,
        [userId, otp, expiresAt],
        async (otpErr) => {
          if (otpErr) {
            console.error('[AUTH] Errore salvataggio OTP:', otpErr.message);
          }

          // Invia email OTP (asincrono, non blocca la risposta)
          sendVerificationEmail(emailLower, otp, firstName.trim()).catch(e =>
            console.error('[AUTH] Errore invio email verifica:', e)
          );

          const emailConfigured = Boolean(process.env.EMAIL_USER && process.env.EMAIL_PASS);
          const msg = emailConfigured
            ? `Controlla la tua email ${emailLower} — hai ricevuto un codice di verifica a 6 cifre.`
            : `Servizio email non ancora configurato su Render. Il tuo codice di verifica è: ${otp}`;

          res.status(201).json({
            requiresVerification: true,
            userId,
            devOtp: emailConfigured ? undefined : otp,
            message: msg
          });
        }
      );
    }
  );
});

// ─── VERIFY EMAIL ─────────────────────────────────────────────────────────────
router.post('/verify-email', (req, res) => {
  const { userId, otp } = req.body;

  if (!userId || !otp) {
    return res.status(400).json({ error: 'userId e otp sono obbligatori.' });
  }

  db.get(
    `SELECT * FROM email_verifications WHERE user_id = ?`,
    [userId],
    (err, row) => {
      if (err) return res.status(500).json({ error: err.message });
      if (!row) return res.status(404).json({ error: 'Nessuna verifica in attesa per questo account.' });

      if (new Date() > new Date(row.expires_at)) {
        return res.status(400).json({ error: 'Codice scaduto. Registrati di nuovo per ricevere un nuovo codice.' });
      }

      if (String(row.otp).trim() !== String(otp).trim()) {
        return res.status(400).json({ error: 'Codice non corretto. Riprova.' });
      }

      // Attiva account
      db.run(`UPDATE users SET is_verified = 1 WHERE id = ?`, [userId], (updateErr) => {
        if (updateErr) return res.status(500).json({ error: updateErr.message });

        // Elimina OTP usato
        db.run(`DELETE FROM email_verifications WHERE user_id = ?`, [userId]);

        // Recupera dati utente e genera token
        db.get(`SELECT * FROM users WHERE id = ?`, [userId], (userErr, user) => {
          if (userErr || !user) return res.status(500).json({ error: 'Errore recupero utente.' });

          const userData = {
            id: user.id,
            email: user.email,
            firstName: user.first_name,
            lastName: user.last_name,
            role: user.role,
            department: user.department
          };

          const token = jwt.sign(userData, JWT_SECRET, { expiresIn: '30d' });
          res.json({ user: userData, token, message: 'Email verificata! Benvenuto su Xelta.' });
        });
      });
    }
  );
});

// ─── RESEND OTP ───────────────────────────────────────────────────────────────
router.post('/resend-otp', (req, res) => {
  const { userId } = req.body;
  if (!userId) return res.status(400).json({ error: 'userId obbligatorio.' });

  db.get(`SELECT * FROM users WHERE id = ? AND is_verified = 0`, [userId], async (err, user) => {
    if (err) return res.status(500).json({ error: err.message });
    if (!user) return res.status(404).json({ error: 'Utente non trovato o già verificato.' });

    const otp = String(Math.floor(100000 + Math.random() * 900000));
    const expiresAt = new Date(Date.now() + 10 * 60 * 1000).toISOString();

    db.run(
      `INSERT OR REPLACE INTO email_verifications (user_id, otp, expires_at) VALUES (?, ?, ?)`,
      [userId, otp, expiresAt],
      async () => {
        await sendVerificationEmail(user.email, otp, user.first_name);
        const emailConfigured = Boolean(process.env.EMAIL_USER && process.env.EMAIL_PASS);
        const msg = emailConfigured
          ? 'Nuovo codice inviato. Controlla la tua email.'
          : `Servizio email non configurato su Render. Codice di verifica: ${otp}`;
        res.json({ message: msg, devOtp: emailConfigured ? undefined : otp });
      }
    );
  });
});

// ─── LOGIN — solo account verificati ─────────────────────────────────────────
router.post('/login', (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ error: 'Inserisci email e password.' });
  }

  db.get(
    `SELECT * FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))`,
    [email],
    (err, user) => {
      if (err) return res.status(500).json({ error: err.message });
      if (!user) return res.status(401).json({ error: 'Credenziali non valide.' });

      const valid = bcrypt.compareSync(password, user.password_hash);
      if (!valid) return res.status(401).json({ error: 'Credenziali non valide.' });

      // Controlla se verificato
      if (user.is_verified === 0) {
        db.get(`SELECT otp FROM email_verifications WHERE user_id = ?`, [user.id], (vErr, vRow) => {
          const emailConfigured = Boolean(process.env.EMAIL_USER && process.env.EMAIL_PASS);
          const devHint = (!emailConfigured && vRow) ? ` (Codice test: ${vRow.otp})` : '';
          return res.status(403).json({
            error: `Account non verificato. Controlla la tua email${devHint}.`,
            requiresVerification: true,
            userId: user.id
          });
        });
        return;
      }

      const userData = {
        id: user.id,
        email: user.email,
        firstName: user.first_name,
        lastName: user.last_name,
        role: user.role,
        department: user.department
      };

      const token = jwt.sign(userData, JWT_SECRET, { expiresIn: '30d' });
      res.json({ user: userData, token });
    }
  );
});

// ─── TECHNICIANS LIST ─────────────────────────────────────────────────────────
router.get('/technicians', (req, res) => {
  db.all(
    `SELECT id, email, first_name, last_name, role, department FROM users WHERE role = 'TECHNICIAN' AND is_verified = 1 ORDER BY first_name ASC`,
    [],
    (err, rows) => {
      if (err) return res.status(500).json({ error: err.message });
      const technicians = rows.map(r => ({
        id: r.id,
        email: r.email,
        firstName: r.first_name,
        lastName: r.last_name,
        fullName: `${r.first_name} ${r.last_name}`,
        role: r.role,
        department: r.department
      }));
      res.json(technicians);
    }
  );
});

// ─── ALL USERS LIST ───────────────────────────────────────────────────────────
router.get('/users', (req, res) => {
  db.all(
    `SELECT id, email, first_name, last_name, role, department FROM users WHERE is_verified = 1 ORDER BY role ASC, first_name ASC`,
    [],
    (err, rows) => {
      if (err) return res.status(500).json({ error: err.message });
      const users = rows.map(r => ({
        id: r.id,
        email: r.email,
        firstName: r.first_name,
        lastName: r.last_name,
        fullName: `${r.first_name} ${r.last_name}`,
        role: r.role,
        department: r.department
      }));
      res.json(users);
    }
  );
});

// ─── UPDATE USER (Personalizza Account) ───────────────────────────────────────
router.put('/users/:id', (req, res) => {
  const { id } = req.params;
  const { firstName, lastName, department, role, password } = req.body;

  if (!id) return res.status(400).json({ error: 'ID utente mancante.' });

  db.get(`SELECT * FROM users WHERE id = ?`, [id], (err, user) => {
    if (err) return res.status(500).json({ error: err.message });
    if (!user) return res.status(404).json({ error: 'Utente non trovato.' });

    const newFirstName = (firstName && firstName.trim()) ? firstName.trim() : user.first_name;
    const newLastName = (lastName && lastName.trim()) ? lastName.trim() : user.last_name;
    const newDept = (department && department.trim()) ? department.trim() : user.department;
    const newRole = (role && role.trim()) ? role.trim().toUpperCase() : user.role;
    let newHash = user.password_hash;
    if (password && password.trim().length >= 6) {
      newHash = bcrypt.hashSync(password.trim(), 10);
    }

    db.run(
      `UPDATE users SET first_name = ?, last_name = ?, department = ?, role = ?, password_hash = ? WHERE id = ?`,
      [newFirstName, newLastName, newDept, newRole, newHash, id],
      function(updateErr) {
        if (updateErr) return res.status(500).json({ error: updateErr.message });

        const updatedUser = {
          id: user.id,
          email: user.email,
          firstName: newFirstName,
          lastName: newLastName,
          fullName: `${newFirstName} ${newLastName}`,
          role: newRole,
          department: newDept
        };
        console.log(`[AUTH] Account ID ${id} (${updatedUser.fullName}) aggiornato.`);
        res.json({ success: true, user: updatedUser });
      }
    );
  });
});

// ─── DELETE USER ──────────────────────────────────────────────────────────────
router.delete('/users/:id', (req, res) => {
  const { id } = req.params;
  if (!id) return res.status(400).json({ error: 'ID utente mancante.' });

  db.run(`DELETE FROM users WHERE id = ? OR LOWER(email) = LOWER(?)`, [id, id], function(err) {
    if (err) return res.status(500).json({ error: err.message });
    if (this.changes === 0) return res.status(404).json({ error: 'Utente non trovato.' });
    console.log(`[AUTH] Account ID/Email ${id} eliminato.`);
    res.json({ success: true, message: 'Account eliminato con successo.' });
  });
});

module.exports = router;
