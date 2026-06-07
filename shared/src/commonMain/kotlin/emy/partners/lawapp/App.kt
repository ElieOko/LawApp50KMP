package emy.partners.lawapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.presentation.components.basics.TopBarCustom
import emy.partners.lawapp.presentation.pages.home.HomePage
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.discovery
import lawapp.shared.generated.resources.evaluation
import lawapp.shared.generated.resources.explore
import lawapp.shared.generated.resources.house
import lawapp.shared.generated.resources.justice
import lawapp.shared.generated.resources.profil
import lawapp.shared.generated.resources.profil_user
import lawapp.shared.generated.resources.quiz
import lawapp.shared.generated.resources.session
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


data class Parent(
    val id : Int,
    val name : String,
    var isActive : Boolean = false,
    val icon : DrawableResource
)
@Composable
@Preview(showBackground = true)
fun App() {
    val liquidState = rememberLiquidState()
    val liquidState2 = rememberLiquidState()
    val scrollVertical = rememberScrollState()
    val listParent = listOf<Parent>(
        Parent(1, stringResource(Res.string.house), icon = Res.drawable.house),
        Parent(2,stringResource(Res.string.discovery), icon = Res.drawable.explore),
        Parent(3,stringResource(Res.string.session), icon = Res.drawable.evaluation),
        Parent(4,stringResource(Res.string.quiz), icon = Res.drawable.quiz),
        Parent(5,stringResource(Res.string.profil), icon = Res.drawable.profil_user),
    )
    val state = remember { mutableIntStateOf(0) }
    MaterialTheme {
        Scaffold(
//            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                //CompositionLocalProvider(LocalRippleConfiguration provides null){
                //Color(0xFF242D2C)
                Box(modifier = Modifier.clip(RoundedCornerShape(9.dp)).liquefiable(liquidState2)){
                    BottomAppBar(containerColor =  Color.White.copy(alpha = 0.5f),modifier = Modifier.background(
                            Color.White.copy(alpha = 0.5f)
                        )) {
                        listParent.forEachIndexed { i, parent ->
                            NavigationBarItem(
                                interactionSource = remember { MutableInteractionSource() },
                                colors =  NavigationBarItemDefaults.colors(
                                    indicatorColor =  Color.White.copy(alpha = 0.65f),
                                    selectedTextColor = Color(0xFf2563EB),
                                    selectedIconColor = Color(0xFf2563EB),
                                    unselectedIconColor = Color.Black.copy(0.6f),
                                    unselectedTextColor = Color.Black.copy(0.6f),
                                ),
                                selected = i == state.intValue,
                                onClick = {
                                    state.intValue = i
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(parent.icon),null, modifier = Modifier.size(28.dp),
                                    )
                                },
                                label = {
                                    Text(parent.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            )
                        }
                    }
                }

                //}
            },
            topBar = {TopBarCustom(scrollVertical)}
//            contentWindowInsets = WindowInsets(0, 0, 0, 0) // Désactive les insets par défaut
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .liquefiable(liquidState)
            ) {
                Image(
                    painter = painterResource(Res.drawable.justice),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                    colorFilter = ColorFilter.colorMatrix(
                        ColorMatrix().apply {
                            setToSaturation(0.7f) // 1 = normal, 0 = noir et blanc
                        }
                    )
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .liquid(liquidState)
                        .background(
                            Color.White.copy(alpha = 0.15f)
                        )
                )
                Column {
                    HomePage(Modifier.padding(bottom = it.calculateBottomPadding()))
//                    ExplorePage(modifier = Modifier.padding(top = it.calculateTopPadding()), scrollVertical)
                }
            }



        }
    }
}

@Composable
expect fun PlatformVideoPlayer(url: String, modifier: Modifier, isPlaying: Boolean)