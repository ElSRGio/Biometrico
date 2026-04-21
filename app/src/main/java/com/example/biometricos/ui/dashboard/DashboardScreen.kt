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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.biometricos.dominios.Entrenamiento
import com.example.biometricos.ui.theme.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToFocus: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var editingWorkout by remember { mutableStateOf<Entrenamiento?>(null) }

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
                    DashboardContent(
                        state = state, 
                        viewModel = viewModel, 
                        onNavigateToProfile = onNavigateToProfile,
                        onEditRequest = { editingWorkout = it }
                    )
                }
                is DashboardUiState.Error -> {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("Error: ${state.message}", color = Color.Red)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadData() }) { Text("Reintentar") }
                    }
                }
            }
        }

        editingWorkout?.let { workout ->
            EditWorkoutDialog(
                workout = workout,
                onDismiss = { editingWorkout = null },
                onConfirm = { dist, time ->
                    workout.id?.let { viewModel.actualizarEntrenamiento(it, dist, time) }
                    editingWorkout = null
                }
            )
        }
    }
}

@Composable
fun EditWorkoutDialog(
    workout: Entrenamiento,
    onDismiss: () -> Unit,
    onConfirm: (Double, Int) -> Unit
) {
    var dist by remember { mutableStateOf(workout.distanciaKm.toString()) }
    var time by remember { mutableStateOf(workout.tiempoMinutos.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("EDITAR ENTRENAMIENTO", color = ElectricBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dist,
                    onValueChange = { dist = it },
                    label = { Text("Distancia (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Tiempo (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                onConfirm(dist.toDoubleOrNull() ?: 0.0, time.toIntOrNull() ?: 0)
            }) {
                Text("GUARDAR", color = ElectricBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        }
    )
}

@Composable
fun DashboardContent(
    state: DashboardUiState.Success, 
    viewModel: DashboardViewModel, 
    onNavigateToProfile: () -> Unit,
    onEditRequest: (Entrenamiento) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            BentoHeader(state, onNavigateToProfile)
        }
        item {
            Text("PROGRESO HISTÓRICO TOTAL", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            FullPerformanceChart(state.workouts)
        }
        item {
            Text("HISTORIAL RECIENTE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        // Usamos una clave más segura para evitar el crash al deslizar/borrar
        items(state.workouts, key = { it.id ?: System.currentTimeMillis() + it.hashCode() }) { workout ->
            WorkoutItem(
                workout = workout, 
                onDelete = { workout.id?.let { viewModel.eliminarEntrenamiento(it) } },
                onEdit = { onEditRequest(workout) }
            )
        }
    }
}

@Composable
fun FullPerformanceChart(workouts: List<Entrenamiento>) {
    val allWorkouts = workouts.reversed() 
    Card(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            factory = { context ->
                LineChart(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    description.isEnabled = false
                    legend.isEnabled = false
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setPinchZoom(true)
                    
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        textColor = Color.White.toArgb()
                        setDrawGridLines(false)
                        granularity = 1f
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "S${value.toInt() + 1}"
                            }
                        }
                    }
                    axisLeft.apply {
                        textColor = Color.White.toArgb()
                        setDrawGridLines(true)
                        gridColor = Color.Gray.copy(alpha = 0.3f).toArgb()
                    }
                    axisRight.isEnabled = false
                }
            },
            update = { chart ->
                val entries = allWorkouts.mapIndexed { index, workout ->
                    Entry(index.toFloat(), workout.distanciaKm.toFloat())
                }
                
                if (entries.isNotEmpty()) {
                    val dataSet = LineDataSet(entries, "Kilómetros").apply {
                        color = ElectricBlue.toArgb()
                        setCircleColor(ElectricBlue.toArgb())
                        lineWidth = 2f
                        circleRadius = 3f
                        setDrawCircleHole(false)
                        valueTextColor = Color.Transparent.toArgb()
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawFilled(true)
                        fillColor = ElectricBlue.toArgb()
                        fillAlpha = 50
                    }
                    chart.data = LineData(dataSet)
                    chart.animateX(500)
                }
                chart.invalidate()
            }
        )
    }
}

@Composable
fun BentoHeader(state: DashboardUiState.Success, onNavigateToProfile: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(180.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val imcColor = when {
            state.imc < 18.5 -> Color(0xFF4DD0E1)
            state.imc < 25.0 -> Color(0xFF66BB6A)
            state.imc < 30.0 -> Color(0xFFFFA726)
            else -> Color(0xFFEF5350)
        }

        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("IMC", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                ImcGauge(state.imc, imcColor)
                Text(state.imcCategory, color = imcColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
fun ImcGauge(imc: Double, color: Color) {
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
                color = color,
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
                    false
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
