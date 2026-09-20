const express = require('express');
const router = express.Router();
const db = require('../db');

// Get all clients
router.get('/', (req, res) => {
  db.all('SELECT * FROM clients ORDER BY name ASC', [], (err, rows) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(rows);
  });
});

// Create client
router.post('/', (req, res) => {
  const { name, phone = '', address = '', email = '', notes = '' } = req.body;

  if (!name || !name.trim()) {
    return res.status(400).json({ error: 'Il nome del cliente è obbligatorio.' });
  }

  db.run(
    `INSERT INTO clients (name, phone, address, email, notes) VALUES (?, ?, ?, ?, ?)`,
    [name.trim(), phone.trim(), address.trim(), email.trim(), notes.trim()],
    function(err) {
      if (err) return res.status(500).json({ error: err.message });
      res.status(201).json({
        id: this.lastID,
        name: name.trim(),
        phone: phone.trim(),
        address: address.trim(),
        email: email.trim(),
        notes: notes.trim()
      });
    }
  );
});

// Delete client
router.delete('/:id', (req, res) => {
  const clientId = req.params.id;
  db.run('DELETE FROM clients WHERE id = ?', [clientId], function(err) {
    if (err) return res.status(500).json({ error: err.message });
    res.json({ success: true, deletedId: clientId });
  });
});

module.exports = router;
