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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import emy.partners.lawapp.domain.models.Comment
import emy.partners.lawapp.domain.models.User
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
) {
    Box {
        Surface(
            color = Color(0x0FE82D2D),
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp
            ),
            modifier = Modifier
                .zIndex(4f)
                .background(color = Color.White)
                .fillMaxHeight(0.7f)
                .align(Alignment.BottomCenter)
        ) {
            Column(Modifier.fillMaxWidth().height(350.dp).padding(10.dp)) {
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
                Spacer(Modifier.height(20.dp))
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
    onDismiss: () -> Unit = {}
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
        )
    }
}
