package com.josuefp.servicenotebook.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val RoseLightColorScheme = lightColorScheme(
    primary = RosePrimaryLight,
    primaryContainer = RosePrimaryContainerLight,
    onPrimaryContainer = RoseOnPrimaryContainerLight,
    secondary = RoseSecondaryLight,
    onPrimary = Color.White
)

val RoseDarkColorScheme = darkColorScheme(
    primary = RosePrimaryDark,
    primaryContainer = RosePrimaryContainerDark,
    onPrimaryContainer = RoseOnPrimaryContainerDark
)

val PurpleLightColorScheme = lightColorScheme(
    primary = PurplePrimaryLight,
    primaryContainer = PurplePrimaryContainerLight,
    onPrimaryContainer = PurpleOnPrimaryContainerLight,
    onPrimary = Color.White
)

val PurpleDarkColorScheme = darkColorScheme(
    primary = PurplePrimaryDark,
    primaryContainer = PurplePrimaryContainerDark,
    onPrimaryContainer = PurpleOnPrimaryContainerDark
)

val TealLightColorScheme = lightColorScheme(
    primary = TealPrimaryLight,
    primaryContainer = TealPrimaryContainerLight,
    onPrimaryContainer = TealOnPrimaryContainerLight,
    onPrimary = Color.White
)

val TealDarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    primaryContainer = TealPrimaryContainerDark,
    onPrimaryContainer = TealOnPrimaryContainerDark
)

val CoralLightColorScheme = lightColorScheme(
    primary = CoralPrimaryLight,
    primaryContainer = CoralPrimaryContainerLight,
    onPrimaryContainer = CoralOnPrimaryContainerLight,
    onPrimary = Color.White
)

val CoralDarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    primaryContainer = CoralPrimaryContainerDark,
    onPrimaryContainer = CoralOnPrimaryContainerDark
)

val ElegantLightColorScheme = lightColorScheme(
    primary = ElegantPrimaryLight,
    primaryContainer = ElegantPrimaryContainerLight,
    onPrimaryContainer = ElegantOnPrimaryContainerLight,
    onPrimary = Color.White
)

val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantPrimaryDark,
    primaryContainer = ElegantPrimaryContainerDark,
    onPrimaryContainer = ElegantOnPrimaryContainerDark
)

@Composable
fun ServiceNotebookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePalette: String = "ROSE",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themePalette.uppercase()) {
        "PURPLE" -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
        "TEAL" -> if (darkTheme) TealDarkColorScheme else TealLightColorScheme
        "CORAL" -> if (darkTheme) CoralDarkColorScheme else CoralLightColorScheme
        "ELEGANT_DARK" -> if (darkTheme) ElegantDarkColorScheme else ElegantLightColorScheme
        else -> if (darkTheme) RoseDarkColorScheme else RoseLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primaryContainer.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
