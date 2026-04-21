package com.example.biometricos.ui.dashboard

import android.view.ViewGroup
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.biometricos.dominios.Entrenamiento
import com.example.biometricos.ui.theme.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToFocus: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onNavigateToFocus,
                containerColor = ElectricBlue,
                contentColor = Color.Black,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.DirectionsRun, contentDescription = "Focus Mode", modifier = Modifier.size(36.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "MEMBER DASHBOARD",
                color = ElectricBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ElectricBlue)
                    }
                }
                is DashboardUiState.Success -> {
                    DashboardContent(state, viewModel, onNavigateToProfile)
                }
                is DashboardUiState.Error -> {
                    Text("Error: ${state.message}", color = Color.Red)
                    Button(onClick = { viewModel.loadData() }) { Text("Reintentar") }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(state: DashboardUiState.Success, viewModel: DashboardViewModel, onNavigateToProfile: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            BentoHeader(state, onNavigateToProfile)
        }
        item {
            Text("COMPARATIVA DE RENDIMIENTO", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            PerformanceChart(state.workouts)
        }
        item {
            Text("HISTORIAL RECIENTE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        items(state.workouts, key = { it.id ?: it.hashCode() }) { workout ->
            WorkoutItem(
                workout = workout,
                onDelete = { viewModel.eliminarEntrenamiento(workout.id!!) },
                onEdit = { /* Implementar edición */ }
            )
        }
    }
}

@Composable
fun PerformanceChart(workouts: List<Entrenamiento>) {
    val lastWorkouts = workouts.take(5).reversed()
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            factory = { context ->
                BarChart(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    description.isEnabled = false
                    legend.isEnabled = false
                    setTouchEnabled(false)
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        textColor = Color.White.toArgb()
                        setDrawGridLines(false)
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "E${value.toInt() + 1}"
                            }
                        }
                    }
                    axisLeft.textColor = Color.White.toArgb()
                    axisRight.isEnabled = false
                }
            },
            update = { chart ->
                val entries = lastWorkouts.mapIndexed { index, workout ->
                    BarEntry(index.toFloat(), workout.distanciaKm.toFloat())
                }
                val dataSet = BarDataSet(entries, "Km").apply {
                    color = ElectricBlue.toArgb()
                    valueTextColor = Color.White.toArgb()
                    valueTextSize = 10f
                }
                chart.data = BarData(dataSet)
                chart.invalidate()
            }
        )
    }
}

@Composable
fun BentoHeader(state: DashboardUiState.Success, onNavigateToProfile: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(180.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("IMC", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                ImcGauge(state.imc)
                Text(state.imcCategory, color = ElectricBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight().clickable { onNavigateToProfile() },
            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("ATLETA", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.profile.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text("${state.profile.weightKg} kg", color = ElectricBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun ImcGauge(imc: Double) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(imc) {
        animatedProgress.animateTo(
            targetValue = (imc.toFloat() / 40f).coerceIn(0f, 1f),
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        )
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
        Canvas(modifier = Modifier.size(80.dp)) {
            drawArc(
                color = Color.DarkGray,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.horizontalGradient(listOf(ElectricBlue, NeonGreen)),
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress.value,
                useCenter = false,
                style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(String.format("%.1f", imc), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutItem(workout: Entrenamiento, onDelete: () -> Unit, onEdit: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false // Don't dismiss for edit
                }
                else -> false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> NeonPink
                SwipeToDismissBoxValue.StartToEnd -> ElectricBlue
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.Center
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                else -> null
            }

            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(color).padding(horizontal = 20.dp), contentAlignment = alignment) {
                icon?.let { Icon(it, contentDescription = null, tint = if (color == ElectricBlue) Color.Black else Color.White) }
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(workout.type, color = ElectricBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${workout.distanciaKm} km", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${workout.tiempoMinutos} min", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        workout.tags.forEach { tag ->
                            SuggestionChip(onClick = {}, label = { Text(tag, fontSize = 10.sp) })
                        }
                    }
                }
                workout.aiFeedback?.let {
                    Text(it.emoji, fontSize = 32.sp)
                }
            }
        }
    }
}
