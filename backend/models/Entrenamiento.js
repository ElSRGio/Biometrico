const mongoose = require('mongoose');

const entrenamientoSchema = new mongoose.Schema({
    distanciaKm: {
        type: Number,
        required: true,
        min: [0, 'La distancia no puede ser negativa']
    },
    tiempoMinutos: {
        type: Number,
        required: true,
        min: [0, 'El tiempo no puede ser negativa']
    },
    textoOriginal: { type: String },
    fecha: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Entrenamiento', entrenamientoSchema);
