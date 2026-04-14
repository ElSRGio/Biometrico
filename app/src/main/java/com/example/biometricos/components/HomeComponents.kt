package com.example.biometricos.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Importamos el modelo desde la carpeta dominios
import com.example.biometricos.dominios.EntradaBitacora




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeaders(
    modifier: Modifier = Modifier,
    title: String= "Bitacora",
    onSearchClick: ()-> Unit = {},
    onProfile: ()-> Unit = {}
) {
    TopAppBar(
        title = {
            AppTitle( // Ahora usamos la versión importada
                text = title,
                modifier= Modifier.padding(start = 16.dp),
                textAlign = TextAlign.Start
            )
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
            }
            IconButton(onClick = onProfile) {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Perfil")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}


@Composable
fun WelcomeCard(
    nombreUsuario: String,
    modifier: Modifier = Modifier,
    activityGraphContent: @Composable () -> Unit= {
        Text(
            text = "[Gráfico de actividad]",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
){
    Card(modifier= modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bienvenido, $nombreUsuario",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Aquí puedes ver un resumen de tu actividad reciente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            activityGraphContent()
        }
    }
}


@Composable
fun LogEntryCard(
    entrada: EntradaBitacora,
    modifier: Modifier = Modifier,
    onClick: ()-> Unit = {}
){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {

            Column(modifier = Modifier.width(64.dp)) {
                Text(
                    text = entrada.fecha,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = entrada.hora,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier= Modifier.width(16.dp))
            // Título y resumen (Columna derecha)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entrada.titulo,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entrada.resumen.isNotEmpty()){
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entrada.resumen,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}


@Composable
fun NewEntryFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Nueva entrada")
            Spacer(modifier= Modifier.width(8.dp))
            Text(
                text = "Nueva entrada",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}