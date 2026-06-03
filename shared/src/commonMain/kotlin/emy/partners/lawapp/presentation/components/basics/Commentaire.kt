package emy.partners.lawapp.presentation.components.basics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheet
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.Companion
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import emy.partners.lawapp.domain.models.Comment
import kotlinx.coroutines.launch
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.close
import lawapp.shared.generated.resources.filter
import lawapp.shared.generated.resources.one
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.lazy.items
import emy.partners.lawapp.domain.models.User

@Composable
@Preview(showBackground = true)
fun Comment(
    comments : List<Comment> = emptyList()
){
    val scroll = rememberScrollState()
    Box{
        Surface(color = Color(0x0FE82D2D), shape =RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp
        ), modifier = Modifier.zIndex(4f).background(color = Color.White).fillMaxHeight(0.7f).align(
            Alignment.BottomCenter)) {
            Column(Modifier.fillMaxWidth().height(350.dp).padding(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
                    Box(Modifier.weight(0.1f)){
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text("${comments.size} commentaires", fontWeight = FontWeight.Bold, modifier = Modifier)
                            Icon(painterResource(Res.drawable.filter),null, modifier = Modifier.size(19.dp))
                        }
                    }
                    Icon(painterResource(Res.drawable.close),null, modifier = Modifier.size(18.dp).clickable{

                    })
                }
                Spacer(Modifier.height(20.dp))
                if (comments.isNotEmpty()){
                    Column  {
                        LazyColumn {
                            items(comments, key = {it.id}){comment ->
                                OneComment(comment)
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun OneComment(comment : Comment = Comment(0,"", User(1,"",""))){
    Column {
        Row {
            Image(painter = painterResource(Res.drawable.one),null, Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(60),
                ),
                contentScale = ContentScale.Crop)
            Spacer(Modifier.width(10.dp))
            Box{
                Row {
                    Text(comment.user.username, color = Color.Black.copy(0.6f), fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
    comments: List<Comment> =  emptyList(),
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
//        contentWindowInsets = { WindowInsets(0) }
    ) {
        Comment(comments)
    }
}
