package emy.partners.lawapp.presentation.components.basics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import emy.partners.lawapp.domain.models.Comment
import emy.partners.lawapp.domain.models.User
import emy.partners.lawapp.presentation.themes.BlueDark
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.close
import lawapp.shared.generated.resources.filter
import lawapp.shared.generated.resources.one
import org.jetbrains.compose.resources.painterResource

@Composable
@Preview(showBackground = true)
fun Comment(
    comments: List<Comment> = emptyList(),
    onClose: () -> Unit = {},
    canComment: Boolean = false,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onSubmitComment: (String) -> Unit = {},
    onLoginRequired: () -> Unit = {},
) {
    var draft by remember { mutableStateOf("") }

    Box {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp
            ),
            modifier = Modifier
                .zIndex(4f)
                .background(color = Color.White)
                .fillMaxHeight(0.72f)
                .align(Alignment.BottomCenter)
        ) {
            Column(Modifier.fillMaxWidth().height(420.dp).padding(10.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(Modifier.weight(0.1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text(
                                "${comments.size} commentaires",
                                fontWeight = FontWeight.Bold,
                            )
                            Icon(
                                painterResource(Res.drawable.filter),
                                contentDescription = null,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    Icon(
                        painterResource(Res.drawable.close),
                        contentDescription = "Fermer",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onClose)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (comments.isNotEmpty()) {
                        LazyColumn {
                            items(comments, key = { it.id }) { comment ->
                                OneComment(comment)
                            }
                        }
                    } else {
                        Text(
                            text = "Aucun commentaire pour le moment.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                if (canComment) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ajouter un commentaire...") },
                            singleLine = true,
                            enabled = !isSubmitting,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    val text = draft.trim()
                                    if (text.isNotBlank() && !isSubmitting) {
                                        onSubmitComment(text)
                                        draft = ""
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BlueDark,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                cursorColor = BlueDark,
                            ),
                            shape = RoundedCornerShape(16.dp),
                        )
                        Button(
                            onClick = {
                                val text = draft.trim()
                                if (text.isNotBlank() && !isSubmitting) {
                                    onSubmitComment(text)
                                    draft = ""
                                }
                            },
                            enabled = !isSubmitting && draft.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueDark),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Envoyer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onLoginRequired,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueDark),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text("Connectez-vous pour commenter", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OneComment(comment: Comment = Comment(0, "", User(1, "", ""))) {
    Column {
        Row {
            Image(
                painter = painterResource(Res.drawable.one),
                contentDescription = null,
                Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(60)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(10.dp))
            Box {
                Row {
                    Text(
                        comment.user.username,
                        color = Color.Black.copy(0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Column(Modifier.absoluteOffset(y = 22.dp)) {
                    Text(comment.comment, fontSize = 13.sp)
                }
                Spacer(Modifier.height(5.dp))
            }
        }
        Spacer(Modifier.height(25.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSheet(
    comments: List<Comment> = emptyList(),
    onDismiss: () -> Unit = {},
    canComment: Boolean = false,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onSubmitComment: (String) -> Unit = {},
    onLoginRequired: () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp
        ),
        dragHandle = null,
    ) {
        Comment(
            comments = comments,
            onClose = onDismiss,
            canComment = canComment,
            isSubmitting = isSubmitting,
            errorMessage = errorMessage,
            onSubmitComment = onSubmitComment,
            onLoginRequired = onLoginRequired,
        )
    }
}
