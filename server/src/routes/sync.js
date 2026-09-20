const express = require('express');
const router = express.Router();
const db = require('../db');

// Ultra-fast cumulative sync endpoint
router.get('/', async (req, res) => {
  try {
    const clients = await new Promise((resolve, reject) => {
      db.all('SELECT * FROM clients ORDER BY name ASC', [], (err, rows) => {
        if (err) reject(err); else resolve(rows);
      });
    });

    const appointments = await new Promise((resolve, reject) => {
      db.all('SELECT * FROM appointments ORDER BY date DESC, time DESC', [], (err, rows) => {
        if (err) reject(err); else resolve(rows);
      });
    });

    const interventions = await new Promise((resolve, reject) => {
      db.all('SELECT * FROM interventions ORDER BY date DESC, id DESC', [], (err, rows) => {
        if (err) reject(err); else resolve(rows);
      });
    });

    const technicians = await new Promise((resolve, reject) => {
      db.all(`SELECT id, email, first_name, last_name, role, department FROM users WHERE role = 'TECHNICIAN'`, [], (err, rows) => {
        if (err) reject(err); else resolve(rows);
      });
    });

    res.json({
      success: true,
      serverTime: new Date().toISOString(),
      clients: clients.map(c => ({
        id: c.id,
        name: c.name,
        phone: c.phone || '',
        address: c.address || '',
        email: c.email || '',
        notes: c.notes || ''
      })),
      appointments: appointments.map(a => ({
        id: a.id,
        clientId: a.client_id,
        clientName: a.client_name,
        date: a.date,
        time: a.time,
        description: a.description,
        department: a.department,
        assignedUserId: a.assigned_user_id,
        assignedUserName: a.assigned_user_name,
        status: a.status
      })),
      interventions: interventions.map(i => ({
        id: i.id,
        clientId: i.client_id,
        clientName: i.client_name,
        date: i.date,
        description: i.description,
        department: i.department,
        technicianId: i.technician_id,
        technicianName: i.technician_name,
        notes: i.notes || '',
        status: i.status
      })),
      technicians: technicians.map(t => ({
        id: t.id,
        email: t.email,
        firstName: t.first_name,
        lastName: t.last_name,
        fullName: `${t.first_name} ${t.last_name}`,
        role: t.role,
        department: t.department
      }))
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
