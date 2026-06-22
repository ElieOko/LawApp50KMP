package emy.partners.lawapp.presentation.components.basics

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.action
import lawapp.shared.generated.resources.app_
import org.jetbrains.compose.resources.painterResource


@Suppress("FrequentlyChangingValue")
@Composable
@Preview(showBackground = false)
fun TopBarCustom(
    scrollState: ScrollState = rememberScrollState(),
    onActionClick: () -> Unit = {}
) {
    val isScrolled = scrollState.value > 0

    val backgroundColor by animateColorAsState(
        targetValue = if (isScrolled)
            Color.White.copy(0.62f)
        else
            Color.Transparent,
        label = "topBarColor"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isScrolled)
            Color.Black
        else
            Color.White,
        label = "iconColor"
    )

    TopAppBar(
        title = {
            Icon(
                painter = painterResource(Res.drawable.app_),
                contentDescription = null,
                modifier = Modifier.size(68.dp),
                tint = iconColor
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
        titleHorizontalAlignment = Alignment.CenterHorizontally,
        actions = {
            IconButton(
                onClick = onActionClick,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(38.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isScrolled) Color.White.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.22f),
                    contentColor = iconColor
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.action),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
    )
}