package com.example.biometricos.dominios

data class EntradaBitacora(
    val fecha: String,
    val hora: String,
    val titulo: String,
    val resumen: String
)


/* este codigo es provicional para su examen la datos debe ser extraidos desde la base de datos*/
val listaEntradaEjemplo = listOf(
    EntradaBitacora(
        fecha = "2024-06-01",
        hora = "08:30",
        titulo = "Reunión con el equipo de desarrollo",
        resumen = "Discutimos los avances del proyecto y asignamos nuevas tareas."
    ),
    EntradaBitacora(
        fecha = "2024-06-02",
        hora = "14:00",
        titulo = "Presentación del proyecto al cliente",
        resumen = "Mostramos el progreso del proyecto y recibimos feedback positivo."
    ),
    EntradaBitacora(
        fecha = "2024-06-03",
        hora = "10:15",
        titulo = "Revisión de código",
        resumen = "Realizamos una revisión de código para mejorar la calidad del software."
    )
)