package emy.partners.lawapp.presentation.themes

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import lawapp.shared.generated.resources.IBMPlex
import lawapp.shared.generated.resources.IBMPlexSans_Bold
import lawapp.shared.generated.resources.IBMPlexSans_Light
import lawapp.shared.generated.resources.IBMPlexSans_Medium
import lawapp.shared.generated.resources.IBMPlexSans_Regular
import lawapp.shared.generated.resources.IBMPlexSans_SemiBold
import lawapp.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalResourceApi::class)
@Composable
fun simplexFontFamily() = FontFamily(
    Font(Res.font.IBMPlexSans_Light, weight = FontWeight.Light),
    Font(Res.font.IBMPlexSans_Regular, weight = FontWeight.Normal),
    Font(Res.font.IBMPlexSans_Medium, weight = FontWeight.Medium),
    Font(Res.font.IBMPlexSans_Bold, weight = FontWeight.Bold),
    Font(Res.font.IBMPlexSans_SemiBold, weight = FontWeight.SemiBold)
)

@Composable
fun tekoTypography() = Typography().run {

    val fontFamily = simplexFontFamily()
    copy(
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontFamily =  fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily)
    )
}