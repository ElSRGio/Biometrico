package com.example.biometricos

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.biometricos.adapters.NotasAdapter
import com.example.biometricos.databinding.ActivityMainBinding
import com.example.biometricos.network.Entrenamiento
import com.example.biometricos.network.RetrofitClient
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notasAdapter: NotasAdapter

    private val speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val matches = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.get(0) ?: ""

            val metricas = extraerMetricas(spokenText)
            if (metricas != null) {
                enviarMetricasAlBackend(metricas.first, metricas.second, spokenText)
            } else {
                Toast.makeText(this, "No se reconocieron métricas en: \"$spokenText\"", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        
        // Inyectamos datos visuales para un look profesional inmediato
        inyectarDatosVisuales()

        binding.btnRecord.setOnClickListener {
            iniciarGrabacion()
        }

        // Cargamos los datos reales desde el backend
        cargarEntrenamientos()
    }

    private fun setupRecyclerView() {
        notasAdapter = NotasAdapter(emptyList()) { id ->
            eliminarEntrenamiento(id)
        }
        binding.rvNotas.apply {
            adapter = notasAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun inyectarDatosVisuales() {
        val entries = ArrayList<BarEntry>()
        // Datos simulados estilo Nu para que la gráfica no inicie vacía
        entries.add(BarEntry(0f, 4.5f)) // Lun
        entries.add(BarEntry(1f, 6.0f)) // Mar
        entries.add(BarEntry(2f, 2.0f)) // Mie
        entries.add(BarEntry(3f, 8.2f)) // Jue
        entries.add(BarEntry(4f, 5.5f)) // Vie
        
        configurarGrafica(binding.barChart, entries)
    }

    private fun cargarEntrenamientos() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { 
                    RetrofitClient.instance.getEntrenamientos() 
                }
                if (response.isSuccessful && response.body() != null) {
                    val lista = response.body()!!
                    notasAdapter.updateData(lista)
                    actualizarGrafica(lista)
                } else {
                    Log.e("API_ERROR", "Error al cargar: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error de red al cargar: ${e.message}")
            }
        }
    }

    private fun enviarMetricasAlBackend(distancia: Double, tiempo: Int, texto: String) {
        lifecycleScope.launch {
            try {
                // Actualizado para coincidir con el nuevo modelo de Entrenamiento
                val request = Entrenamiento(
                    distanceKm = distancia, 
                    timeMinutes = tiempo, 
                    originalText = texto,
                    type = "RUNNING",
                    tags = listOf("Manos Libres", "Voz")
                )
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.guardarEntrenamiento(request)
                }

                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "✅ Entrenamiento guardado", Toast.LENGTH_SHORT).show()
                    cargarEntrenamientos() 
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Error servidor: ${response.code()} - $errorBody")
                    Toast.makeText(this@MainActivity, "❌ Error servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Fallo de red", e)
                Toast.makeText(this@MainActivity, "📡 Error de red: Verifica conexión o URL", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun eliminarEntrenamiento(id: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { 
                    RetrofitClient.instance.borrarEntrenamiento(id) 
                }
                if (response.isSuccessful) {
                    cargarEntrenamientos()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error al eliminar")
            }
        }
    }

    private fun actualizarGrafica(lista: List<Entrenamiento>) {
        val entries = ArrayList<BarEntry>()
        lista.takeLast(7).forEachIndexed { index, ent ->
            entries.add(BarEntry(index.toFloat(), ent.distanceKm.toFloat()))
        }
        configurarGrafica(binding.barChart, entries)
    }

    private fun configurarGrafica(barChart: BarChart, entries: ArrayList<BarEntry>) {
        val dataSet = BarDataSet(entries, "Distancia (km)")
        dataSet.color = Color.parseColor("#00E676") // Verde Neón
        dataSet.valueTextColor = Color.WHITE
        
        barChart.data = BarData(dataSet)
        barChart.description.isEnabled = false
        barChart.xAxis.textColor = Color.WHITE
        barChart.axisLeft.textColor = Color.WHITE
        barChart.axisRight.isEnabled = false
        barChart.legend.textColor = Color.WHITE
        barChart.animateY(800)
        barChart.invalidate()
    }

    private fun iniciarGrabacion() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
        }
        speechResultLauncher.launch(intent)
    }

    private fun extraerMetricas(textoOriginal: String): Pair<Double, Int>? {
        // 1. Limpiamos y normalizamos números y unidades
        var texto = textoOriginal.lowercase().trim()
            .replace("punto", ".")
            .replace("coma", ".")
            .replace(" con ", ".")
            .replace("un ", "1 ")
            .replace("uno ", "1 ")
            .replace("dos", "2").replace("tres", "3").replace("cuatro", "4")
            .replace("cinco", "5").replace("seis", "6").replace("siete", "7")
            .replace("ocho", "8").replace("nueve", "9").replace("diez", "10")
            .replace("media hora", "30 minutos").replace("una hora", "60 minutos")
        
        // Limpiar espacios entre número y punto (ej: "5 . 5" -> "5.5")
        texto = texto.replace(Regex("(\\d)\\s*\\.\\s*(\\d)"), "$1.$2")

        // 2. Regex ultra-flexible para distancia (km, k, kilómetros, singular/plural)
        val regexDistancia = Regex("([0-9]+[.,]?[0-9]*)\\s*(km|kilómetros|kilometros|k|kilómetro|kilometro)", RegexOption.IGNORE_CASE)
        // 3. Regex ultra-flexible para tiempo (min, minutos, m, minuto, hora)
        val regexTiempo = Regex("([0-9]+)\\s*(min|minutos|m|minuto|hora|horas|hr|hrs)", RegexOption.IGNORE_CASE)

        val matchDistancia = regexDistancia.find(texto)
        val matchTiempo = regexTiempo.find(texto)

        // Log para depurar qué está escuchando realmente el teléfono
        Log.d("VOZ_DEBUG", "Original: $textoOriginal | Procesado: $texto")

        if (matchDistancia != null && matchTiempo != null) {
            val distancia = matchDistancia.groupValues[1].replace(",", ".").toDoubleOrNull()
            var tiempo = matchTiempo.groupValues[1].toIntOrNull() ?: 0
            
            // Conversión de horas a minutos
            if (matchTiempo.groupValues[2].contains("hora") || matchTiempo.groupValues[2].contains("hr")) {
                tiempo *= 60
            }

            if (distancia != null && tiempo > 0) {
                return Pair(distancia, tiempo)
            }
        }
        return null
    }
}
