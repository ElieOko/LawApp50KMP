package emy.partners.lawapp.presentation.pages.content

import VideoPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import emy.partners.lawapp.data.remote.auth.AuthRepository
import emy.partners.lawapp.data.remote.contenu.ContenuRepository
import emy.partners.lawapp.data.remote.contenu.IMAGE_TYPE_CONTENU_ID
import emy.partners.lawapp.data.remote.contenu.SCOPE_FREE_ID
import emy.partners.lawapp.data.remote.contenu.SCOPE_PREMIUM_ID
import emy.partners.lawapp.data.remote.contenu.VIDEO_TYPE_CONTENU_ID
import emy.partners.lawapp.data.remote.contenu.resolveTypeContenuId
import emy.partners.lawapp.domain.models.ContentAttachment
import emy.partners.lawapp.domain.models.ContentDestination
import emy.partners.lawapp.domain.models.UserGeneratedContentDraft
import emy.partners.lawapp.presentation.components.basics.PickedFile
import emy.partners.lawapp.presentation.components.basics.rememberFilePickerLauncher
import emy.partners.lawapp.presentation.pages.auth.AuthBrandHeader
import emy.partners.lawapp.presentation.pages.auth.AuthChoiceChips
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel
import emy.partners.lawapp.presentation.pages.auth.AuthLoadingDialog
import emy.partners.lawapp.presentation.pages.auth.AuthMessageDialog
import emy.partners.lawapp.presentation.pages.auth.AuthPrimaryButton
import emy.partners.lawapp.presentation.pages.auth.AuthTextField
import emy.partners.lawapp.presentation.settings.LocalAppUiController
import kotlinx.coroutines.launch
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.close
import lawapp.shared.generated.resources.ic_add_media
import lawapp.shared.generated.resources.ic_change_media
import org.jetbrains.compose.resources.painterResource

private val PageBgLight = Color(0xFFE8EEF7)
private val PageBgDark = Color(0xFF0B1220)

private data class PublishPopup(
    val title: String,
    val message: String,
    val success: Boolean = false,
)

@Composable
fun ContentCreatePage(
    modifier: Modifier = Modifier,
    initialDestination: ContentDestination = ContentDestination.Home,
    onBack: () -> Unit = {},
    onPublish: (UserGeneratedContentDraft) -> Unit = {},
) {
    val ui = LocalAppUiController.current
    val pageBg = if (ui.settings.darkMode) PageBgDark else PageBgLight
    val scope = rememberCoroutineScope()
    val session = AuthRepository.currentSession
    val authorName = remember(session) {
        session?.profile?.let { profile ->
            listOfNotNull(
                profile.firstName?.takeIf { it.isNotBlank() },
                profile.lastName?.takeIf { it.isNotBlank() },
            ).joinToString(" ").ifBlank {
                profile.username?.trimStart('@').orEmpty()
            }
        }.orEmpty().ifBlank { "Moi" }
    }

    var destination by remember { mutableStateOf(initialDestination) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var scopeLabel by remember { mutableStateOf("Free") }
    var selectedFile by remember { mutableStateOf<PickedFile?>(null) }
    var isPublishing by remember { mutableStateOf(false) }
    var popup by remember { mutableStateOf<PublishPopup?>(null) }

    val typeContenuId = remember(selectedFile) {
        resolveTypeContenuId(selectedFile?.mimeType, selectedFile?.name)
    }
    val typeLabel = when (typeContenuId) {
        VIDEO_TYPE_CONTENU_ID -> "Video (type 3)"
        IMAGE_TYPE_CONTENU_ID -> "Image (type 2)"
        else -> "Texte (type 1)"
    }
    val scopeId = if (scopeLabel.equals("Premium", ignoreCase = true)) {
        SCOPE_PREMIUM_ID
    } else {
        SCOPE_FREE_ID
    }
    val canPublish =
        title.isNotBlank() && description.isNotBlank() && selectedFile != null && !isPublishing

    val pickMedia = rememberFilePickerLauncher { files ->
        val media = files.firstOrNull { file ->
            file.isImageLike() || file.isVideoLike()
        } ?: files.firstOrNull()
        selectedFile = media
    }

    AuthLoadingDialog(
        visible = isPublishing,
        message = "Publication en cours...",
    )
    popup?.let { dialog ->
        AuthMessageDialog(
            title = dialog.title,
            message = dialog.message,
            onConfirm = {
                val wasSuccess = dialog.success
                val draftTitle = title
                val draftDescription = description
                val draftFile = selectedFile
                val draftDestination = destination
                popup = null
                if (wasSuccess) {
                    onPublish(
                        UserGeneratedContentDraft(
                            destination = draftDestination,
                            title = draftTitle.trim(),
                            description = draftDescription.trim(),
                            author = authorName,
                            attachment = draftFile?.toAttachment(),
                        )
                    )
                }
            }
        )
    }

    Column(
        modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .padding(top = 12.dp, bottom = 28.dp)
    ) {
        AuthBrandHeader(
            title = "Publier un contenu",
            subtitle = "Ajoute une image ou une video, precise le titre et la description, puis choisis Free ou Premium.",
            onBack = onBack,
        )
        Spacer(Modifier.height(16.dp))

        AuthFormPanel {
            Text(
                text = "Destination",
                color = AuthColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
            )
            Spacer(Modifier.height(10.dp))
            AuthChoiceChips(
                label = "Ou publier apres envoi",
                options = listOf("Home", "Explore"),
                selected = if (destination == ContentDestination.Home) "Home" else "Explore",
                onSelected = {
                    destination = if (it == "Explore") {
                        ContentDestination.Explore
                    } else {
                        ContentDestination.Home
                    }
                },
            )
            Spacer(Modifier.height(16.dp))

            AuthTextField(
                value = title,
                onValueChange = { title = it },
                label = "Titre *",
            )
            Spacer(Modifier.height(10.dp))
            PublishMultilineField(
                value = description,
                onValueChange = { description = it },
                label = "Description *",
            )
            Spacer(Modifier.height(14.dp))

            AuthChoiceChips(
                label = "Scope",
                options = listOf("Free", "Premium"),
                selected = scopeLabel,
                onSelected = { scopeLabel = it },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Scope ${if (scopeId == SCOPE_PREMIUM_ID) "2 · Premium" else "1 · Free"}",
                color = AuthColors.TextSecondary,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Type de contenu : $typeLabel",
                color = AuthColors.AccentBright,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Media (image ou video) *",
                color = AuthColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Obligatoire : le serveur refuse une publication sans fichier.",
                color = AuthColors.TextSecondary,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (selectedFile == null) {
                    IconButton(onClick = pickMedia) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add_media),
                            contentDescription = "Ajouter un media",
                            tint = AuthColors.AccentBright,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                } else {
                    IconButton(onClick = pickMedia) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_change_media),
                            contentDescription = "Changer le media",
                            tint = AuthColors.AccentBright,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    IconButton(onClick = { selectedFile = null }) {
                        Icon(
                            painter = painterResource(Res.drawable.close),
                            contentDescription = "Retirer le media",
                            tint = Color(0xFFB91C1C),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            selectedFile?.let { file ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = file.name,
                    color = AuthColors.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(10.dp))
                MediaPreview(file = file)
            }

            Spacer(Modifier.height(18.dp))
            AuthPrimaryButton(
                text = "Publier",
                enabled = canPublish,
                onClick = {
                    val currentSession = session
                    val userId = currentSession?.profile?.userId
                    val media = selectedFile
                    if (currentSession == null || userId == null || currentSession.accessToken.isBlank()) {
                        popup = PublishPopup(
                            title = "Connexion requise",
                            message = "Connectez-vous depuis le profil pour publier un contenu.",
                        )
                        return@AuthPrimaryButton
                    }
                    if (media == null) {
                        popup = PublishPopup(
                            title = "Media requis",
                            message = "Ajoutez une image ou une video avant de publier.",
                        )
                        return@AuthPrimaryButton
                    }
                    if (title.isBlank() || description.isBlank()) {
                        popup = PublishPopup(
                            title = "Champs requis",
                            message = "Merci de renseigner un titre et une description.",
                        )
                        return@AuthPrimaryButton
                    }
                    isPublishing = true
                    scope.launch {
                        ContenuRepository.publishContenu(
                            title = title,
                            description = description,
                            scopeId = scopeId,
                            fileName = media.name,
                            fileMimeType = media.mimeType,
                            fileUri = media.uri,
                        ).onSuccess {
                            popup = PublishPopup(
                                title = "Publie",
                                message = "Contenu envoye. Faites un pull-to-refresh sur Home pour le voir dans le fil.",
                                success = true,
                            )
                        }.onFailure { error ->
                            popup = PublishPopup(
                                title = "Publication",
                                message = error.message ?: "Impossible de publier le contenu.",
                            )
                        }
                        isPublishing = false
                    }
                },
            )
        }
    }
}

@Composable
private fun PublishMultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        minLines = 4,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AuthColors.TextPrimary,
            unfocusedTextColor = AuthColors.TextPrimary,
            focusedBorderColor = AuthColors.AccentBright,
            unfocusedBorderColor = AuthColors.Border,
            cursorColor = AuthColors.AccentBright,
            focusedLabelColor = AuthColors.AccentBright,
            unfocusedLabelColor = AuthColors.TextSecondary,
            focusedContainerColor = AuthColors.Field,
            unfocusedContainerColor = AuthColors.Field,
        ),
    )
}

@Composable
private fun MediaPreview(file: PickedFile) {
    when {
        file.isVideoLike() -> {
            VideoPlayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(18.dp)),
                url = file.uri,
                autoPlay = false,
                showControls = true,
            )
        }
        file.isImageLike() -> {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(file.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = file.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .clip(RoundedCornerShape(18.dp)),
            )
        }
        else -> {
            Text(
                text = "Fichier: ${file.name}",
                color = AuthColors.TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }
    }
}

private fun PickedFile.isImageLike(): Boolean {
    val mime = mimeType.orEmpty().lowercase()
    val uriValue = uri.lowercase()
    val nameValue = name.lowercase()
    return mime.startsWith("image/") ||
        uriValue.endsWith(".png") || uriValue.endsWith(".jpg") ||
        uriValue.endsWith(".jpeg") || uriValue.endsWith(".webp") ||
        uriValue.endsWith(".gif") ||
        nameValue.endsWith(".png") || nameValue.endsWith(".jpg") ||
        nameValue.endsWith(".jpeg") || nameValue.endsWith(".webp") ||
        nameValue.endsWith(".gif")
}

private fun PickedFile.isVideoLike(): Boolean {
    val mime = mimeType.orEmpty().lowercase()
    val uriValue = uri.lowercase()
    val nameValue = name.lowercase()
    return mime.startsWith("video/") ||
        uriValue.endsWith(".mp4") || uriValue.endsWith(".mov") ||
        uriValue.endsWith(".webm") || uriValue.endsWith(".mkv") ||
        uriValue.endsWith(".avi") ||
        nameValue.endsWith(".mp4") || nameValue.endsWith(".mov") ||
        nameValue.endsWith(".webm") || nameValue.endsWith(".mkv") ||
        nameValue.endsWith(".avi")
}

private fun PickedFile.toAttachment() = ContentAttachment(
    name = name,
    uri = uri,
    mimeType = mimeType,
)
