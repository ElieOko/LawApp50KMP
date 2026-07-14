package emy.partners.lawapp.presentation.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.local.AppLanguage
import emy.partners.lawapp.presentation.pages.auth.AuthBrandHeader
import emy.partners.lawapp.presentation.pages.auth.AuthChoiceChips
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel
import emy.partners.lawapp.presentation.settings.LocalAppUiController
import emy.partners.lawapp.presentation.settings.t

@Composable
fun ThemeSettingsPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val ui = LocalAppUiController.current
    val strings = ui.settings
    val pageBg = if (ui.settings.darkMode) Color(0xFF0B1220) else Color(0xFFE8EEF7)

    Column(
        modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .padding(top = 36.dp, bottom = 24.dp)
    ) {
        AuthBrandHeader(
            title = strings.t("Mode d'affichage", "Display mode"),
            subtitle = strings.t(
                "Choisissez le theme clair ou sombre de l'application.",
                "Choose the light or dark theme for the app."
            ),
            onBack = onBack,
        )
        Spacer(Modifier.height(18.dp))
        AuthFormPanel {
            Text(
                text = strings.t("Theme", "Theme"),
                color = AuthColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(12.dp))
            AuthChoiceChips(
                label = strings.t("Mode", "Mode"),
                options = listOf(strings.t("Clair", "Light"), strings.t("Sombre", "Dark")),
                selected = if (ui.settings.darkMode) {
                    strings.t("Sombre", "Dark")
                } else {
                    strings.t("Clair", "Light")
                },
                onSelected = { option ->
                    ui.updateSettings(
                        ui.settings.copy(darkMode = option == strings.t("Sombre", "Dark"))
                    )
                }
            )
        }
    }
}

@Composable
fun LanguageSettingsPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val ui = LocalAppUiController.current
    val strings = ui.settings
    val pageBg = if (ui.settings.darkMode) Color(0xFF0B1220) else Color(0xFFE8EEF7)

    Column(
        modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .padding(top = 36.dp, bottom = 24.dp)
    ) {
        AuthBrandHeader(
            title = strings.t("Langue", "Language"),
            subtitle = strings.t(
                "Choisissez la langue d'affichage de l'application.",
                "Choose the display language of the app."
            ),
            onBack = onBack,
        )
        Spacer(Modifier.height(18.dp))
        AuthFormPanel {
            Text(
                text = strings.t("Langue", "Language"),
                color = AuthColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(12.dp))
            AuthChoiceChips(
                label = strings.t("Langue", "Language"),
                options = listOf("Francais", "English"),
                selected = if (ui.settings.language == AppLanguage.French) "Francais" else "English",
                onSelected = { option ->
                    ui.updateSettings(
                        ui.settings.copy(
                            language = if (option == "English") AppLanguage.English else AppLanguage.French
                        )
                    )
                }
            )
        }
    }
}
