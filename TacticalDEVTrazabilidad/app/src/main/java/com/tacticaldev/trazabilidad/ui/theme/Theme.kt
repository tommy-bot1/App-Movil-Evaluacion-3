package com.tacticaldev.trazabilidad.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Verde80,
    onPrimary = VerdeSobrePrimario80,
    primaryContainer = VerdeContenedor80,
    onPrimaryContainer = VerdeSobreContenedor80,
    secondary = VerdeSecundario80,
    secondaryContainer = VerdeContenedorSec80,
    onSecondaryContainer = VerdeSobreContenedor80,
    tertiary = Verde80
)

private val LightColorScheme = lightColorScheme(
    primary = Verde40,
    onPrimary = Color.White,
    primaryContainer = VerdeContenedor40,
    onPrimaryContainer = VerdeSobreContenedor40,
    secondary = VerdeSecundario40,
    secondaryContainer = VerdeContenedor40,
    onSecondaryContainer = VerdeSobreContenedor40,
    tertiary = Verde40
)

@Composable
fun TacticalDEVTrazabilidadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivado para que se respete la paleta verde de la app
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}