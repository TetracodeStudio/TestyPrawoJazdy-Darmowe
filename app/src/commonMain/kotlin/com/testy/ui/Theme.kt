package com.testy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import testy.app.generated.resources.*

enum class AppThemeMode(val id: String, val label: String) {
    SYSTEM("system", "Systemowy"),
    LIGHT("light", "Jasny"),
    DARK("dark", "Ciemny");

    companion object {
        fun fromId(id: String?): AppThemeMode {
            return entries.find { it.id == id } ?: SYSTEM
        }
    }
}

enum class AppThemePalette(val id: String, val label: String, val previewColor: Color) {
    BLUE("blue", "Niebieski", Color(0xFF0284C7)),
    GREEN("green", "Zielony", Color(0xFF059669)),
    ORANGE("orange", "Bursztynowy", Color(0xFFEA580C)),
    PURPLE("purple", "Fioletowy", Color(0xFF7C3AED)),
    RED("red", "Czerwony", Color(0xFFDC2626)),
    SLATE("slate", "Grafitowy", Color(0xFF475569));

    companion object {
        fun fromId(id: String?): AppThemePalette {
            return entries.find { it.id == id } ?: BLUE
        }
    }
}

// ---------- COLOR SCHEMES FOR PALETTES ----------
private fun getLightColorScheme(palette: AppThemePalette): ColorScheme {
    return when (palette) {
        AppThemePalette.BLUE -> lightColorScheme(
            primary = Color(0xFF0284C7),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE0F2FE),
            onPrimaryContainer = Color(0xFF0369A1),
            secondary = Color(0xFF0D9488),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCCFBF1),
            onSecondaryContainer = Color(0xFF115E59),
            tertiary = Color(0xFF2563EB),
            onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFE2E8F0),
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFFCBD5E1),
            error = Color(0xFFDC2626),
            onError = Color(0xFFFFFFFF)
        )
        AppThemePalette.GREEN -> lightColorScheme(
            primary = Color(0xFF059669),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD1FAE5),
            onPrimaryContainer = Color(0xFF065F46),
            secondary = Color(0xFF0284C7),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE0F2FE),
            onSecondaryContainer = Color(0xFF0369A1),
            tertiary = Color(0xFF16A34A),
            onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFE2E8F0),
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFFCBD5E1),
            error = Color(0xFFDC2626),
            onError = Color(0xFFFFFFFF)
        )
        AppThemePalette.ORANGE -> lightColorScheme(
            primary = Color(0xFFEA580C),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDBCB),
            onPrimaryContainer = Color(0xFF341100),
            secondary = Color(0xFFB45309),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDBCF),
            onSecondaryContainer = Color(0xFF351000),
            tertiary = Color(0xFFCA8A04),
            onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFFFFFBF5),
            onBackground = Color(0xFF1C1917),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1C1917),
            surfaceVariant = Color(0xFFF5EDE3),
            onSurfaceVariant = Color(0xFF57534E),
            outline = Color(0xFFD6D3D1),
            error = Color(0xFFDC2626),
            onError = Color(0xFFFFFFFF)
        )
        AppThemePalette.PURPLE -> lightColorScheme(
            primary = Color(0xFF7C3AED),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEDE9FE),
            onPrimaryContainer = Color(0xFF5B21B6),
            secondary = Color(0xFF9333EA),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF3E8FF),
            onSecondaryContainer = Color(0xFF6B21A8),
            tertiary = Color(0xFFC084FC),
            onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFFFAF5FF),
            onBackground = Color(0xFF13091F),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF13091F),
            surfaceVariant = Color(0xFFF3E8FF),
            onSurfaceVariant = Color(0xFF4B5563),
            outline = Color(0xFFD1D5DB),
            error = Color(0xFFDC2626),
            onError = Color(0xFFFFFFFF)
        )
        AppThemePalette.RED -> lightColorScheme(
            primary = Color(0xFFDC2626),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFEE2E2),
            onPrimaryContainer = Color(0xFF991B1B),
            secondary = Color(0xFFE11D48),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFE4E6),
            onSecondaryContainer = Color(0xFF9F1239),
            tertiary = Color(0xFFD97706),
            onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFFFFF5F5),
            onBackground = Color(0xFF180A0A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF180A0A),
            surfaceVariant = Color(0xFFFEE2E2),
            onSurfaceVariant = Color(0xFF4B5563),
            outline = Color(0xFFD1D5DB),
            error = Color(0xFFB91C1C),
            onError = Color(0xFFFFFFFF)
        )
        AppThemePalette.SLATE -> lightColorScheme(
            primary = Color(0xFF475569),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE2E8F0),
            onPrimaryContainer = Color(0xFF1E293B),
            secondary = Color(0xFF64748B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF1F5F9),
            onSecondaryContainer = Color(0xFF0F172A),
            tertiary = Color(0xFF0EA5E9),
            onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFE2E8F0),
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFFCBD5E1),
            error = Color(0xFFDC2626),
            onError = Color(0xFFFFFFFF)
        )
    }
}

private fun getDarkColorScheme(palette: AppThemePalette): ColorScheme {
    return when (palette) {
        AppThemePalette.BLUE -> darkColorScheme(
            primary = Color(0xFF38BDF8),
            onPrimary = Color(0xFF0C4A6E),
            primaryContainer = Color(0xFF0369A1),
            onPrimaryContainer = Color(0xFFE0F2FE),
            secondary = Color(0xFF2DD4BF),
            onSecondary = Color(0xFF115E59),
            secondaryContainer = Color(0xFF134E4A),
            onSecondaryContainer = Color(0xFFCCFBF1),
            tertiary = Color(0xFF60A5FA),
            onTertiary = Color(0xFF1E3A8A),
            background = Color(0xFF0F172A),
            onBackground = Color(0xFFF1F5F9),
            surface = Color(0xFF1E293B),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF334155),
            onSurfaceVariant = Color(0xFFCBD5E1),
            outline = Color(0xFF64748B),
            error = Color(0xFFF87171),
            onError = Color(0xFF450A0A)
        )
        AppThemePalette.GREEN -> darkColorScheme(
            primary = Color(0xFF4ADE80),
            onPrimary = Color(0xFF052E16),
            primaryContainer = Color(0xFF065F46),
            onPrimaryContainer = Color(0xFFD1FAE5),
            secondary = Color(0xFF60A5FA),
            onSecondary = Color(0xFF082F49),
            secondaryContainer = Color(0xFF166534),
            onSecondaryContainer = Color(0xFFDCFCE7),
            tertiary = Color(0xFF34D399),
            onTertiary = Color(0xFF064E3B),
            background = Color(0xFF0A0A0A),
            onBackground = Color(0xFFE5E7EB),
            surface = Color(0xFF161616),
            onSurface = Color(0xFFD1D5DB),
            surfaceVariant = Color(0xFF1F1F1F),
            onSurfaceVariant = Color(0xFF9CA3AF),
            outline = Color(0xFF4B5563),
            error = Color(0xFFEF4444),
            onError = Color(0xFF450A0A)
        )
        AppThemePalette.ORANGE -> darkColorScheme(
            primary = Color(0xFFFB923C),
            onPrimary = Color(0xFF431407),
            primaryContainer = Color(0xFF7E2D00),
            onPrimaryContainer = Color(0xFFFFDBCB),
            secondary = Color(0xFFFBBF24),
            onSecondary = Color(0xFF451A03),
            secondaryContainer = Color(0xFF3A280D),
            onSecondaryContainer = Color(0xFFFFDBCF),
            tertiary = Color(0xFFFCD34D),
            onTertiary = Color(0xFF422006),
            background = Color(0xFF171412),
            onBackground = Color(0xFFF7F4EF),
            surface = Color(0xFF292524),
            onSurface = Color(0xFFF7F4EF),
            surfaceVariant = Color(0xFF44403C),
            onSurfaceVariant = Color(0xFFD6D3D1),
            outline = Color(0xFF78716C),
            error = Color(0xFFF87171),
            onError = Color(0xFF450A0A)
        )
        AppThemePalette.PURPLE -> darkColorScheme(
            primary = Color(0xFFA78BFA),
            onPrimary = Color(0xFF3B0764),
            primaryContainer = Color(0xFF5B21B6),
            onPrimaryContainer = Color(0xFFEDE9FE),
            secondary = Color(0xFFC084FC),
            onSecondary = Color(0xFF581C87),
            secondaryContainer = Color(0xFF6B21A8),
            onSecondaryContainer = Color(0xFFF3E8FF),
            tertiary = Color(0xFFE9D5FF),
            onTertiary = Color(0xFF4A044E),
            background = Color(0xFF13091F),
            onBackground = Color(0xFFF5F3FF),
            surface = Color(0xFF221533),
            onSurface = Color(0xFFF5F3FF),
            surfaceVariant = Color(0xFF3B2754),
            onSurfaceVariant = Color(0xFFD1D5DB),
            outline = Color(0xFF6B7280),
            error = Color(0xFFF87171),
            onError = Color(0xFF450A0A)
        )
        AppThemePalette.RED -> darkColorScheme(
            primary = Color(0xFFF87171),
            onPrimary = Color(0xFF450A0A),
            primaryContainer = Color(0xFF991B1B),
            onPrimaryContainer = Color(0xFFFEE2E2),
            secondary = Color(0xFFFB7185),
            onSecondary = Color(0xFF4C0519),
            secondaryContainer = Color(0xFF9F1239),
            onSecondaryContainer = Color(0xFFFFE4E6),
            tertiary = Color(0xFFFBBF24),
            onTertiary = Color(0xFF451A03),
            background = Color(0xFF180A0A),
            onBackground = Color(0xFFFEF2F2),
            surface = Color(0xFF2A1212),
            onSurface = Color(0xFFFEF2F2),
            surfaceVariant = Color(0xFF451A1A),
            onSurfaceVariant = Color(0xFFE5E7EB),
            outline = Color(0xFF6B7280),
            error = Color(0xFFF87171),
            onError = Color(0xFF450A0A)
        )
        AppThemePalette.SLATE -> darkColorScheme(
            primary = Color(0xFF94A3B8),
            onPrimary = Color(0xFF0F172A),
            primaryContainer = Color(0xFF334155),
            onPrimaryContainer = Color(0xFFF1F5F9),
            secondary = Color(0xFFCBD5E1),
            onSecondary = Color(0xFF1E293B),
            secondaryContainer = Color(0xFF1E293B),
            onSecondaryContainer = Color(0xFFF8FAFC),
            tertiary = Color(0xFF38BDF8),
            onTertiary = Color(0xFF0C4A6E),
            background = Color(0xFF0F172A),
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF1E293B),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF334155),
            onSurfaceVariant = Color(0xFFCBD5E1),
            outline = Color(0xFF64748B),
            error = Color(0xFFF87171),
            onError = Color(0xFF450A0A)
        )
    }
}

// ---------- TYPOGRAPHY ----------
val AppFontFamily = FontFamily.SansSerif

val TestyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ---------- THEME COMPOSABLES ----------
@Composable
fun TestyTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    palette: AppThemePalette = AppThemePalette.BLUE,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    TestyTheme(
        darkTheme = darkTheme,
        palette = palette,
        content = content
    )
}

@Composable
fun TestyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: AppThemePalette = AppThemePalette.BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) getDarkColorScheme(palette) else getLightColorScheme(palette)


    CompositionLocalProvider() {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TestyTypography,
            content = content
        )
    }
}

// ---------- UI COMPONENT: SELEKCJA MOTYWU I PALETY ----------
@Composable
fun AppearanceAndThemeCard(
    currentThemeMode: AppThemeMode,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    currentPalette: AppThemePalette,
    onPaletteSelected: (AppThemePalette) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wygląd i Motyw Aplikacji",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Spacer(modifier = Modifier.height(2.dp))

                        // --- TRYB MOTYWU ---
                        Text(
                            text = "Tryb Motywu:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppThemeMode.entries.forEach { mode ->
                                val isSelected = mode == currentThemeMode
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clickable { onThemeModeSelected(mode) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = mode.label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // --- PALETA KOLORÓW ---
                        Text(
                            text = "Paleta Kolorystyczna:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppThemePalette.entries.forEach { palette ->
                                val isSelected = palette == currentPalette
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPaletteSelected(palette) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(palette.previewColor)
                                            )
                                            Text(
                                                text = palette.label,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
