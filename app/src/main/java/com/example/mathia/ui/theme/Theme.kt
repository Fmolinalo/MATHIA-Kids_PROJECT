package com.example.mathia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.mathia.AppColors
import com.example.mathia.viewModels.ThemeMode

// Dynamic color helpers locked to high-contrast Light Theme
val Gray2Dinamico: Color
    @Composable
    get() = AppColors.Gray700

val GrayDinamico: Color
    @Composable
    get() = AppColors.White

val Purple2Dinamico: Color
    @Composable
    get() = AppColors.MathiaPurple

val PurpleDinamico: Color
    @Composable
    get() = AppColors.MathiaPurple

private val LightColorScheme = lightColorScheme(
    primary = AppColors.MathiaPurple,
    secondary = AppColors.MathiaTeal,
    tertiary = AppColors.MathiaGold,
    background = AppColors.Bg,
    surface = AppColors.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = AppColors.Gray800,
    onSurface = AppColors.Gray800
)

@Composable
fun MathkidsTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
