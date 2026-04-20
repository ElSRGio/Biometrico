package com.example.biometricos

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
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
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var notasAdapter: NotasAdapter

    private val speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val matches = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.get(0) ?: ""

            val metricas = extraerMetricas(spokenText)
            if (metricas != null) {
                enviarMetricasAlBackend(metricas.first, metricas.second, spokenText)
            } else {
                Toast.makeText(this, "No se reconocieron métricas en la voz", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupBiometrics()

        binding.btnRecord.setOnClickListener {
            iniciarGrabacion()
        }

        // Iniciar con autenticación
        biometricPrompt.authenticate(promptInfo)
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
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error al cargar: ${e.message}")
            }
        }
    }

    private fun enviarMetricasAlBackend(distancia: Double, tiempo: Int, texto: String) {
        lifecycleScope.launch {
            try {
                val request = Entrenamiento(distanciaKm = distancia, tiempoMinutos = tiempo, textoOriginal = texto)
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.guardarEntrenamiento(request)
                }

                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Entrenamiento guardado", Toast.LENGTH_SHORT).show()
                    cargarEntrenamientos() 
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error de red", Toast.LENGTH_SHORT).show()
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
            entries.add(BarEntry(index.toFloat(), ent.distanciaKm.toFloat()))
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

    private fun setupBiometrics() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                cargarEntrenamientos()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso Premium")
            .setSubtitle("Autenticación biométrica requerida")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
    }

    private fun iniciarGrabacion() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
        }
        speechResultLauncher.launch(intent)
    }

    private fun extraerMetricas(textoOriginal: String): Pair<Double, Int>? {
        // 1. Limpiamos y normalizamos números hablados
        var texto = textoOriginal.lowercase().trim()
            .replace("un", "1").replace("uno", "1").replace("dos", "2")
            .replace("tres", "3").replace("cuatro", "4").replace("cinco", "5")
            .replace("seis", "6").replace("siete", "7").replace("ocho", "8")
            .replace("nueve", "9").replace("diez", "10")
            .replace("media hora", "30 minutos").replace("una hora", "60 minutos")

        // 2. Regex súper flexible (acepta "5km", "5 km", "5k", "5.5 kilómetros")
        val regexDistancia = Regex("([0-9]+[.,]?[0-9]*)\\s*(km|kilómetros|kilometros|k)", RegexOption.IGNORE_CASE)
        // Acepta "20min", "20 min", "20 minutos", "20m"
        val regexTiempo = Regex("([0-9]+)\\s*(min|minutos|m)", RegexOption.IGNORE_CASE)

        val matchDistancia = regexDistancia.find(texto)
        val matchTiempo = regexTiempo.find(texto)

        // Log para depurar qué está escuchando realmente el teléfono
        android.util.Log.d("VOZ", "Texto original: $textoOriginal | Normalizado: $texto")

        if (matchDistancia != null && matchTiempo != null) {
            val distancia = matchDistancia.groupValues[1].replace(",", ".").toDoubleOrNull()
            val tiempo = matchTiempo.groupValues[1].toIntOrNull()

            if (distancia != null && tiempo != null) {
                return Pair(distancia, tiempo)
            }
        }
        return null
    }
}
