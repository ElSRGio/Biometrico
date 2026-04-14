package com.example.biometricos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.biometricos.activitys.HomeActivity
import com.example.biometricos.activitys.LoginActivity
import com.example.biometricos.ui.theme.BiometricosTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var estaAutenticado by remember { mutableStateOf(false) }

                    if (estaAutenticado){
                        HomeActivity()
                    }else{
                        LoginActivity(
                            onAutenticacionExitosa = { estaAutenticado = true }
                        )
                    }

                }
            }
        }
    }
}