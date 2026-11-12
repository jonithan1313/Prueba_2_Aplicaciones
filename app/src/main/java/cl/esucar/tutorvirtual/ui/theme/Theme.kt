package cl.esucar.tutorvirtual.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import cl.esucar.tutorvirtual.domain.DeliverableStatus

// Paleta pensada para contraste WCAG AA (texto normal >= 4.5:1).
private val LightColors = lightColorScheme(
    primary = Color(0xFF0B5D3B), onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBDC), onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF1F3A5F), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9E3F5), onSecondaryContainer = Color(0xFF0A1B33),
    background = Color(0xFFFAFAF7), onBackground = Color(0xFF111111),
    surface = Color.White, onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFE6EAE6), onSurfaceVariant = Color(0xFF303630),
    error = Color(0xFFB3261E), onError = Color.White,
    errorContainer = Color(0xFFFFE1DE), onErrorContainer = Color(0xFF5C0B07),
    outline = Color(0xFF5C625C)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD6AE), onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF0B5D3B), onPrimaryContainer = Color(0xFFCDEBDC),
    secondary = Color(0xFFA9C4EC), onSecondary = Color(0xFF0A1B33),
    secondaryContainer = Color(0xFF1F3A5F), onSecondaryContainer = Color(0xFFD9E3F5),
    background = Color(0xFF0F1411), onBackground = Color(0xFFEDEFED),
    surface = Color(0xFF161C18), onSurface = Color(0xFFEDEFED),
    surfaceVariant = Color(0xFF2B332E), onSurfaceVariant = Color(0xFFD0D6D1),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF9AA39C)
)

private val AppTypography = Typography().let { t ->
    t.copy(
        bodyLarge = t.bodyLarge.copy(fontSize = 17.sp, lineHeight = 26.sp),
        bodyMedium = t.bodyMedium.copy(fontSize = 16.sp, lineHeight = 24.sp),
        labelLarge = t.labelLarge.copy(fontSize = 14.sp)
    )
}

data class StatusColors(val container: Color, val content: Color)

@Composable
fun statusColors(status: DeliverableStatus): StatusColors {
    val dark = isSystemInDarkTheme()
    return when (status) {
        DeliverableStatus.POR_INICIAR ->
            if (dark) StatusColors(Color(0xFF2F3532), Color(0xFFE3E6E3)) else StatusColors(Color(0xFFE8EAE8), Color(0xFF2E2E2E))
        DeliverableStatus.EN_DESARROLLO ->
            if (dark) StatusColors(Color(0xFF4A3600), Color(0xFFFFE2A8)) else StatusColors(Color(0xFFFFF3D9), Color(0xFF6B4300))
        DeliverableStatus.ENVIADO ->
            if (dark) StatusColors(Color(0xFF123A6B), Color(0xFFCFE2FF)) else StatusColors(Color(0xFFDCE9FB), Color(0xFF0B3F82))
        DeliverableStatus.APROBADO ->
            if (dark) StatusColors(Color(0xFF0F4A22), Color(0xFFC8F0D2)) else StatusColors(Color(0xFFD4F0DB), Color(0xFF0F5A22))
    }
}

@Composable
fun TutorVirtualTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
