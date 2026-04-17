package sa.com.stc.softpossimulator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = InkBlue,
    secondary = SignalTeal,
    tertiary = WarmGray,
    background = Sand,
    surface = ColorTokens.Surface,
    surfaceVariant = ColorTokens.SurfaceVariant,
)

private val DarkColors = darkColorScheme(
    primary = Mist,
    secondary = ColorTokens.DarkSecondary,
    tertiary = ColorTokens.DarkTertiary,
    background = Night,
    surface = ColorTokens.DarkSurface,
    surfaceVariant = ColorTokens.DarkSurfaceVariant,
)

@Composable
fun SoftPosSimulatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TypographyTokens.Default,
        content = content,
    )
}

private object ColorTokens {
    val Surface = androidx.compose.ui.graphics.Color(0xFFFFFCF6)
    val SurfaceVariant = androidx.compose.ui.graphics.Color(0xFFE8EEF2)
    val DarkSecondary = androidx.compose.ui.graphics.Color(0xFF62C7DA)
    val DarkTertiary = androidx.compose.ui.graphics.Color(0xFFB7C2CF)
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF102030)
    val DarkSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF1B3144)
}
