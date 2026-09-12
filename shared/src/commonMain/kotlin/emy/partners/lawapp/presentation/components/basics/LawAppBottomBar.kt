package emy.partners.lawapp.presentation.components.basics

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.ic_add_media
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Couleurs 100% opaques, style TikTok. Aucun canal alpha. */
object LawAppBottomBarColors {
    val Background = Color(0xFF000000)
    val Divider = Color(0xFF2C2C2C)
    val Selected = Color(0xFFFFFFFF)
    val Unselected = Color(0xFF8A8A8A)
    val PlusCyan = Color(0xFF25F4EE)
    val PlusRed = Color(0xFFFE2C55)
    val PlusCenter = Color(0xFFFFFFFF)
    val PlusIcon = Color(0xFF000000)
}

data class LawAppBottomBarItem(
    val id: String,
    val label: String,
    val icon: DrawableResource,
)

/** Barre basse opaque facon TikTok : fond noir, labels, bouton + au centre. */
@Composable
fun LawAppBottomBar(
    items: List<LawAppBottomBarItem>,
    selectedId: String,
    onItemClick: (LawAppBottomBarItem) -> Unit,
    onCreateClick: () -> Unit,
    createContentDescription: String,
    modifier: Modifier = Modifier,
) {
    val splitIndex = items.size / 2

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LawAppBottomBarColors.Background)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LawAppBottomBarColors.Divider)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                if (index == splitIndex) {
                    TikTokCreateButton(
                        contentDescription = createContentDescription,
                        onClick = onCreateClick,
                        modifier = Modifier
                            .width(52.dp)
                            .fillMaxHeight(),
                    )
                }
                LawAppBottomBarTab(
                    item = item,
                    selected = item.id == selectedId,
                    onClick = { onItemClick(item) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun LawAppBottomBarTab(
    item: LawAppBottomBarItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(
        targetValue = if (selected) LawAppBottomBarColors.Selected else LawAppBottomBarColors.Unselected,
        label = "bottomBarTint",
    )
    Column(
        modifier = modifier
            .semantics {
                this.selected = selected
                contentDescription = item.label
            }
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(if (selected) 26.dp else 24.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = item.label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TikTokCreateButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(width = 46.dp, height = 30.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-3).dp)
                    .size(width = 26.dp, height = 22.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LawAppBottomBarColors.PlusCyan)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 3.dp)
                    .size(width = 26.dp, height = 22.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LawAppBottomBarColors.PlusRed)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 32.dp, height = 22.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LawAppBottomBarColors.PlusCenter),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add_media),
                    contentDescription = null,
                    tint = LawAppBottomBarColors.PlusIcon,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
