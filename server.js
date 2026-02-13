const express = require("express");
const bodyParser = require("body-parser");
const sqlite3 = require("sqlite3").verbose();

const swaggerUi = require("swagger-ui-express");
const swaggerSpec = require("./swagger");

const app = express();
app.use(bodyParser.json());

/**
 * @swagger
 * components:
 *   schemas:
 *     Speaker:
 *       type: object
 *       required:
 *         - name
 *         - topic
 *       properties:
 *         id:
 *           type: integer
 *         name:
 *           type: string
 *         topic:
 *           type: string
 *       example:
 *         name: Alice Johnson
 *         topic: Automation Testing
 *
 *     Session:
 *       type: object
 *       required:
 *         - title
 *         - speakerId
 *       properties:
 *         id:
 *           type: integer
 *         title:
 *           type: string
 *         speakerId:
 *           type: integer
 *       example:
 *         title: Intro to Cypress
 *         speakerId: 1
 */

app.get("/", (req, res) => {
  res.send("API is running! Use /speakers and /sessions routes.");
});

/**
 * @swagger
 * /api-docs:
 *   get:
 *     summary: Swagger API Documentation
 */
app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));


// Create SQLite database
const db = new sqlite3.Database(":memory:");

// Create tables
db.serialize(() => {
  db.run(`CREATE TABLE Speaker (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    topic TEXT
  )`);

  db.run(`CREATE TABLE Session (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT,
    speakerId INTEGER,
    FOREIGN KEY(speakerId) REFERENCES Speaker(id)
  )`);
});


// ===============================
// --- CRUD for Speaker ---
// ===============================

/**
 * @swagger
 * /speakers:
 *   post:
 *     summary: Create a new speaker
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/Speaker'
 *     responses:
 *       200:
 *         description: Speaker created successfully
 */
app.post("/speakers", (req, res) => {
  const { name, topic } = req.body;
  db.run(
    `INSERT INTO Speaker(name, topic) VALUES(?, ?)`,
    [name, topic],
    function (err) {
      if (err) return res.status(500).json({ error: err.message });
      res.json({ id: this.lastID, name, topic });
    }
  );
});

/**
 * @swagger
 * /speakers:
 *   get:
 *     summary: Get all speakers
 *     responses:
 *       200:
 *         description: List of speakers
 */
app.get("/speakers", (req, res) => {
  db.all(`SELECT * FROM Speaker`, [], (err, rows) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(rows);
  });
});

/**
 * @swagger
 * /speakers/{id}:
 *   get:
 *     summary: Get a speaker by ID
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Speaker found
 *       404:
 *         description: Speaker not found
 */
app.get("/speakers/:id", (req, res) => {
  const { id } = req.params;
  db.get(`SELECT * FROM Speaker WHERE id = ?`, [id], (err, row) => {
    if (err) return res.status(500).json({ error: err.message });
    if (!row) return res.status(404).json({ error: "Speaker not found" });
    res.json(row);
  });
});

/**
 * @swagger
 * /speakers/{id}:
 *   put:
 *     summary: Update a speaker by ID
 */
app.put("/speakers/:id", (req, res) => {
  const { id } = req.params;
  const { name, topic } = req.body;

  db.run(
    `UPDATE Speaker SET name = ?, topic = ? WHERE id = ?`,
    [name, topic, id],
    function (err) {
      if (err) return res.status(500).json({ error: err.message });
      if (this.changes === 0)
        return res.status(404).json({ error: "Speaker not found" });

      res.json({ id, name, topic });
    }
  );
});

/**
 * @swagger
 * /speakers/{id}:
 *   delete:
 *     summary: Delete a speaker by ID
 */
app.delete("/speakers/:id", (req, res) => {
  const { id } = req.params;

  db.run(`DELETE FROM Speaker WHERE id = ?`, [id], function (err) {
    if (err) return res.status(500).json({ error: err.message });
    if (this.changes === 0)
      return res.status(404).json({ error: "Speaker not found" });

    res.json({ message: "Speaker deleted" });
  });
});


// ===============================
// --- CRUD for Session ---
// ===============================

/**
 * @swagger
 * /sessions:
 *   post:
 *     summary: Create a new session
 */
app.post("/sessions", (req, res) => {
  const { title, speakerId } = req.body;

  db.run(
    `INSERT INTO Session(title, speakerId) VALUES(?, ?)`,
    [title, speakerId],
    function (err) {
      if (err) return res.status(500).json({ error: err.message });
      res.json({ id: this.lastID, title, speakerId });
    }
  );
});

/**
 * @swagger
 * /sessions:
 *   get:
 *     summary: Get all sessions (with speaker name)
 */
app.get("/sessions", (req, res) => {
  const sql = `
    SELECT Session.id, Session.title, Session.speakerId, Speaker.name AS speakerName
    FROM Session
    LEFT JOIN Speaker ON Session.speakerId = Speaker.id
  `;

  db.all(sql, [], (err, rows) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(rows);
  });
});

/**
 * @swagger
 * /sessions/{id}:
 *   get:
 *     summary: Get a session by ID
 */
app.get("/sessions/:id", (req, res) => {
  const { id } = req.params;

  db.get(`SELECT * FROM Session WHERE id = ?`, [id], (err, row) => {
    if (err) return res.status(500).json({ error: err.message });
    if (!row) return res.status(404).json({ error: "Session not found" });

    res.json(row);
  });
});

/**
 * @swagger
 * /sessions/{id}:
 *   put:
 *     summary: Update a session by ID
 */
app.put("/sessions/:id", (req, res) => {
  const { id } = req.params;
  const { title, speakerId } = req.body;

  db.run(
    `UPDATE Session SET title = ?, speakerId = ? WHERE id = ?`,
    [title, speakerId, id],
    function (err) {
      if (err) return res.status(500).json({ error: err.message });
      if (this.changes === 0)
        return res.status(404).json({ error: "Session not found" });

      res.json({ id, title, speakerId });
    }
  );
});

/**
 * @swagger
 * /sessions/{id}:
 *   delete:
 *     summary: Delete a session by ID
 */
app.delete("/sessions/:id", (req, res) => {
  const { id } = req.params;

  db.run(`DELETE FROM Session WHERE id = ?`, [id], function (err) {
    if (err) return res.status(500).json({ error: err.message });
    if (this.changes === 0)
      return res.status(404).json({ error: "Session not found" });

    res.json({ message: "Session deleted" });
  });
});


// Start server
const PORT = 3000;
app.listen(PORT, () =>
  console.log(`Server running on http://localhost:${PORT}`)
);
