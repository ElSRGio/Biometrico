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

// Obtener Perfil
app.get('/api/users/profile', verificarApiKey, async (req, res) => {
    let perfil = await Usuario.findOne();
    if (!perfil) perfil = await Usuario.create({ name: "Atleta", weightKg: 70, heightCm: 170 });
    res.json(perfil);
});

// Actualizar Perfil
app.post('/api/users/profile', verificarApiKey, async (req, res) => {
    try {
        const { name, weightKg, heightCm, avatarUrl } = req.body;
        let perfil = await Usuario.findOne();
        if (perfil) {
            perfil.name = name;
            perfil.weightKg = weightKg;
            perfil.heightCm = heightCm;
            perfil.avatarUrl = avatarUrl;
            await perfil.save();
        } else {
            perfil = await Usuario.create({ name, weightKg, heightCm, avatarUrl });
        }
        res.json(perfil);
    } catch (error) { res.status(400).json({ error: error.message }); }
});

// Guardar Entrenamiento
app.post('/api/entrenamientos', verificarApiKey, async (req, res) => {
    try {
        const { distanciaKm, tiempoMinutos, originalText, type, tags } = req.body;
        const nuevo = new Entrenamiento({
            distanciaKm,
            tiempoMinutos,
            originalText,
            type,
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

// Actualizar Entrenamiento
app.put('/api/entrenamientos/:id', verificarApiKey, async (req, res) => {
    try {
        const { distanciaKm, tiempoMinutos } = req.body;
        const actualizado = await Entrenamiento.findByIdAndUpdate(
            req.params.id,
            { distanciaKm, tiempoMinutos },
            { new: true }
        );
        res.json(actualizado);
    } catch (error) { res.status(400).json({ error: error.message }); }
});

// Obtener Entrenamientos
app.get('/api/entrenamientos', verificarApiKey, async (req, res) => {
    const data = await Entrenamiento.find().sort({ fecha: -1 });
    res.json(data);
});

// Eliminar Entrenamiento
app.delete('/api/entrenamientos/:id', verificarApiKey, async (req, res) => {
    try {
        await Entrenamiento.findByIdAndDelete(req.params.id);
        res.json({ message: 'Eliminado' });
    } catch (error) { res.status(400).json({ error: error.message }); }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Servidor en puerto ${PORT}`));
