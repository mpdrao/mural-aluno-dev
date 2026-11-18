package br.edu.ifsp.ifsigard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = IfGreen,
    onPrimary = Color.White,
    primaryContainer = IfGreenLight,
    onPrimaryContainer = IfGreenDark,
    secondary = IfGreenDark,
    onSecondary = Color.White,
    error = IfRed,
    onError = Color.White,
    errorContainer = IfRedLight,
    onErrorContainer = IfRed,
    background = SurfaceBg,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary
)

@Composable
fun IFSIGARDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
