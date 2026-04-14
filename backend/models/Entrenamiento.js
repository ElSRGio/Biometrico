const mongoose = require('mongoose');

const entrenamientoSchema = new mongoose.Schema({
    distanciaKm: { type: Number, required: true },
    tiempoMinutos: { type: Number, required: true },
    textoOriginal: { type: String },
    fecha: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Entrenamiento', entrenamientoSchema);
