package com.example.biometricos.ui.focus

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biometricos.ui.theme.*

@Composable
fun FocusModeScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.get(0) ?: ""
            // Simple logic de extracción para el demo
            viewModel.enviarMetricas(5.5, 30, spokenText)
        } else {
            viewModel.reset()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                is FocusUiState.Idle, is FocusUiState.Recording -> {
                    PulseButton(
                        isRecording = state is FocusUiState.Recording,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.setRecording(true)
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
                            }
                            speechLauncher.launch(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        if (state is FocusUiState.Recording) "ESCUCHANDO..." else "TAP PARA GRABAR",
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
                is FocusUiState.Processing -> {
                    CircularProgressIndicator(color = ElectricBlue)
                }
                is FocusUiState.Success -> {
                    FeedbackDisplay(state.feedback.emoji, state.feedback.shortMessage) {
                        viewModel.reset()
                    }
                }
                is FocusUiState.Error -> {
                    Text(state.message, color = NeonPink)
                    Button(onClick = { viewModel.reset() }) { Text("Reintentar") }
                }
            }
        }
    }
}

@Composable
fun PulseButton(isRecording: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.2f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(200.dp)
            .scale(scale)
            .background(
                Brush.radialGradient(listOf(ElectricBlue.copy(alpha = 0.4f), Color.Transparent)),
                CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = ElectricBlue,
            tonalElevation = 8.dp
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.padding(32.dp).size(48.dp),
                tint = Color.Black
            )
        }
    }
}

@Composable
fun FeedbackDisplay(emoji: String, message: String, onReset: () -> Unit) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(500, easing = OvershootInterpolator(2f).asEasing()))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale.value)) {
        Text(emoji, fontSize = 120.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
        ) {
            Text("NUEVA NOTA", color = ElectricBlue)
        }
    }
}

// Extension to use Interpolator as Easing
fun android.view.animation.Interpolator.asEasing() = Easing { x -> getInterpolation(x) }
class OvershootInterpolator(val tension: Float) : android.view.animation.Interpolator {
    override fun getInterpolation(t: Float): Float {
        var x = t - 1.0f
        return x * x * ((tension + 1) * x + tension) + 1.0f
    }
}