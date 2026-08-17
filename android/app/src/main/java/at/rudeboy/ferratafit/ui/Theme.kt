package at.rudeboy.ferratafit.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Farbwelt: Fels und Nacht als Grund, Gletscherblau als Leitfarbe, Seilgelb als
 * Signal für alles, was Aufmerksamkeit braucht (Steigerungen, Ziele).
 */
object Palette {
    val Ink = Color(0xFF0B0F14)
    val Surface = Color(0xFF131A22)
    val SurfaceHigh = Color(0xFF1B2530)
    val Outline = Color(0xFF2C3947)

    val Sky = Color(0xFF38BDF8)
    val SkyDeep = Color(0xFF0EA5E9)
    val Amber = Color(0xFFFBBF24)
    val Emerald = Color(0xFF34D399)
    val Rose = Color(0xFFFB7185)
    val Violet = Color(0xFFA78BFA)

    val TextHigh = Color(0xFFECF3FA)
    val TextMid = Color(0xFF9BAEC2)
    val TextLow = Color(0xFF64788C)
}

private val DarkColors = darkColorScheme(
    primary = Palette.Sky,
    onPrimary = Color(0xFF042230),
    primaryContainer = Color(0xFF10394F),
    onPrimaryContainer = Color(0xFFCBEBFB),
    secondary = Palette.Amber,
    onSecondary = Color(0xFF2E1F00),
    secondaryContainer = Color(0xFF44320A),
    onSecondaryContainer = Color(0xFFFDECC0),
    tertiary = Palette.Emerald,
    onTertiary = Color(0xFF01291C),
    background = Palette.Ink,
    onBackground = Palette.TextHigh,
    surface = Palette.Surface,
    onSurface = Palette.TextHigh,
    surfaceVariant = Palette.SurfaceHigh,
    onSurfaceVariant = Palette.TextMid,
    outline = Palette.Outline,
    outlineVariant = Color(0xFF223040),
    error = Palette.Rose,
    onError = Color(0xFF390A12)
)

// Helles Schema als Rückfallebene. Die App ist auf Dunkel ausgelegt — im Studio
// oder abends am Gerät ist das angenehmer und spart auf OLED Strom.
private val LightColors = lightColorScheme(
    primary = Color(0xFF0369A1),
    secondary = Color(0xFFB45309),
    tertiary = Color(0xFF047857),
    background = Color(0xFFF6F9FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7EEF5)
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 46.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp),
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 23.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.5.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp)
)

/** Verläufe, die im UI mehrfach auftauchen. */
object Gradients {
    val hero = Brush.linearGradient(listOf(Color(0xFF12304A), Color(0xFF0B1220)))
    val sky = Brush.linearGradient(listOf(Palette.Sky, Palette.SkyDeep))
    val amber = Brush.linearGradient(listOf(Palette.Amber, Color(0xFFF59E0B)))
    val emerald = Brush.linearGradient(listOf(Palette.Emerald, Color(0xFF10B981)))
}

@Composable
fun FerrataTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
