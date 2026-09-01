package it.uniboft.aquarium.uicompose.ui.theme


import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext


private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    primaryContainer = LightPrimaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    errorContainer = LightErrorContainer,
    tertiaryContainer = LightTertiaryContainer,
    onPrimary = White,
    onBackground = LightText,
    onSurface = LightText
)


private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    primaryContainer = DarkPrimaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    errorContainer = DarkErrorContainer,
    tertiaryContainer = DarkTertiaryContainer,
    onPrimary = DarkBackground,
    onBackground = DarkText,
    onSurface = DarkText
)


private val HighContrastDarkColorScheme = darkColorScheme(
    primary = HcDarkPrimary,
    primaryContainer = HcDarkPrimaryContainer,
    background = HcDarkBackground,
    surface = HcDarkSurface,
    surfaceVariant = HcDarkSurfaceVariant,
    errorContainer = HcDarkErrorContainer,
    tertiaryContainer = HcDarkTertiaryContainer,
    onPrimary = Black,
    onBackground = HcDarkText,
    onSurface = HcDarkText
)


@Composable
fun AquariumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Lettura diretta dal registro di sistema: infallibile, bypassa i bug dell'SDK
    val isHighContrast = remember<Boolean>(context) {
        val highContrastFlag = Settings.Secure.getInt(
            context.contentResolver,
            "high_text_contrast_enabled",
            0
        )
        highContrastFlag == 1
    }


    val colorScheme = when {
        darkTheme && isHighContrast -> HighContrastDarkColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }


    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
