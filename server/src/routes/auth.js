const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const db = require('../db');

const JWT_SECRET = process.env.JWT_SECRET || 'tech_intervention_jwt_secret_2026';

// Register
router.post('/register', (req, res) => {
  const { email, password, firstName, lastName, role, department } = req.body;

  if (!email || !password || !firstName || !lastName) {
    return res.status(400).json({ error: 'Tutti i campi obbligatori devono essere compilati.' });
  }

  const assignedRole = (role === 'ADMIN') ? 'ADMIN' : 'TECHNICIAN';
  const assignedDept = (department && department.trim()) ? department.trim() : 'Generale';
  const hash = bcrypt.hashSync(password, 10);

  db.run(
    `INSERT INTO users (email, password_hash, first_name, last_name, role, department)
     VALUES (?, ?, ?, ?, ?, ?)`,
    [email.toLowerCase().trim(), hash, firstName.trim(), lastName.trim(), assignedRole, assignedDept],
    function(err) {
      if (err) {
        if (err.message.includes('UNIQUE')) {
          return res.status(409).json({ error: 'Questa email è già registrata nel sistema.' });
        }
        return res.status(500).json({ error: err.message });
      }

      const user = {
        id: this.lastID,
        email: email.toLowerCase().trim(),
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        role: assignedRole,
        department: assignedDept
      };

      const token = jwt.sign(user, JWT_SECRET, { expiresIn: '30d' });
      res.status(201).json({ user, token });
    }
  );
});

// Login
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

// Technicians list
router.get('/technicians', (req, res) => {
  db.all(
    `SELECT id, email, first_name, last_name, role, department FROM users WHERE role = 'TECHNICIAN' ORDER BY first_name ASC`,
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

module.exports = router;
