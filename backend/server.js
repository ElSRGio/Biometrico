require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');

const app = express();
app.use(express.json());

// RNF01: Seguridad, conexión mediante variable de entorno
mongoose.connect(process.env.MONGO_URI)
    .then(() => console.log('MongoDB Atlas conectado'))
    .catch(err => console.error('Error de conexión:', err));

const Entrenamiento = require('./models/Entrenamiento');

// Endpoint para guardar métricas (RF04)
app.post('/api/entrenamientos', async (req, res) => {
    try {
        const { distanciaKm, tiempoMinutos, textoOriginal } = req.body;
        const nuevoEntrenamiento = new Entrenamiento({
            distanciaKm,
            tiempoMinutos,
            textoOriginal,
            fecha: new Date()
        });
        await nuevoEntrenamiento.save();
        res.status(201).json(nuevoEntrenamiento);
    } catch (error) {
        res.status(500).json({ error: 'Error al guardar el entrenamiento' });
    }
});

app.get('/api/entrenamientos', async (req, res) => {
    try {
         const entrenamientos = await Entrenamiento.find().sort({ fecha: 1 });
         res.status(200).json(entrenamientos);
    } catch (error) {
         res.status(500).json({ error: 'Error al obtener datos' });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Servidor corriendo en el puerto ${PORT}`));
