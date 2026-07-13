package emy.partners.lawapp.presentation.pages

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.remote.auth.AuthRepository
import emy.partners.lawapp.data.remote.auth.AuthSession
import emy.partners.lawapp.data.remote.auth.AuthUserProfile
import emy.partners.lawapp.data.remote.auth.SelectableAccountDto
import emy.partners.lawapp.presentation.pages.auth.AuthChoiceChips
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel
import emy.partners.lawapp.presentation.pages.auth.AuthLoadingDialog
import emy.partners.lawapp.presentation.pages.auth.AuthMessageDialog
import emy.partners.lawapp.presentation.pages.auth.AuthPrimaryButton
import emy.partners.lawapp.presentation.themes.BlueDark
import kotlinx.coroutines.launch

private val ProfileBg = Color(0xFFE8EEF7)
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
) {
    ProfilBuild(
        modifier = modifier,
        scrollVertical = scrollVertical,
        session = session,
        onConnectClick = onConnectClick,
    )
}

@Composable
fun ProfilBuild(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState(),
    session: AuthSession? = AuthRepository.currentSession,
    onConnectClick: () -> Unit = {},
) {
    var currentSession by remember(session) { mutableStateOf(session) }
    // Infos visibles uniquement avec une vraie session (token).
    val isLoggedIn = !currentSession?.accessToken.isNullOrBlank()
    val profile = currentSession?.profile.takeIf { isLoggedIn }

    var selectableAccounts by remember { mutableStateOf<List<SelectableAccountDto>>(emptyList()) }
    var selectedAccountName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("Chargement...") }
    var popup by remember { mutableStateOf<ProfilePopup?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isLoggedIn, profile?.userId, profile?.hasAccountType) {
        if (!isLoggedIn || profile?.hasAccountType == true) return@LaunchedEffect
        isLoading = true
        loadingMessage = "Chargement des types de compte..."
        val result = AuthRepository.getSelectableAccounts()
        isLoading = false
        result.onSuccess { accounts ->
            selectableAccounts = accounts.filter {
                it.name.equals("student", ignoreCase = true) ||
                    it.name.equals("teacher", ignoreCase = true)
            }
            if (selectedAccountName.isBlank()) {
                selectedAccountName = selectableAccounts.firstOrNull()?.displayLabel.orEmpty()
            }
        }.onFailure { error ->
            popup = ProfilePopup(
                title = "Types de compte",
                message = error.message ?: "Impossible de charger les types de compte"
            )
        }
    }

    AuthLoadingDialog(visible = isLoading, message = loadingMessage)
    popup?.let { dialog ->
        AuthMessageDialog(
            title = dialog.title,
            message = dialog.message,
            onConfirm = { popup = null }
        )
    }

    Column(
        modifier
            .fillMaxSize()
            .background(ProfileBg)
            .verticalScroll(scrollVertical)
            .padding(horizontal = 14.dp)
            .padding(top = 8.dp, bottom = 90.dp)
    ) {
        ProfileHeaderCard(profile = profile, isLoggedIn = isLoggedIn)
        Spacer(Modifier.height(16.dp))

        if (isLoggedIn) {
            AuthFormPanel {
                Text(
                    text = "Informations du compte",
                    color = AuthColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(14.dp))
                ProfileInfoRow(label = "Premium", value = if (profile?.premium == true) "Oui" else "Non")
                ProfileInfoRow(label = "Email", value = profile?.email.orDash())
                ProfileInfoRow(label = "Nom", value = profile?.lastName.orDash())
                ProfileInfoRow(label = "Prenom", value = profile?.firstName.orDash())
                ProfileInfoRow(label = "Pseudo", value = profile?.username.orDash())
                ProfileInfoRow(label = "Telephone", value = profile?.phone.orDash())
                ProfileInfoRow(label = "Ville", value = profile?.city.orDash())
                ProfileInfoRow(label = "Fullname", value = profile?.fullName.orDash())
                ProfileInfoRow(
                    label = "Type de compte",
                    value = if (profile?.hasAccountType == true) profile.accountTypeLabel else "A completer"
                )
            }

            Spacer(Modifier.height(16.dp))
            AuthFormPanel {
                Text(
                    text = "Completer le profil",
                    color = AuthColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(8.dp))
                if (profile?.hasAccountType == true) {
                    Text(
                        text = "Votre type de compte est defini et ne peut plus etre modifie.",
                        color = AuthColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    ProfileInfoRow(label = "Compte", value = profile.accountTypeLabel)
                } else {
                    Text(
                        text = "Choisissez Etudiant ou Enseignant. Ce choix est definitif.",
                        color = AuthColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    if (selectableAccounts.isNotEmpty()) {
                        AuthChoiceChips(
                            label = "Type de compte",
                            options = selectableAccounts.map { it.displayLabel },
                            selected = selectedAccountName.ifBlank {
                                selectableAccounts.first().displayLabel
                            },
                            onSelected = { selectedAccountName = it }
                        )
                        Spacer(Modifier.height(14.dp))
                        AuthPrimaryButton(
                            text = "Valider mon type de compte",
                            enabled = !isLoading && selectedAccountName.isNotBlank(),
                            onClick = {
                                val account = selectableAccounts.firstOrNull {
                                    it.displayLabel.equals(selectedAccountName, ignoreCase = true)
                                }
                                if (account == null) {
                                    popup = ProfilePopup(
                                        title = "Selection requise",
                                        message = "Choisissez Etudiant ou Enseignant."
                                    )
                                    return@AuthPrimaryButton
                                }
                                loadingMessage = "Enregistrement du type de compte..."
                                isLoading = true
                                scope.launch {
                                    val result = AuthRepository.selectAccountType(account)
                                    isLoading = false
                                    result.onSuccess { updated ->
                                        currentSession = updated
                                        popup = ProfilePopup(
                                            title = "Profil complete",
                                            message = "Type de compte enregistre : ${updated.profile?.accountTypeLabel ?: account.displayLabel}."
                                        )
                                    }.onFailure { error ->
                                        popup = ProfilePopup(
                                            title = "Echec",
                                            message = error.message ?: "Impossible d'enregistrer le type de compte"
                                        )
                                        currentSession = AuthRepository.currentSession
                                    }
                                }
                            }
                        )
                    } else {
                        Text(
                            text = "Aucun type de compte disponible pour le moment.",
                            color = AuthColors.TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            AuthFormPanel {
                Text(
                    text = "Session",
                    color = AuthColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Deconnectez-vous pour masquer et verrouiller vos informations locales.",
                    color = AuthColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(14.dp))
                AuthPrimaryButton(
                    text = "Deconnecter",
                    onClick = {
                        AuthRepository.clearSession()
                        currentSession = null
                        selectableAccounts = emptyList()
                        selectedAccountName = ""
                    }
                )
            }
        } else {
            AuthFormPanel {
                Text(
                    text = "Connectez-vous",
                    color = AuthColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Connectez-vous pour y acceder. Vos informations (premium, email, nom, prenom, pseudo, telephone, ville) restent cachees tant que vous n'etes pas connecte.",
                    color = AuthColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(14.dp))
                AuthPrimaryButton(
                    text = "Se connecter",
                    onClick = onConnectClick
                )
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    profile: AuthUserProfile?,
    isLoggedIn: Boolean,
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
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLoggedIn) profileInitials(profile) else "LA",
                    color = BlueDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
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
