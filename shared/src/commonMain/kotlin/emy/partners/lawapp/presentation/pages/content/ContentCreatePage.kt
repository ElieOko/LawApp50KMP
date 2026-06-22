package emy.partners.lawapp.presentation.pages.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import emy.partners.lawapp.domain.models.ContentAttachment
import emy.partners.lawapp.domain.models.ContentDestination
import emy.partners.lawapp.domain.models.UserGeneratedContentDraft
import emy.partners.lawapp.presentation.components.basics.PickedFile
import emy.partners.lawapp.presentation.components.basics.PlatformPdfViewer
import emy.partners.lawapp.presentation.components.basics.rememberFilePickerLauncher
import emy.partners.lawapp.presentation.themes.BlueDark
import emy.partners.lawapp.presentation.themes.BlueDarkEffect
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.explore
import lawapp.shared.generated.resources.house
import org.jetbrains.compose.resources.painterResource

@Composable
fun ContentCreatePage(
    modifier: Modifier = Modifier,
    initialDestination: ContentDestination = ContentDestination.Home,
    onBack: () -> Unit = {},
    onPublish: (UserGeneratedContentDraft) -> Unit = {}
) {
    var step by remember { mutableIntStateOf(0) }
    var destination by remember { mutableStateOf(initialDestination) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("Moi") }
    var link by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf<String?>(null) }
    val selectedFiles = remember { mutableStateListOf<PickedFile>() }
    val selectedFile = selectedFiles.firstOrNull()
    val canPublish = title.isNotBlank() && description.isNotBlank() && author.isNotBlank()
    val launcher = rememberFilePickerLauncher { files ->
        if (files.isNotEmpty()) {
            selectedFiles.clear()
            selectedFiles.add(files.first())
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "< Retour",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White.copy(alpha = 0.14f))
                .clickable(onClick = onBack)
                .padding(horizontal = 14.dp, vertical = 9.dp)
        )
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF2563EB).copy(alpha = 0.95f),
                            BlueDark.copy(alpha = 0.95f),
                            BlueDarkEffect.copy(alpha = 0.94f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = if (step == 0) "Choisis d'abord la page cible" else "Formulaire de contenu",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (step == 0) {
                        "Precise si tu publies sur Home ou Explore avant de remplir le formulaire."
                    } else {
                        "Complete les champs et valide avec un preview en direct."
                    },
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 14.sp
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        StepPill(currentStep = step)
        Spacer(Modifier.height(14.dp))

        if (step == 0) {
            DestinationChoiceCard(
                title = "Publier sur Home",
                description = "Ideal pour un post rapide multimedia dans le flux principal.",
                selected = destination == ContentDestination.Home,
                icon = Res.drawable.house,
                onClick = { destination = ContentDestination.Home }
            )
            Spacer(Modifier.height(10.dp))
            DestinationChoiceCard(
                title = "Publier sur Explore",
                description = "Ideal pour un contenu de decouverte, guide ou fiche article.",
                selected = destination == ContentDestination.Explore,
                icon = Res.drawable.explore,
                onClick = { destination = ContentDestination.Explore }
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { step = 1 },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BlueDark)
            ) {
                Text("Continuer vers le formulaire", fontWeight = FontWeight.ExtraBold)
            }
        } else {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    ModernField("Titre *", title, { title = it }, true, 56, "Ex: Nouveau cas pratique")
                    ModernField(
                        "Description *",
                        description,
                        { description = it },
                        false,
                        100,
                        "Decris ton contenu..."
                    )
                    ModernField("Auteur *", author, { author = it }, true, 56, "Nom")
                    ModernField("Lien (optionnel)", link, { link = it }, true, 56, "https://...")
                    Text(
                        "Piece jointe (optionnelle)",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = launcher,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueDark)
                        ) {
                            Text("Ajouter un fichier")
                        }
                        if (selectedFile != null) {
                            OutlinedButton(onClick = { selectedFiles.clear() }, shape = RoundedCornerShape(12.dp)) {
                                Text("Retirer")
                            }
                        }
                    }
                    if (selectedFile != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            selectedFile.name,
                            color = Color(0xFF334155),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            PreviewCard(
                title = title.ifBlank { "Titre du contenu" },
                description = description.ifBlank { "Le texte du contenu apparaitra ici." },
                author = author.ifBlank { "Auteur" },
                destination = destination,
                file = selectedFile
            )
            savedMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { step = 0 },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Precedent", color = Color.White)
                }
                Button(
                    onClick = {
                        if (!canPublish) {
                            savedMessage = "Complete titre, description et auteur."
                            return@Button
                        }
                        onPublish(
                            UserGeneratedContentDraft(
                                destination = destination,
                                title = title.trim(),
                                description = description.trim(),
                                author = author.trim(),
                                link = link.takeIf { it.isNotBlank() }?.trim(),
                                attachment = selectedFile?.toAttachment()
                            )
                        )
                    },
                    enabled = canPublish,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = BlueDark
                    )
                ) {
                    Text(
                        "Publier sur ${if (destination == ContentDestination.Home) "Home" else "Explore"}",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun StepPill(currentStep: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StepSmallChip("1. Choix de page", selected = currentStep == 0, completed = currentStep > 0)
        StepSmallChip("2. Formulaire + Preview", selected = currentStep == 1, completed = false)
    }
}

@Composable
private fun StepSmallChip(label: String, selected: Boolean, completed: Boolean) {
    val background = when {
        completed -> Color(0xFF1D4ED8)
        selected -> Color.White
        else -> Color.White.copy(alpha = 0.18f)
    }
    val textColor = when {
        completed -> Color.White
        selected -> BlueDark
        else -> Color.White
    }
    Text(
        text = label,
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(40.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun DestinationChoiceCard(
    title: String,
    description: String,
    selected: Boolean,
    icon: org.jetbrains.compose.resources.DrawableResource,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (selected) Color(0xFFEFF6FF) else Color.White)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = if (selected) BlueDark else Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                Text(title, color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text(description, color = Color(0xFF64748B), fontSize = 12.sp, lineHeight = 15.sp)
            }
            Text(
                if (selected) "Selectionne" else "Choisir",
                color = if (selected) Color(0xFF1D4ED8) else Color(0xFF475569),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ModernField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    minHeight: Int,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight.dp)
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Black.copy(alpha = 0.2f),
            focusedBorderColor = Color(0xFF2563EB)
        )
    )
}

@Composable
private fun PreviewCard(
    title: String,
    description: String,
    author: String,
    destination: ContentDestination,
    file: PickedFile?
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Preview", color = Color(0xFF2563EB), fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(
                if (destination == ContentDestination.Home) "Publication Home" else "Publication Explore",
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            Text(description, color = Color(0xFF334155), fontSize = 13.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(8.dp))
            Text("Par $author", color = Color(0xFF64748B), fontSize = 12.sp)
            if (file != null) {
                Spacer(Modifier.height(10.dp))
                if (file.isImageLike()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(file.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                    )
                } else if (file.isPdfLike()) {
                    PlatformPdfViewer(
                        uri = file.uri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(10.dp)
                    ) {
                        Text(
                            "Fichier: ${file.name}",
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private fun PickedFile.isImageLike(): Boolean {
    val mime = mimeType.orEmpty().lowercase()
    val uriValue = uri.lowercase()
    return mime.startsWith("image/") ||
        uriValue.endsWith(".png") ||
        uriValue.endsWith(".jpg") ||
        uriValue.endsWith(".jpeg") ||
        uriValue.endsWith(".webp")
}

private fun PickedFile.isPdfLike(): Boolean {
    val mime = mimeType.orEmpty().lowercase()
    val uriValue = uri.lowercase()
    return mime == "application/pdf" || uriValue.endsWith(".pdf")
}

private fun PickedFile.toAttachment() = ContentAttachment(
    name = name,
    uri = uri,
    mimeType = mimeType
)

