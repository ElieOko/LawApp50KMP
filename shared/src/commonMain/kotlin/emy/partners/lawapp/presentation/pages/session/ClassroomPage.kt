package emy.partners.lawapp.presentation.pages.session

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.remote.classroom.ClassroomMember
import emy.partners.lawapp.data.remote.classroom.ClassroomRepository
import emy.partners.lawapp.data.remote.classroom.ClassroomSummary
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel
import emy.partners.lawapp.presentation.pages.auth.AuthLoadingDialog
import emy.partners.lawapp.presentation.pages.auth.AuthMessageDialog
import emy.partners.lawapp.presentation.pages.auth.AuthPrimaryButton
import emy.partners.lawapp.presentation.pages.auth.AuthTextField
import kotlinx.coroutines.launch

@Composable
fun ClassroomPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState(),
) {
    val scope = rememberCoroutineScope()
    var classrooms by remember { mutableStateOf(ClassroomRepository.cachedMine()) }
    var selected by remember { mutableStateOf<ClassroomSummary?>(null) }
    var members by remember { mutableStateOf<List<ClassroomMember>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var memberUserId by remember { mutableStateOf("") }
    var isWorking by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf<String?>(null) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    fun loadMembers(classroom: ClassroomSummary) {
        scope.launch {
            ClassroomRepository.loadMembers(classroom.id)
                .onSuccess { members = it }
                .onFailure {
                    dialogTitle = "Presence"
                    dialogMessage = it.message ?: "Impossible de charger les membres."
                }
        }
    }

    LaunchedEffect(Unit) {
        ClassroomRepository.loadMine(forceRefresh = true)
            .onSuccess { classrooms = it }
            .onFailure {
                if (classrooms.isEmpty()) {
                    dialogTitle = "Salles"
                    dialogMessage = it.message ?: "Impossible de charger les salles."
                }
            }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(Color(0xFFE8EEF7))
            .verticalScroll(scrollVertical)
            .padding(14.dp)
    ) {
        Text(
            "< Retour",
            color = AuthColors.AccentBright,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .clickable(onClick = onBack)
                .padding(horizontal = 14.dp, vertical = 9.dp),
        )
        Spacer(Modifier.height(14.dp))
        AuthFormPanel {
            Text("Salles de classe", color = AuthColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "Creez une salle, ajoutez des etudiants (userId) et suivez la presence.",
                color = AuthColors.TextSecondary,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(14.dp))
            AuthTextField(value = name, onValueChange = { name = it }, label = "Nom de la salle *")
            Spacer(Modifier.height(8.dp))
            AuthTextField(value = description, onValueChange = { description = it }, label = "Description")
            Spacer(Modifier.height(10.dp))
            AuthPrimaryButton(
                text = "Creer la salle",
                enabled = !isWorking && name.isNotBlank(),
                onClick = {
                    isWorking = true
                    scope.launch {
                        ClassroomRepository.createClassroom(name, description)
                            .onSuccess {
                                classrooms = ClassroomRepository.cachedMine()
                                selected = it
                                name = ""
                                description = ""
                                loadMembers(it)
                            }
                            .onFailure {
                                dialogTitle = "Creation impossible"
                                dialogMessage = it.message
                            }
                        isWorking = false
                    }
                },
            )
        }
        Spacer(Modifier.height(14.dp))
        AuthFormPanel {
            Text("Mes salles", color = AuthColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(Modifier.height(10.dp))
            if (classrooms.isEmpty()) {
                Text("Aucune salle pour le moment.", color = AuthColors.TextSecondary)
            } else {
                classrooms.forEach { room ->
                    val active = selected?.id == room.id
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (active) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                            .clickable {
                                selected = room
                                loadMembers(room)
                            }
                            .padding(12.dp)
                    ) {
                        Text(room.name, fontWeight = FontWeight.ExtraBold, color = AuthColors.TextPrimary)
                        if (room.description.isNotBlank()) {
                            Text(room.description, color = AuthColors.TextSecondary, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        selected?.let { room ->
            Spacer(Modifier.height(14.dp))
            AuthFormPanel {
                Text("Presence — ${room.name}", color = AuthColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "${members.count { it.present }} present(s) / ${members.size} membre(s)",
                    color = AuthColors.TextSecondary,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(12.dp))
                members.forEach { member ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Utilisateur #${member.userId}", color = AuthColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (member.present) "Present" else "Absent",
                            color = if (member.present) Color(0xFF059669) else AuthColors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
                Spacer(Modifier.height(8.dp))
                AuthTextField(
                    value = memberUserId,
                    onValueChange = { memberUserId = it.filter { ch -> ch.isDigit() } },
                    label = "Ajouter un userId",
                    keyboardType = KeyboardType.Number,
                )
                Spacer(Modifier.height(10.dp))
                AuthPrimaryButton(
                    text = "Ajouter a la salle",
                    enabled = !isWorking && memberUserId.isNotBlank(),
                    onClick = {
                        val userId = memberUserId.toLongOrNull() ?: return@AuthPrimaryButton
                        isWorking = true
                        scope.launch {
                            ClassroomRepository.addMember(room.id, userId)
                                .onSuccess {
                                    memberUserId = ""
                                    loadMembers(room)
                                }
                                .onFailure {
                                    dialogTitle = "Ajout impossible"
                                    dialogMessage = it.message
                                }
                            isWorking = false
                        }
                    },
                )
            }
        }
        AuthLoadingDialog(visible = isWorking, message = "Traitement...")
        if (dialogTitle != null && dialogMessage != null) {
            AuthMessageDialog(
                title = dialogTitle.orEmpty(),
                message = dialogMessage.orEmpty(),
                onConfirm = {
                    dialogTitle = null
                    dialogMessage = null
                },
            )
        }
        Spacer(Modifier.height(80.dp))
    }
}
