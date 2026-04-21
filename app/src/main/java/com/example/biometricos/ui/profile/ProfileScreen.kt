package com.example.biometricos.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.biometricos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EDITAR PERFIL", color = ElectricBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ElectricBlue)
                }
                is ProfileUiState.Success -> {
                    ProfileFields(
                        initialName = state.profile.name,
                        initialWeight = state.profile.weightKg,
                        initialHeight = state.profile.heightCm,
                        initialAvatar = state.profile.avatarUrl
                    ) { n, w, h, a ->
                        viewModel.saveProfile(n, w, h, a)
                    }
                }
                is ProfileUiState.Saved -> {
                    LaunchedEffect(Unit) { onBack() }
                }
                is ProfileUiState.Error -> {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = NeonPink)
                        Button(onClick = { viewModel.loadProfile() }) { Text("Reintentar") }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ProfileFields(
    initialName: String, 
    initialWeight: Double, 
    initialHeight: Int, 
    initialAvatar: String,
    onSave: (String, Double, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var weight by remember { mutableStateOf(initialWeight.toString()) }
    var height by remember { mutableStateOf(initialHeight.toString()) }
    var avatarUrl by remember { mutableStateOf(initialAvatar) }

    Column(
        modifier = Modifier.padding(24.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Avatar Preview
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(CardBackground)
                .border(2.dp, ElectricBlue, CircleShape)
        ) {
            AsyncImage(
                model = avatarUrl.ifEmpty { "https://ui-avatars.com/api/?name=$name" },
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedBorderColor = ElectricBlue,
                focusedLabelColor = ElectricBlue
            )
        )

        OutlinedTextField(
            value = avatarUrl,
            onValueChange = { avatarUrl = it },
            label = { Text("URL de Foto de Perfil") },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedBorderColor = ElectricBlue,
                focusedLabelColor = ElectricBlue
            )
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Peso (kg)") },
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = ElectricBlue,
                    focusedLabelColor = ElectricBlue
                )
            )

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Altura (cm)") },
                leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = ElectricBlue,
                    focusedLabelColor = ElectricBlue
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { 
                onSave(name, weight.toDoubleOrNull() ?: 0.0, height.toIntOrNull() ?: 0, avatarUrl)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("GUARDAR CAMBIOS", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
