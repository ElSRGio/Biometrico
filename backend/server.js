require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const app = express();
app.use(express.json());

mongoose.connect(process.env.MONGO_URI)
    .then(() => console.log('MongoDB Atlas conectado'))
    .catch(err => console.error(err));

const verificarApiKey = (req, res, next) => {
    const apiKey = req.headers['x-api-key'];
    if (!apiKey || apiKey !== process.env.API_KEY) return res.status(401).json({ error: 'Inválido' });
    next();
};

const Entrenamiento = require('./models/Entrenamiento');
const Usuario = require('./models/Usuario');

// RF06: Obtener Perfil para Dashboard e IMC
app.get('/api/users/profile', verificarApiKey, async (req, res) => {
    let perfil = await Usuario.findOne();
    if (!perfil) perfil = await Usuario.create({});
    res.json(perfil);
});

// RF04: Guardar con Feedback Dinámico
app.post('/api/entrenamientos', verificarApiKey, async (req, res) => {
    try {
        const { distanciaKm, tiempoMinutos, textoOriginal } = req.body;
        const tags = distanciaKm > 8 ? ["Récord", "Cardio"] : ["Cardio"];

        const nuevo = new Entrenamiento({
            distanciaKm,
            tiempoMinutos,
            textoOriginal,
            tags,
            aiFeedback: {
                emoji: distanciaKm > 5 ? "⚡" : "🔋",
                shortMessage: distanciaKm > 5 ? "¡Velocidad pro!" : "Buen esfuerzo."
            }
        });
        await nuevo.save();
        res.status(201).json(nuevo);
    } catch (error) { res.status(400).json({ error: error.message }); }
});

app.get('/api/entrenamientos', verificarApiKey, async (req, res) => {
    const data = await Entrenamiento.find().sort({ fecha: -1 });
    res.json(data);
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Servidor en puerto ${PORT}`));