package emy.partners.lawapp

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.presentation.components.basics.TopBarCustom
import emy.partners.lawapp.presentation.pages.HomePage
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.discovery
import lawapp.shared.generated.resources.explore
import lawapp.shared.generated.resources.house
import lawapp.shared.generated.resources.people
import lawapp.shared.generated.resources.profil
import lawapp.shared.generated.resources.quiz
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
    val listParent = listOf<Parent>(
        Parent(1, stringResource(Res.string.house), icon = Res.drawable.house),
        Parent(2,stringResource(Res.string.discovery), icon = Res.drawable.explore),
        Parent(3,stringResource(Res.string.quiz), icon = Res.drawable.people),
        Parent(4,stringResource(Res.string.profil), icon = Res.drawable.profil),
    )
    val state = remember { mutableIntStateOf(0) }
    MaterialTheme {
        Scaffold(
//            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                //CompositionLocalProvider(LocalRippleConfiguration provides null){
                    BottomAppBar(containerColor = Color(0xFF08092B)
                    ) {
                        listParent.forEachIndexed { i, parent ->
                            NavigationBarItem(
                                interactionSource = remember { MutableInteractionSource() },
                                colors =  NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.White,
                                    selectedTextColor = Color.White,
                                    selectedIconColor = Color(0xFF04052F),
                                    unselectedIconColor = Color(0xFF0B0D4E),
                                    unselectedTextColor = Color.White.copy(0.6f),
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
                //}
            },
            topBar = {TopBarCustom()}
//            contentWindowInsets = WindowInsets(0, 0, 0, 0) // Désactive les insets par défaut
        ) {
            Column {
                HomePage(Modifier.padding(bottom = it.calculateBottomPadding()))
            }
        }
    }
}

@Composable
expect fun PlatformVideoPlayer(url: String, modifier: Modifier, isPlaying: Boolean)