package emy.partners.lawapp.presentation.components.basics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.domain.models.Blog
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.justice
import lawapp.shared.generated.resources.like
import lawapp.shared.generated.resources.preview
import lawapp.shared.generated.resources.weui
import org.jetbrains.compose.resources.painterResource

@Composable
fun CardMulti(
    onclickEvent :()-> Unit = {},
    onlonclickEvent : ()-> Unit = {},
    isSelect : Boolean = true
){

    Card(colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.7F))) {
        Row(
            Modifier.fillMaxWidth().combinedClickable(
                onClick = onclickEvent,
                onLongClick = onlonclickEvent
            ).background(Color.Unspecified), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.padding(2.dp), horizontalArrangement = Arrangement.End) {
                Column(Modifier.padding(10.dp)) {
                    Box {
                        Badge(modifier = Modifier.size(40.dp),containerColor = Color(0xFF042542), contentColor = Color.White){
                            Icon(
                                painter = painterResource(Res.drawable.preview),
                                contentDescription = null,
                                modifier = Modifier.size(45.dp).clip(RectangleShape))
                        }
                    }
                }
                Column{
                    Spacer(Modifier.height(15.dp))
                    Text( "Completed identify verification", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Required to unlock transfers", color =  Color.White.copy(0.5f), fontSize = 11.sp, modifier = Modifier.absoluteOffset(y = (-5).dp))
                }
            }
            Row {
                Column(Modifier.padding(10.dp)) {
                    //  Text(discussion.listConversation[lastIndice].time,color =  Color.Black.copy(0.5f), fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Spacer(Modifier.width(10.dp))
//                        Badge(modifier = Modifier.size(27.dp),containerColor = Color.Red, contentColor = Color.White){
                        Icon(painter =  painterResource(Res.drawable.weui), null)
//                        }
                    }
                }
            }
        }
    }

}


@Composable

@Preview(showBackground = true)
fun CardCustom(){
    Column {
        LiquidGlassDemo()
    }
}

@Composable
fun LiquidGlassDemo(blog : Blog? = null) {
    val liquidState = rememberLiquidState()
    val isLike = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.size(width = 240.dp, height = 250.dp).clip(RoundedCornerShape(24.dp))
    ) {
        // Fond
        Image(
            painter = painterResource(blog?.background?: Res.drawable.preview),
            contentDescription = null,
            contentScale = ContentScale.None,
            colorFilter = ColorFilter.colorMatrix(
                ColorMatrix().apply {
                    setToSaturation(0.52f) // 1 = normal, 0 = noir et blanc
                }
            ),
            modifier = Modifier
                .fillMaxSize()
                .liquefiable(liquidState)
        )
        // Carte avec effet liquid glass
        Column(Modifier.fillMaxSize().padding(5.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box{
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 50.dp, height = 50.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .liquid(liquidState)
                            .background(
                                Color.White.copy(alpha = 0.15f)
                            )
                    ) {
                        Row(Modifier.fillMaxWidth()) {
                            IconButton(onClick = {
                                isLike.value = !isLike.value
                            }, colors = IconButtonDefaults.iconButtonColors(contentColor = if (isLike.value) Color.Red else Color.Black)){
                                Icon(painter = painterResource(Res.drawable.like), null, modifier = Modifier.size(23.dp))
                            }
//                            Text("", fontSize = 13.sp, color = Color.White, modifier = Modifier.absoluteOffset(y = (16).dp))
                        }
                    }
                }
                Box{
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 90.dp, height = 30.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .liquid(liquidState)
                            .background(
                                Color.White.copy(alpha = 0.15f)
                            )
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(blog?.type?:"Document", fontSize = 13.sp, color = Color.Black, modifier = Modifier, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Box{
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .align(Alignment.Center)

                            .clip(RoundedCornerShape(24.dp))
                            .liquid(liquidState)
                            .background(
                                Color.White.copy(alpha = 0.85f)
                            )
                    ) {
                        Row(Modifier.fillMaxWidth().padding(5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(blog?.title?:"Document", lineHeight = 13.sp,fontSize = 13.sp, color = Color.Black.copy(alpha = 0.5f), modifier = Modifier, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

//@Composable
//@Preview(showBackground = true)
//fun Background(){
//    val liquidState = rememberLiquidState()
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .liquefiable(liquidState)
//    ) {
//        Image(
//            painter = painterResource(Res.drawable.justice),
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.matchParentSize(),
//            colorFilter = ColorFilter.colorMatrix(
//                ColorMatrix().apply {
//                    setToSaturation(0.7f) // 1 = normal, 0 = noir et blanc
//                }
//            )
//        )
//
//        Box(
//            modifier = Modifier
//                .matchParentSize()
//                .liquid(liquidState)
//                .background(
//                    Color.White.copy(alpha = 0.15f)
//                )
//        )
//        Column(Modifier.fillMaxSize()) {
//
//        }
//    }
//}