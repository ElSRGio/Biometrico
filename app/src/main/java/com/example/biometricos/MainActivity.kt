package com.example.biometricos

import android.content.Intent
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
import com.example.biometricos.databinding.ActivityMainBinding
import com.example.biometricos.network.ApiService
import com.example.biometricos.network.EntrenamientoRequest
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    
    // Configuración de Retrofit (En producción, esto iría en un Singleton o DI)
    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/") // IP por defecto para el host desde el emulador Android
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val matches = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.get(0) ?: ""

            val metricas = extraerMetricas(spokenText)
            if (metricas != null) {
                enviarMetricasAlBackend(metricas.first, metricas.second, spokenText)
            } else {
                Toast.makeText(this, "No se reconocieron las métricas. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBiometrics()

        binding.btnRecord.setOnClickListener {
            iniciarGrabacion()
        }

        val datosEjemplo = listOf(
            Pair("Lun", 5.0f),
            Pair("Mar", 8.2f),
            Pair("Mie", 4.5f),
            Pair("Jue", 10.0f),
            Pair("Vie", 6.7f)
        )
        configurarGrafica(binding.barChart, datosEjemplo)

        biometricPrompt.authenticate(promptInfo)
    }

    private fun enviarMetricasAlBackend(distancia: Double, tiempo: Int, texto: String) {
        // RF04: Persistencia real en la nube usando Coroutines
        lifecycleScope.launch {
            try {
                val request = EntrenamientoRequest(distancia, tiempo, texto)
                // RNF01: Uso de API Key (Hardcoded para el examen, en producción usar BuildConfig o Keystore)
                val response = withContext(Dispatchers.IO) {
                    apiService.guardarEntrenamiento("secreto_deportivo_123", request)
                }

                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Entrenamiento guardado en la nube", Toast.LENGTH_LONG).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("NetworkError", "Error al guardar: $errorBody")
                    Toast.makeText(this@MainActivity, "Error del servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("NetworkError", "Fallo de conexión", e)
                Toast.makeText(this@MainActivity, "Fallo de red: Verifica tu conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBiometrics() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Acceso concedido", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso Deportivo")
            .setSubtitle("Usa tu huella o PIN para acceder a tus métricas")
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
        var texto = textoOriginal.lowercase()
            .replace("un", "1").replace("uno", "1")
            .replace("dos", "2").replace("tres", "3")
            .replace("cuatro", "4").replace("cinco", "5")
            .replace("seis", "6").replace("siete", "7")
            .replace("ocho", "8").replace("nueve", "9")
            .replace("diez", "10").replace("media hora", "30 minutos")

        val regexDistancia = Regex("([0-9]+[.,]?[0-9]*)\\s*(km|kilómetros|kilometros)", RegexOption.IGNORE_CASE)
        val regexTiempo = Regex("([0-9]+)\\s*(min|minutos)", RegexOption.IGNORE_CASE)

        val matchDistancia = regexDistancia.find(texto)
        val matchTiempo = regexTiempo.find(texto)

        if (matchDistancia != null && matchTiempo != null) {
            val distancia = matchDistancia.groupValues[1].replace(",", ".").toDoubleOrNull()
            val tiempo = matchTiempo.groupValues[1].toIntOrNull()

            if (distancia != null && tiempo != null) {
                return Pair(distancia, tiempo)
            }
        }
        return null
    }

    private fun configurarGrafica(barChart: BarChart, datos: List<Pair<String, Float>>) {
        val entries = ArrayList<BarEntry>()

        datos.forEachIndexed { index, dato ->
            entries.add(BarEntry(index.toFloat(), dato.second))
        }

        val dataSet = BarDataSet(entries, "Kilómetros Recorridos")
        dataSet.color = ContextCompat.getColor(this, android.R.color.holo_teal_dark)
        dataSet.valueTextColor = ContextCompat.getColor(this, android.R.color.white)
        dataSet.valueTextSize = 12f

        val data = BarData(dataSet)
        barChart.data = data

        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1000)
        barChart.axisLeft.textColor = ContextCompat.getColor(this, android.R.color.white)
        barChart.axisRight.isEnabled = false
        barChart.xAxis.textColor = ContextCompat.getColor(this, android.R.color.white)
        barChart.legend.textColor = ContextCompat.getColor(this, android.R.color.white)
        barChart.invalidate()
    }
}
