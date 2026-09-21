const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const bcrypt = require('bcryptjs');

const dbPath = path.resolve(__dirname, '../data.db');
const db = new sqlite3.Database(dbPath);

db.serialize(() => {
  db.run('PRAGMA foreign_keys = ON;');

  // Users Table
  db.run(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      email TEXT UNIQUE NOT NULL,
      password_hash TEXT NOT NULL,
      first_name TEXT NOT NULL,
      last_name TEXT NOT NULL,
      role TEXT NOT NULL DEFAULT 'TECHNICIAN',
      department TEXT NOT NULL DEFAULT 'Generale',
      is_verified INTEGER NOT NULL DEFAULT 1,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Migrazione sicura: aggiunge is_verified se manca su DB esistente
  db.run(`ALTER TABLE users ADD COLUMN is_verified INTEGER NOT NULL DEFAULT 1`, () => {});

  // Email Verifications Table (OTP 6 cifre)
  db.run(`
    CREATE TABLE IF NOT EXISTS email_verifications (
      user_id INTEGER PRIMARY KEY,
      otp TEXT NOT NULL,
      expires_at TEXT NOT NULL,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    )
  `);

  // Clients Table
  db.run(`
    CREATE TABLE IF NOT EXISTS clients (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      phone TEXT DEFAULT '',
      address TEXT DEFAULT '',
      email TEXT DEFAULT '',
      notes TEXT DEFAULT '',
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Appointments Table
  db.run(`
    CREATE TABLE IF NOT EXISTS appointments (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      client_id INTEGER DEFAULT 0,
      client_name TEXT NOT NULL,
      date TEXT NOT NULL,
      time TEXT NOT NULL,
      description TEXT NOT NULL,
      department TEXT NOT NULL DEFAULT 'Generale',
      assigned_user_id INTEGER DEFAULT 0,
      assigned_user_name TEXT DEFAULT '',
      status TEXT NOT NULL DEFAULT 'Programmato',
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Interventions Table
  db.run(`
    CREATE TABLE IF NOT EXISTS interventions (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      client_id INTEGER DEFAULT 0,
      client_name TEXT NOT NULL,
      date TEXT NOT NULL,
      description TEXT NOT NULL,
      department TEXT NOT NULL DEFAULT 'Generale',
      technician_id INTEGER DEFAULT 0,
      technician_name TEXT DEFAULT '',
      notes TEXT DEFAULT '',
      status TEXT NOT NULL DEFAULT 'In corso',
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Seed default admin (pre-verificato, non richiede email)
  db.get('SELECT COUNT(*) AS count FROM users', (err, row) => {
    if (!err && row && row.count === 0) {
      const hash = bcrypt.hashSync('password123', 10);
      db.run(
        `INSERT INTO users (email, password_hash, first_name, last_name, role, department, is_verified)
         VALUES (?, ?, ?, ?, ?, ?, 1)`,
        ['andrea@test.it', hash, 'Andrea', 'Admin', 'ADMIN', 'Direzione']
      );
      console.log('Seeded default admin: andrea@test.it / password123');
    }
  });
});

module.exports = db;
