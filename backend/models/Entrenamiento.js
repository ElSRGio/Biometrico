const mongoose = require('mongoose');

const entrenamientoSchema = new mongoose.Schema({
    type: { type: String, default: "RUNNING" },
    distanciaKm: { type: Number, required: true },
    tiempoMinutos: { type: Number, required: true },
    textoOriginal: { type: String },
    tags: [{ type: String }],
    aiFeedback: {
        emoji: { type: String, default: "🔥" },
        shortMessage: { type: String, default: "¡Buen ritmo!" }
    },
    fecha: { type: Date, default: Date.now }
});

// Limpia el ID para Android
entrenamientoSchema.set('toJSON', {
    virtuals: true,
    versionKey: false,
    transform: (doc, ret) => { delete ret._id; }
});

module.exports = mongoose.model('Entrenamiento', entrenamientoSchema);