package emy.partners.lawapp.presentation.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.local.AppLanguage
import emy.partners.lawapp.presentation.pages.auth.AuthBrandHeader
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
            title = strings.t("Parametres d'affichage", "Display settings"),
            subtitle = strings.t(
                "Activez le mode sombre ou restez en mode clair.",
                "Enable dark mode or stay in light mode."
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
            Spacer(Modifier.height(14.dp))
            SettingsSwitchRow(
                title = strings.t("Mode sombre", "Dark mode"),
                subtitle = if (ui.settings.darkMode) {
                    strings.t("Active", "On")
                } else {
                    strings.t("Desactive", "Off")
                },
                checked = ui.settings.darkMode,
                onCheckedChange = { enabled ->
                    ui.updateSettings(ui.settings.copy(darkMode = enabled))
                },
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
    val isEnglish = ui.settings.language == AppLanguage.English

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
            Spacer(Modifier.height(14.dp))
            SettingsSwitchRow(
                title = "English",
                subtitle = if (isEnglish) "On" else "Off / Francais",
                checked = isEnglish,
                onCheckedChange = { enabled ->
                    ui.updateSettings(
                        ui.settings.copy(
                            language = if (enabled) AppLanguage.English else AppLanguage.French
                        )
                    )
                },
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                color = AuthColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
            Text(
                text = subtitle,
                color = AuthColors.TextSecondary,
                fontSize = 12.sp,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = AuthColors.AccentBright,
                checkedThumbColor = Color.White,
            ),
        )
    }
}
