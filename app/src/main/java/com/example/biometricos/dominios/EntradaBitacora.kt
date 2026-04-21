package com.example.biometricos.dominios

data class EntradaBitacora(
    val fecha: String,
    val hora: String,
    val titulo: String,
    val resumen: String
)

val listaEntradaEjemplo = listOf(
    EntradaBitacora("24 Oct", "08:00 AM", "Entrenamiento de fuerza", "Sesión intensa de pesas y cardio."),
    EntradaBitacora("23 Oct", "07:30 PM", "Caminata nocturna", "Paseo relajante de 30 minutos."),
    EntradaBitacora("22 Oct", "06:45 AM", "Yoga matutino", "Estiramientos y meditación para empezar el día.")
)
