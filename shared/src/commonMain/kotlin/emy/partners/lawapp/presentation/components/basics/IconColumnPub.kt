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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
fun IconColumnPub(extra: ExtraContent = ExtraContent()) {
    val like = remember { mutableStateOf(0) }
    val comment = remember { mutableStateOf(0) }
    val favorite = remember { mutableStateOf(0) }
    val share = remember { mutableStateOf(0) }
    val isLike = remember{mutableStateOf(false)}
    val isFavorite = remember{mutableStateOf(false)}
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box{
            IconButton(onClick = {
                isLike.value = !isLike.value
                when(isLike.value){
                    true -> like.value++
                    else -> like.value--
                }
            }, colors = IconButtonDefaults.iconButtonColors(contentColor = if (isLike.value) Color.Red else Color.White)){
                Icon(painter = painterResource(Res.drawable.like), null, modifier = Modifier.size(35.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Spacer(Modifier.height(40.dp))
                Text(like.value.toString(), color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.absoluteOffset(x = 20.dp))
            }
        }
        Box{
            IconButton(onClick = {},colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)){
                Icon(painter = painterResource(Res.drawable.comment), null, modifier = Modifier.size(35.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Spacer(Modifier.height(40.dp))
                Text(comment.value.toString(), color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.absoluteOffset(x = 20.dp))
            }
        }
        Box{
            IconButton(onClick = {
                isFavorite.value = !isFavorite.value
                when(isFavorite.value){
                    true -> favorite.value++
                    else -> favorite.value--
                }
            },colors = IconButtonDefaults.iconButtonColors(contentColor = if (isFavorite.value) Color.Red else Color.White)){
                Icon(painter = painterResource(Res.drawable.favorite), null, modifier = Modifier.size(35.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Spacer(Modifier.height(40.dp))
                Text(favorite.value.toString(), color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.absoluteOffset(x = 20.dp))
            }
        }
        Box{
            IconButton(onClick = {},colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)){
                Icon(painter = painterResource(Res.drawable.share), null, modifier = Modifier.size(35.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Spacer(Modifier.height(40.dp))
                Text(share.value.toString(), color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.absoluteOffset(x = 20.dp))
            }
        }
    }
}