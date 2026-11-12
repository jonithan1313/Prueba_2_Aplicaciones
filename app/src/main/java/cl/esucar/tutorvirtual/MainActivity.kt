package cl.esucar.tutorvirtual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import cl.esucar.tutorvirtual.di.LocalContainer
import cl.esucar.tutorvirtual.ui.navigation.AppRoot
import cl.esucar.tutorvirtual.ui.theme.TutorVirtualTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as TutorApp).container
        setContent {
            CompositionLocalProvider(LocalContainer provides container) {
                TutorVirtualTheme {
                    val windowSize = calculateWindowSizeClass(this)
                    AppRoot(windowSize.widthSizeClass)
                }
            }
        }
    }
}
