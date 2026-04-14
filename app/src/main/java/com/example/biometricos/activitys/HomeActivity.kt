package com.example.biometricos.activitys


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.biometricos.components.LogEntryCard
import com.example.biometricos.components.NewEntryFAB
import com.example.biometricos.components.WelcomeCard
import com.example.biometricos.dominios.listaEntradaEjemplo


@Composable
fun HomeActivity() {
    val context = LocalContext.current

    val permissionLauncher= rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted){
                Toast.makeText(context, "Permiso concedido", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, "Se requiere el micrófono para grabar entradas de voz", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        floatingActionButton = {
            NewEntryFAB(
                onClick = { Toast.makeText(context, "Nueva Entrada", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.padding(16.dp)
            )
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()

                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WelcomeCard(nombreUsuario = "Usuario")
                }

                items(listaEntradaEjemplo){ entrada ->
                    LogEntryCard(
                        entrada = entrada,
                        onClick = {
                            Toast.makeText(context, "Entrada: ${entrada.titulo}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}