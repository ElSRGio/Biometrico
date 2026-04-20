const mongoose = require('mongoose');

const usuarioSchema = new mongoose.Schema({
    name: { type: String, default: "Sergio Atleta" },
    weightKg: { type: Number, default: 72.5 },
    heightCm: { type: Number, default: 178 },
    avatarUrl: { type: String, default: "https://ui-avatars.com/api/?name=Sergio+Atleta&background=00E5FF&color=fff" }
}, { timestamps: true });

// Limpia el ID para Android
usuarioSchema.set('toJSON', {
    virtuals: true,
    versionKey: false,
    transform: (doc, ret) => { delete ret._id; }
});

module.exports = mongoose.model('Usuario', usuarioSchema);