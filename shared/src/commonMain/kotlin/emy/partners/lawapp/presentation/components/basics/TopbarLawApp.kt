package emy.partners.lawapp.presentation.components.basics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.rememberLiquidState
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.action
import lawapp.shared.generated.resources.app_
import org.jetbrains.compose.resources.painterResource


@Suppress("FrequentlyChangingValue")
@Composable
@Preview(showBackground = false)
fun TopBarCustom(scrollState: ScrollState =  rememberScrollState()) {
    val liquidState = rememberLiquidState()
    val isScrolled = scrollState.value > 0

    val backgroundColor by animateColorAsState(
        targetValue = if (isScrolled)
            Color.White.copy(0.5f)
        else
            Color.Transparent,
        label = "bottomBarColor"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isScrolled)
            Color.Black
        else
            Color.White,
        label = "iconColor"
    )

    TopAppBar(
        modifier = Modifier.shadow(
            elevation = if (isScrolled) 8.dp else 0.dp
        ),
        title = {},
        colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
        navigationIcon = {
            Row(Modifier.padding(2.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(Res.drawable.app_)
                    .size(300)
                    .build(),
                contentDescription = null,
                colorFilter = ColorFilter.tint(iconColor),
                modifier = Modifier.size(90.dp)
            )
            }
            Row(Modifier.padding(5.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
                Column {
                    Spacer(Modifier.height(5.dp))
                    IconButton({}, modifier = Modifier.size(50.dp)){
                        Icon(painterResource(Res.drawable.action),null, modifier = Modifier.size(50.dp), tint = iconColor)
                    }
                }
            }
        },
    )
}