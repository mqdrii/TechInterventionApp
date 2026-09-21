const express = require('express');
const router = express.Router();
const db = require('../db');
const { sendAssignmentEmail } = require('../mailer');

// Get appointments
router.get('/', (req, res) => {
  const { department, assignedUserId } = req.query;

  let query = 'SELECT * FROM appointments';
  const params = [];

  if (assignedUserId && assignedUserId > 0) {
    query += ' WHERE assigned_user_id = ?';
    params.push(assignedUserId);
  } else if (department && department.trim() && department !== 'ALL') {
    query += ' WHERE LOWER(TRIM(department)) = LOWER(TRIM(?))';
    params.push(department);
  }

  query += ' ORDER BY date DESC, time DESC';

  db.all(query, params, (err, rows) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(rows);
  });
});

// Create appointment
router.post('/', (req, res) => {
  const {
    clientId = 0,
    clientName,
    date,
    time,
    description,
    department = 'Generale',
    assignedUserId = 0,
    assignedUserName = ''
  } = req.body;

  if (!clientName || !date || !time || !description) {
    return res.status(400).json({ error: 'Tutti i campi obbligatori devono essere compilati.' });
  }

  db.run(
    `INSERT INTO appointments (client_id, client_name, date, time, description, department, assigned_user_id, assigned_user_name, status)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Programmato')`,
    [clientId, clientName.trim(), date.trim(), time.trim(), description.trim(), department.trim(), assignedUserId, assignedUserName.trim()],
    function(err) {
      if (err) return res.status(500).json({ error: err.message });

      const result = {
        id: this.lastID,
        clientId,
        clientName: clientName.trim(),
        date: date.trim(),
        time: time.trim(),
        description: description.trim(),
        department: department.trim(),
        assignedUserId,
        assignedUserName: assignedUserName.trim(),
        status: 'Programmato'
      };

      // Invia email notifica al tecnico assegnato (se presente)
      if (assignedUserId && assignedUserId > 0) {
        db.get(`SELECT email, first_name FROM users WHERE id = ?`, [assignedUserId], (uErr, user) => {
          if (!uErr && user) {
            sendAssignmentEmail(
              user.email, user.first_name,
              'Appuntamento', clientName.trim(),
              `${date.trim()} ${time.trim()}`, description.trim()
            ).catch(() => {});
          }
        });
      }

      res.status(201).json(result);
    }
  );
});

// Update appointment status
router.patch('/:id/status', (req, res) => {
  const appointmentId = req.params.id;
  const { status } = req.body;

  if (!status) {
    return res.status(400).json({ error: 'Lo stato è obbligatorio.' });
  }

  db.run(
    'UPDATE appointments SET status = ? WHERE id = ?',
    [status.trim(), appointmentId],
    function(err) {
      if (err) return res.status(500).json({ error: err.message });
      res.json({ success: true, id: appointmentId, status: status.trim() });
    }
  );
});

// Delete appointment
router.delete('/:id', (req, res) => {
  const appointmentId = req.params.id;
  db.run('DELETE FROM appointments WHERE id = ?', [appointmentId], function(err) {
    if (err) return res.status(500).json({ error: err.message });
    console.log(`[APPOINTMENTS] Appuntamento ID ${appointmentId} eliminato.`);
    res.json({ success: true, id: appointmentId, message: 'Appuntamento eliminato con successo.' });
  });
});

module.exports = router;
