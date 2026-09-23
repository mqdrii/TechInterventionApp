const express = require('express');
const cors = require('cors');
const os = require('os');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Enable CORS for all mobile clients
app.use(cors());
app.use(express.json());

// Request logger
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    const duration = Date.now() - start;
    console.log(`[${new Date().toLocaleTimeString()}] ${req.method} ${req.originalUrl} -> ${res.statusCode} (${duration}ms)`);
  });
  next();
});

// In-memory rolling log buffer for diagnostic inspection
const serverLogs = [];
const MAX_LOGS = 120;

function recordLog(level, ...args) {
  const line = args.map(a => (typeof a === 'object' ? JSON.stringify(a) : String(a))).join(' ');
  serverLogs.push({ time: new Date().toISOString(), level, line });
  if (serverLogs.length > MAX_LOGS) serverLogs.shift();
}

const origLog = console.log;
const origError = console.error;
const origWarn = console.warn;

console.log = (...args) => { origLog(...args); recordLog('INFO', ...args); };
console.error = (...args) => { origError(...args); recordLog('ERROR', ...args); };
console.warn = (...args) => { origWarn(...args); recordLog('WARN', ...args); };

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({
    status: 'OK',
    server: 'TechIntervention Cloud API',
    uptime: process.uptime(),
    timestamp: new Date().toISOString()
  });
});

// Logs endpoint for diagnostics
app.get('/api/logs', (req, res) => {
  res.json({
    uptime: process.uptime(),
    count: serverLogs.length,
    logs: serverLogs.slice().reverse()
  });
});

// Mail diagnostics & SMTP verification
app.get('/api/mail-status', async (req, res) => {
  try {
    const mailer = require('./mailer');
    const diagnostic = mailer.getMailDiagnostic ? mailer.getMailDiagnostic() : {};
    const smtpTest = (typeof mailer.verifySmtp === 'function') ? await mailer.verifySmtp() : null;
    res.json({
      diagnostic,
      smtpTest
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Test email sender endpoint
app.post('/api/mail-test', async (req, res) => {
  const { to } = req.body;
  if (!to) return res.status(400).json({ error: 'Specifica campo "to" nel body' });
  const mailer = require('./mailer');
  const success = await mailer.sendVerificationEmail(to, '123456', 'Admin Test');
  const diagnostic = mailer.getMailDiagnostic();
  res.json({ success, diagnostic });
});

// Routes
app.use('/api/auth', require('./routes/auth'));
app.use('/api/clients', require('./routes/clients'));
app.use('/api/appointments', require('./routes/appointments'));
app.use('/api/interventions', require('./routes/interventions'));
app.use('/api/sync', require('./routes/sync'));

// Welcome page
app.get('/', (req, res) => {
  res.send(`
    <html>
      <head>
        <title>TechIntervention Cloud API</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; padding: 40px; background: #0f172a; color: #f8fafc; }
          .card { background: #1e293b; padding: 30px; border-radius: 16px; border: 1px solid #334155; max-width: 600px; margin: auto; }
          h1 { color: #38bdf8; margin-top: 0; }
          .badge { display: inline-block; padding: 6px 12px; background: #0284c7; border-radius: 20px; font-size: 13px; font-weight: bold; }
          pre { background: #090d16; padding: 12px; border-radius: 8px; color: #a5f3fc; overflow-x: auto; }
        </style>
      </head>
      <body>
        <div class="card">
          <h1>TechIntervention API Server</h1>
          <p><span class="badge">SERVER ATTIVO</span> Sistema centralizzato multi-dispositivo</p>
          <p>Connetti la tua applicazione Android impostando l'indirizzo:</p>
          <pre>http://${getLocalIp()}:${PORT}</pre>
          <p>Endpoints disponibili:</p>
          <ul>
            <li><code>GET /api/health</code> - Health check e ping</li>
            <li><code>GET /api/sync</code> - Sincronizzazione cumulativa istantanea</li>
            <li><code>/api/auth</code> - Registrazione, login e tecnici</li>
            <li><code>/api/clients</code> - Gestione rubrica clienti</li>
            <li><code>/api/appointments</code> - Pianificazione e stati appuntamenti</li>
            <li><code>/api/interventions</code> - Registro e stati interventi</li>
          </ul>
        </div>
      </body>
    </html>
  `);
});

function getLocalIp() {
  const interfaces = os.networkInterfaces();
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        return iface.address;
      }
    }
  }
  return 'localhost';
}

app.listen(PORT, '0.0.0.0', () => {
  const localIp = getLocalIp();
  console.log(`====================================================`);
  console.log(` TechIntervention Cloud API attiva e in ascolto!    `);
  console.log(`----------------------------------------------------`);
  console.log(` Locale:     http://localhost:${PORT}`);
  console.log(` Rete Wi-Fi: http://${localIp}:${PORT}`);
  console.log(` Healthcheck: http://${localIp}:${PORT}/api/health`);
  console.log(`====================================================`);
});
