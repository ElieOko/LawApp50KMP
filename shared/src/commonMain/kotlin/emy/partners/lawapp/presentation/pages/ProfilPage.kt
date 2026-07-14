package emy.partners.lawapp.presentation.pages

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.remote.auth.AuthRepository
import emy.partners.lawapp.data.remote.auth.AuthSession
import emy.partners.lawapp.data.remote.auth.AuthUserProfile
import emy.partners.lawapp.data.remote.auth.SelectableAccountDto
import emy.partners.lawapp.data.remote.auth.UserRequestChange
import emy.partners.lawapp.presentation.components.basics.ProfilePhotoAvatar
import emy.partners.lawapp.presentation.components.basics.rememberFilePickerLauncher
import emy.partners.lawapp.presentation.pages.auth.AuthChoiceChips
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel
import emy.partners.lawapp.presentation.pages.auth.AuthLoadingDialog
import emy.partners.lawapp.presentation.pages.auth.AuthMessageDialog
import emy.partners.lawapp.presentation.pages.auth.AuthPrimaryButton
import emy.partners.lawapp.presentation.pages.auth.AuthTextField
import emy.partners.lawapp.presentation.settings.LocalAppUiController
import emy.partners.lawapp.presentation.settings.t
import emy.partners.lawapp.presentation.themes.BlueDark
import kotlinx.coroutines.launch

private val ProfileBgLight = Color(0xFFE8EEF7)
private val ProfileBgDark = Color(0xFF0B1220)
private val PanelShape = RoundedCornerShape(28.dp)

private data class ProfilePopup(
    val title: String,
    val message: String,
)

@Composable
fun ProfilPage(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState(),
    session: AuthSession? = AuthRepository.currentSession,
    onConnectClick: () -> Unit = {},
    onOpenThemeSettings: () -> Unit = {},
    onOpenLanguageSettings: () -> Unit = {},
) {
    ProfilBuild(
        modifier = modifier,
        scrollVertical = scrollVertical,
        session = session,
        onConnectClick = onConnectClick,
        onOpenThemeSettings = onOpenThemeSettings,
        onOpenLanguageSettings = onOpenLanguageSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilBuild(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState(),
    session: AuthSession? = AuthRepository.currentSession,
    onConnectClick: () -> Unit = {},
    onOpenThemeSettings: () -> Unit = {},
    onOpenLanguageSettings: () -> Unit = {},
) {
    val ui = LocalAppUiController.current
    val strings = ui.settings
    var currentSession by remember(session) { mutableStateOf(session) }
    val isLoggedIn = !currentSession?.accessToken.isNullOrBlank()
    val profile = currentSession?.profile.takeIf { isLoggedIn }

    var selectableAccounts by remember { mutableStateOf<List<SelectableAccountDto>>(emptyList()) }
    var selectedAccountName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var popup by remember { mutableStateOf<ProfilePopup?>(null) }

    var email by remember(profile?.email) { mutableStateOf(profile?.email.orEmpty()) }
    var pseudo by remember(profile?.username) { mutableStateOf(profile?.username?.trimStart('@').orEmpty()) }
    var phone by remember(profile?.phone) { mutableStateOf(profile?.phone.orEmpty()) }
    var city by remember(profile?.city) { mutableStateOf(profile?.city.orEmpty()) }
    var firstName by remember(profile?.firstName) { mutableStateOf(profile?.firstName.orEmpty()) }
    var lastName by remember(profile?.lastName) { mutableStateOf(profile?.lastName.orEmpty()) }

    val scope = rememberCoroutineScope()
    val pullState = rememberPullToRefreshState()
    val pageBg = if (ui.settings.darkMode) ProfileBgDark else ProfileBgLight

    val pickPhoto = rememberFilePickerLauncher { files ->
        val image = files.firstOrNull {
            it.mimeType?.startsWith("image/") == true ||
                it.name.contains(Regex("\\.(png|jpe?g|webp|gif)$", RegexOption.IGNORE_CASE))
        } ?: files.firstOrNull()
        if (image != null) {
            currentSession = AuthRepository.updateAvatarUri(image.uri) ?: currentSession
        }
    }

    // Charge les types de compte en silence (pas de popup de chargement).
    LaunchedEffect(isLoggedIn, profile?.userId, profile?.hasAccountType) {
        if (!isLoggedIn || profile?.hasAccountType == true) return@LaunchedEffect
        AuthRepository.getSelectableAccounts().onSuccess { accounts ->
            selectableAccounts = accounts.filter {
                it.name.equals("student", ignoreCase = true) ||
                    it.name.equals("teacher", ignoreCase = true)
            }
            if (selectedAccountName.isBlank()) {
                selectedAccountName = selectableAccounts.firstOrNull()?.displayLabel.orEmpty()
            }
        }
    }

    AuthLoadingDialog(
        visible = isSaving,
        message = strings.t("Enregistrement...", "Saving...")
    )
    popup?.let { dialog ->
        AuthMessageDialog(
            title = dialog.title,
            message = dialog.message,
            onConfirm = { popup = null }
        )
    }

    fun refreshProfile() {
        if (!isLoggedIn || isRefreshing) return
        isRefreshing = true
        scope.launch {
            AuthRepository.refreshProfile()
                .onSuccess { updated ->
                    currentSession = updated
                    email = updated.profile?.email.orEmpty()
                    pseudo = updated.profile?.username?.trimStart('@').orEmpty()
                    phone = updated.profile?.phone.orEmpty()
                    city = updated.profile?.city.orEmpty()
                    firstName = updated.profile?.firstName.orEmpty()
                    lastName = updated.profile?.lastName.orEmpty()
                }
                .onFailure { error ->
                    popup = ProfilePopup(
                        title = strings.t("Actualisation", "Refresh"),
                        message = error.message
                            ?: strings.t("Impossible de rafraichir le profil", "Unable to refresh profile")
                    )
                }
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { refreshProfile() },
        state = pullState,
        modifier = modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollVertical)
                .padding(horizontal = 14.dp)
                .padding(top = 8.dp, bottom = 90.dp)
        ) {
            ProfileHeaderCard(
                profile = profile,
                isLoggedIn = isLoggedIn,
                onChangePhoto = if (isLoggedIn) pickPhoto else null,
                changePhotoLabel = strings.t("Changer la photo", "Change photo"),
            )
            Spacer(Modifier.height(16.dp))

            if (isLoggedIn) {
                AuthFormPanel {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.t("Informations du compte", "Account information"),
                            color = AuthColors.TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isEditing) {
                                strings.t("Annuler", "Cancel")
                            } else {
                                strings.t("Modifier", "Edit")
                            },
                            color = AuthColors.AccentBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    if (isEditing) {
                                        email = profile?.email.orEmpty()
                                        pseudo = profile?.username?.trimStart('@').orEmpty()
                                        phone = profile?.phone.orEmpty()
                                        city = profile?.city.orEmpty()
                                        firstName = profile?.firstName.orEmpty()
                                        lastName = profile?.lastName.orEmpty()
                                    }
                                    isEditing = !isEditing
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))

                    if (isEditing) {
                        AuthTextField(value = email, onValueChange = { email = it }, label = "Email", keyboardType = KeyboardType.Email)
                        Spacer(Modifier.height(10.dp))
                        AuthTextField(value = pseudo, onValueChange = { pseudo = it }, label = strings.t("Pseudo", "Username"))
                        Spacer(Modifier.height(10.dp))
                        AuthTextField(value = phone, onValueChange = { phone = it }, label = strings.t("Telephone", "Phone"), keyboardType = KeyboardType.Phone)
                        Spacer(Modifier.height(10.dp))
                        AuthTextField(value = city, onValueChange = { city = it }, label = strings.t("Ville", "City"))
                        Spacer(Modifier.height(10.dp))
                        AuthTextField(value = firstName, onValueChange = { firstName = it }, label = strings.t("Prenom", "First name"))
                        Spacer(Modifier.height(10.dp))
                        AuthTextField(value = lastName, onValueChange = { lastName = it }, label = strings.t("Nom", "Last name"))
                        Spacer(Modifier.height(14.dp))
                        AuthPrimaryButton(
                            text = strings.t("Enregistrer", "Save"),
                            enabled = !isSaving,
                            onClick = {
                                if (email.isBlank() || firstName.isBlank() || lastName.isBlank() || city.isBlank() || phone.isBlank() || pseudo.isBlank()) {
                                    popup = ProfilePopup(
                                        title = strings.t("Champs requis", "Required fields"),
                                        message = strings.t(
                                            "Merci de remplir email, pseudo, telephone, ville, prenom et nom.",
                                            "Please fill email, username, phone, city, first name and last name."
                                        )
                                    )
                                    return@AuthPrimaryButton
                                }
                                isSaving = true
                                scope.launch {
                                    val result = AuthRepository.updateProfile(
                                        UserRequestChange(
                                            email = email.trim(),
                                            pseudo = pseudo.trim(),
                                            phone = phone.trim(),
                                            city = city.trim(),
                                            firstName = firstName.trim(),
                                            lastName = lastName.trim(),
                                        )
                                    )
                                    isSaving = false
                                    result.onSuccess { updated ->
                                        currentSession = updated
                                        isEditing = false
                                        popup = ProfilePopup(
                                            title = strings.t("Profil mis a jour", "Profile updated"),
                                            message = strings.t(
                                                "Vos informations ont ete enregistrees.",
                                                "Your information has been saved."
                                            )
                                        )
                                    }.onFailure { error ->
                                        popup = ProfilePopup(
                                            title = strings.t("Echec", "Failed"),
                                            message = error.message
                                                ?: strings.t("Mise a jour impossible", "Update failed")
                                        )
                                    }
                                }
                            }
                        )
                    } else {
                        ProfileInfoRow(label = "Premium", value = if (profile?.premium == true) strings.t("Oui", "Yes") else strings.t("Non", "No"))
                        ProfileInfoRow(label = "Email", value = profile?.email.orDash())
                        ProfileInfoRow(label = strings.t("Nom", "Last name"), value = profile?.lastName.orDash())
                        ProfileInfoRow(label = strings.t("Prenom", "First name"), value = profile?.firstName.orDash())
                        ProfileInfoRow(label = strings.t("Pseudo", "Username"), value = profile?.username.orDash())
                        ProfileInfoRow(label = strings.t("Telephone", "Phone"), value = profile?.phone.orDash())
                        ProfileInfoRow(label = strings.t("Ville", "City"), value = profile?.city.orDash())
                        ProfileInfoRow(label = "Fullname", value = profile?.fullName.orDash())
                        ProfileInfoRow(
                            label = strings.t("Type de compte", "Account type"),
                            value = if (profile?.hasAccountType == true) {
                                profile.accountTypeLabel
                            } else {
                                strings.t("A completer", "Incomplete")
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                // Section choix type de compte uniquement si pas encore defini.
                if (profile?.hasAccountType != true) {
                    AuthFormPanel {
                        Text(
                            text = strings.t("Type de compte", "Account type"),
                            color = AuthColors.TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = strings.t(
                                "Choisissez Etudiant ou Enseignant. Ce choix est definitif.",
                                "Choose Student or Teacher. This choice is final."
                            ),
                            color = AuthColors.TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(14.dp))
                        if (selectableAccounts.isNotEmpty()) {
                            AuthChoiceChips(
                                label = strings.t("Type de compte", "Account type"),
                                options = selectableAccounts.map { it.displayLabel },
                                selected = selectedAccountName.ifBlank {
                                    selectableAccounts.first().displayLabel
                                },
                                onSelected = { selectedAccountName = it }
                            )
                            Spacer(Modifier.height(14.dp))
                            AuthPrimaryButton(
                                text = strings.t("Valider mon type de compte", "Confirm account type"),
                                enabled = !isSaving && selectedAccountName.isNotBlank(),
                                onClick = {
                                    val account = selectableAccounts.firstOrNull {
                                        it.displayLabel.equals(selectedAccountName, ignoreCase = true)
                                    } ?: return@AuthPrimaryButton
                                    isSaving = true
                                    scope.launch {
                                        val result = AuthRepository.selectAccountType(account)
                                        isSaving = false
                                        result.onSuccess { updated ->
                                            currentSession = updated
                                            popup = ProfilePopup(
                                                title = strings.t("Profil complete", "Profile completed"),
                                                message = strings.t(
                                                    "Type de compte enregistre : ${updated.profile?.accountTypeLabel ?: account.displayLabel}.",
                                                    "Account type saved: ${updated.profile?.accountTypeLabel ?: account.displayLabel}."
                                                )
                                            )
                                        }.onFailure { error ->
                                            currentSession = AuthRepository.currentSession
                                            popup = ProfilePopup(
                                                title = strings.t("Echec", "Failed"),
                                                message = error.message
                                                    ?: strings.t(
                                                        "Impossible d'enregistrer le type de compte",
                                                        "Unable to save account type"
                                                    )
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                AuthFormPanel {
                    Text(
                        text = strings.t("Parametres", "Settings"),
                        color = AuthColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = strings.t(
                            "Gerez le theme et la langue dans leurs pages dediees.",
                            "Manage theme and language on their dedicated pages."
                        ),
                        color = AuthColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    AuthPrimaryButton(
                        text = strings.t("Mode d'affichage", "Display mode"),
                        onClick = onOpenThemeSettings,
                    )
                    Spacer(Modifier.height(10.dp))
                    AuthPrimaryButton(
                        text = strings.t("Langue", "Language"),
                        onClick = onOpenLanguageSettings,
                    )
                }

                Spacer(Modifier.height(16.dp))
                AuthFormPanel {
                    Text(
                        text = strings.t("Session", "Session"),
                        color = AuthColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = strings.t(
                            "Tirez vers le bas pour actualiser le profil. Deconnectez-vous pour masquer vos informations.",
                            "Pull down to refresh your profile. Sign out to hide your information."
                        ),
                        color = AuthColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    AuthPrimaryButton(
                        text = strings.t("Deconnecter", "Sign out"),
                        onClick = {
                            AuthRepository.clearSession()
                            currentSession = null
                            selectableAccounts = emptyList()
                            selectedAccountName = ""
                            isEditing = false
                        }
                    )
                }
            } else {
                AuthFormPanel {
                    Text(
                        text = strings.t("Connectez-vous", "Sign in"),
                        color = AuthColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = strings.t(
                            "Connectez-vous pour y acceder.",
                            "Sign in to access your profile."
                        ),
                        color = AuthColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    AuthPrimaryButton(
                        text = strings.t("Se connecter", "Sign in"),
                        onClick = onConnectClick
                    )
                }

                Spacer(Modifier.height(16.dp))
                AuthFormPanel {
                    Text(
                        text = strings.t("Parametres", "Settings"),
                        color = AuthColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    AuthPrimaryButton(
                        text = strings.t("Mode d'affichage", "Display mode"),
                        onClick = onOpenThemeSettings,
                    )
                    Spacer(Modifier.height(10.dp))
                    AuthPrimaryButton(
                        text = strings.t("Langue", "Language"),
                        onClick = onOpenLanguageSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    profile: AuthUserProfile?,
    isLoggedIn: Boolean,
    onChangePhoto: (() -> Unit)?,
    changePhotoLabel: String,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        BlueDark.copy(alpha = 0.96f),
                        Color(0xFF08092B).copy(alpha = 0.94f)
                    )
                )
            )
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    ProfilePhotoAvatar(
                        uri = profile?.avatarUri,
                        initials = if (isLoggedIn) profileInitials(profile) else "LA",
                        size = 64.dp,
                    )
                    if (onChangePhoto != null) {
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable(onClick = onChangePhoto),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = BlueDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
                if (onChangePhoto != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = changePhotoLabel,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onChangePhoto)
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = "LawApp50",
                    color = Color.White.copy(alpha = 0.72f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = if (isLoggedIn) profile?.fullName ?: "Utilisateur LawApp" else "Profil",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Text(
                    text = if (isLoggedIn) {
                        profile?.displayHandle ?: "@lawapp_member"
                    } else {
                        "Connectez-vous pour y acceder"
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
        if (isLoggedIn) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileBadge(
                    label = "Premium",
                    value = if (profile?.premium == true) "Actif" else "Standard",
                    modifier = Modifier.weight(1f)
                )
                ProfileBadge(
                    label = "Compte",
                    value = when {
                        profile?.hasAccountType == true -> profile.accountTypeLabel
                        profile?.certified == true -> "Certifie"
                        else -> "A completer"
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, AuthColors.Border, RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AuthColors.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = AuthColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

private fun profileInitials(profile: AuthUserProfile?): String {
    val prenom = profile?.firstName?.trim()?.firstOrNull()?.uppercaseChar()
    val nom = profile?.lastName?.trim()?.firstOrNull()?.uppercaseChar()
    val username = profile?.username?.trim()?.trimStart('@').orEmpty()
    return when {
        prenom != null && nom != null -> "$prenom$nom"
        prenom != null -> "$prenom"
        username.isNotBlank() -> username.take(2).uppercase()
        else -> "LA"
    }
}

private fun String?.orDash(): String = this?.takeIf { it.isNotBlank() } ?: "—"

@Composable
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
fun ProfilPreview() {
    ProfilBuild()
}
