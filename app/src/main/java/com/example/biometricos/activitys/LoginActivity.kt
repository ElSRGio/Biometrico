package com.example.biometricos.activitys

import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.biometricos.components.ActionButton
import com.example.biometricos.components.AppTitle
import com.example.biometricos.components.FinggerprintIcon

@Composable
fun LoginActivity(onAutenticacionExitosa: ()-> Unit) {

    val context = LocalContext.current
    val activity =  context as? FragmentActivity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FinggerprintIcon(modifier = Modifier.padding(bottom = 24.dp))
            AppTitle(text = "Bitacora")
            Spacer(modifier = Modifier.height(48.dp))

            FinggerprintIcon(size = 120.dp, alpha = 0.6f)

            Spacer(modifier = Modifier.height(48.dp))

            ActionButton(
                onClick = {
                    if (activity != null) {
                        LanzarBiometrica(activity, onAutenticacionExitosa)
                    } else {
                        Toast.makeText(
                            context,
                            "Error: No se pudo iniciar la autenticación biométrica",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                text = "Iniciar sesión",
                icon = Icons.Default.Fingerprint
            )
            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = {}) {
                Text(
                    text = "Usar pin de dispositivo",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }


}

fun LanzarBiometrica(activity: FragmentActivity, onAutenticacionExitosa: () -> Unit){

    val executor = ContextCompat.getMainExecutor(activity)

    val biometricPrompt = BiometricPrompt(activity, executor,
        object: BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(activity, "Error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(activity, "Desbloqueo exitoso", Toast.LENGTH_SHORT).show()
                onAutenticacionExitosa()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(activity, "Huella no reconocida, intenta de nuevo", Toast.LENGTH_SHORT).show()
            }
        })

    val promptInfo= BiometricPrompt.PromptInfo.Builder()
        .setTitle("Acceso a la bitacora")
        .setSubtitle("Autenticacion reqeurida")
        .setDescription("Usa tu huella para acceder a la bitacora")
        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        .build()

    val biometricManager = BiometricManager.from(activity)
    if (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)== BiometricManager.BIOMETRIC_SUCCESS){
        biometricPrompt.authenticate(promptInfo)
    }else{
        Toast.makeText(activity, "Huella no configurada en el dispositivo", Toast.LENGTH_SHORT).show()
    }
}