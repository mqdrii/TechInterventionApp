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

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({
    status: 'OK',
    server: 'TechIntervention Cloud API',
    uptime: process.uptime(),
    timestamp: new Date().toISOString()
  });
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
