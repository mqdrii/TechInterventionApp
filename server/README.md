# TechIntervention Cloud API Server 🚀

Backend centralizzato per la sincronizzazione multi-dispositivo in tempo reale dell'applicazione **TechIntervention**.

## Come Avviare in Rete Locale (PC / Ufficio)
1. Assicurati di avere Node.js installato.
2. Apri il terminale in questa cartella:
   ```bash
   npm install
   npm start
   ```
3. Il server mostrerà a terminale l'indirizzo IP locale (es: `http://192.168.1.50:3000`).
4. Tutti gli smartphone connessi alla stessa rete Wi-Fi possono connettersi inserendo questo indirizzo nell'app (tasto ⚙️ Configura Server).

## Come Eseguire il Deploy Gratuito nel Cloud (Per Usarlo Ovunque via 4G/5G)
Puoi ospitare il server gratuitamente e per sempre su **Render.com** o **Railway.app**:
1. Crea un account gratuito su [Render.com](https://render.com).
2. Crea un nuovo **Web Service** collegando questo repository GitHub.
3. Seleziona la cartella radice `server`, runtime `Node`, Build command `npm install`, Start command `npm start`.
4. Render ti fornirà un URL pubblico sicuro e gratuito (es. `https://techintervention-api.onrender.com`).
5. Inserisci questo URL nell'app Android: tutti i tecnici e l'amministratore saranno sincronizzati istantaneamente ovunque si trovino nel mondo!
