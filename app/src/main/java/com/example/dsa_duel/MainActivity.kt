package com.example.dsa_duel

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import com.example.dsa_duel.navigation.DSADuelNavGraph
import com.example.dsa_duel.ui.theme.DSA_DUELTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp {
                DSADuelNavGraph()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    // Disable dynamicColor to use your own theme colors
    DSA_DUELTheme(dynamicColor = false) {
        content()
    }
}
