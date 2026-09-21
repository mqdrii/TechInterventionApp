const express = require('express');
const router = express.Router();
const db = require('../db');
const { sendAssignmentEmail } = require('../mailer');

// Get interventions
router.get('/', (req, res) => {
  const { department, technicianId } = req.query;

  let query = 'SELECT * FROM interventions';
  const params = [];

  if (technicianId && technicianId > 0) {
    query += ' WHERE technician_id = ?';
    params.push(technicianId);
  } else if (department && department.trim() && department !== 'ALL') {
    query += ' WHERE LOWER(TRIM(department)) = LOWER(TRIM(?))';
    params.push(department);
  }

  query += ' ORDER BY date DESC, id DESC';

  db.all(query, params, (err, rows) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(rows);
  });
});

// Create intervention
router.post('/', (req, res) => {
  const {
    clientId = 0,
    clientName,
    date,
    description,
    department = 'Generale',
    technicianId = 0,
    technicianName = '',
    notes = ''
  } = req.body;

  if (!clientName || !date || !description) {
    return res.status(400).json({ error: 'Tutti i campi obbligatori devono essere compilati.' });
  }

  db.run(
    `INSERT INTO interventions (client_id, client_name, date, description, department, technician_id, technician_name, notes, status)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'In corso')`,
    [clientId, clientName.trim(), date.trim(), description.trim(), department.trim(), technicianId, technicianName.trim(), notes.trim()],
    function(err) {
      if (err) return res.status(500).json({ error: err.message });

      const result = {
        id: this.lastID,
        clientId,
        clientName: clientName.trim(),
        date: date.trim(),
        description: description.trim(),
        department: department.trim(),
        technicianId,
        technicianName: technicianName.trim(),
        notes: notes.trim(),
        status: 'In corso'
      };

      // Invia email notifica al tecnico assegnato (se presente)
      if (technicianId && technicianId > 0) {
        db.get(`SELECT email, first_name FROM users WHERE id = ?`, [technicianId], (uErr, user) => {
          if (!uErr && user) {
            sendAssignmentEmail(
              user.email, user.first_name,
              'Intervento', clientName.trim(),
              date.trim(), description.trim()
            ).catch(() => {});
          }
        });
      }

      res.status(201).json(result);
    }
  );
});

// Update intervention status
router.patch('/:id/status', (req, res) => {
  const interventionId = req.params.id;
  const { status } = req.body;

  if (!status) {
    return res.status(400).json({ error: 'Lo stato è obbligatorio.' });
  }

  db.run(
    'UPDATE interventions SET status = ? WHERE id = ?',
    [status.trim(), interventionId],
    function(err) {
      if (err) return res.status(500).json({ error: err.message });
      res.json({ success: true, id: interventionId, status: status.trim() });
    }
  );
});

// Delete intervention
router.delete('/:id', (req, res) => {
  const interventionId = req.params.id;
  db.run('DELETE FROM interventions WHERE id = ?', [interventionId], function(err) {
    if (err) return res.status(500).json({ error: err.message });
    console.log(`[INTERVENTIONS] Intervento ID ${interventionId} eliminato.`);
    res.json({ success: true, id: interventionId, message: 'Intervento eliminato con successo.' });
  });
});

module.exports = router;
