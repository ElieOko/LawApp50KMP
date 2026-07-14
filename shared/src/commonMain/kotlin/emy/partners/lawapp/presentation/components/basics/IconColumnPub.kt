package emy.partners.lawapp.presentation.components.basics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import emy.partners.lawapp.domain.models.ExtraContent
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.comment
import lawapp.shared.generated.resources.favorite
import lawapp.shared.generated.resources.like
import lawapp.shared.generated.resources.share
import org.jetbrains.compose.resources.painterResource

@Composable
@Preview(showBackground = false)
fun IconColumnPub(
    extra: ExtraContent = ExtraContent(),
    liked: Boolean = false,
    onLikeClick: (() -> Unit)? = null,
    eventComment: () -> Unit = {},
) {
    var likeCount by remember(extra.like) { mutableStateOf(extra.like) }
    var commentCount by remember(extra.comment) { mutableStateOf(extra.comment) }
    var favoriteCount by remember(extra.favorite) { mutableStateOf(extra.favorite) }
    val shareCount by remember(extra.share) { mutableStateOf(extra.share) }
    var isLike by remember(liked) { mutableStateOf(liked) }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(extra.like, extra.comment, liked) {
        likeCount = extra.like
        commentCount = extra.comment
        isLike = liked
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box {
            IconButton(
                onClick = {
                    if (onLikeClick != null) {
                        onLikeClick()
                    } else {
                        isLike = !isLike
                        likeCount = if (isLike) likeCount + 1 else (likeCount - 1).coerceAtLeast(0)
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (isLike) Color.Red else Color.White
                )
            ) {
                Icon(painter = painterResource(Res.drawable.like), null, modifier = Modifier.size(35.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Spacer(Modifier.height(40.dp))
                Text(
                    likeCount.toString(),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.absoluteOffset(x = 20.dp)
                )
            }
        }
        Box {
            IconButton(
                onClick = { eventComment() },
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(painter = painterResource(Res.drawable.comment), null, modifier = Modifier.size(35.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Spacer(Modifier.height(40.dp))
                Text(
                    commentCount.toString(),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.absoluteOffset(x = 20.dp)
                )
            }
        }
        Box {
            IconButton(
                onClick = {
                    isFavorite = !isFavorite
                    favoriteCount = if (isFavorite) favoriteCount + 1 else (favoriteCount - 1).coerceAtLeast(0)
                },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (isFavorite) Color.Red else Color.White
                )
            ) {
                Icon(painter = painterResource(Res.drawable.favorite), null, modifier = Modifier.size(35.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Spacer(Modifier.height(40.dp))
                Text(
                    favoriteCount.toString(),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.absoluteOffset(x = 20.dp)
                )
            }
        }
        Box {
            IconButton(
                onClick = {},
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(painter = painterResource(Res.drawable.share), null, modifier = Modifier.size(35.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Spacer(Modifier.height(40.dp))
                Text(
                    shareCount.toString(),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.absoluteOffset(x = 20.dp)
                )
            }
        }
    }
}
