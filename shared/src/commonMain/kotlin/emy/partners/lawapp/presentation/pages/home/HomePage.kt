package emy.partners.lawapp.presentation.pages.home

import VideoPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import emy.partners.lawapp.data.Constants.generateArticle
import emy.partners.lawapp.domain.models.ContentDestination
import emy.partners.lawapp.domain.models.ExtraContent
import emy.partners.lawapp.domain.models.UserGeneratedContent
import emy.partners.lawapp.presentation.components.basics.CommentsSheet
import emy.partners.lawapp.presentation.components.basics.ContentPublication
import emy.partners.lawapp.presentation.components.basics.IconColumnPub
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    createdContents: List<UserGeneratedContent> = emptyList(),
) {
    HomeBuild(
        modifier = modifier,
        isActive = isActive,
        createdContents = createdContents
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBuild(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    createdContents: List<UserGeneratedContent> = emptyList(),
    pagerState: PagerState = rememberPagerState(
        pageCount = {
            generateArticle.size + createdContents.count { it.destination == ContentDestination.Home }
        }
    ),
) {
    val homeContents = remember(createdContents) {
        createdContents.filter { it.destination == ContentDestination.Home }
    }
    val liquidState = rememberLiquidState()
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        VerticalPager(state = pagerState) { pageIndex ->
            var show by remember { mutableStateOf(false) }
            val isCreatedPage = pageIndex < homeContents.size

            if (isCreatedPage) {
                CreatedHomeContentPage(
                    content = homeContents[pageIndex],
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val articleIndex = pageIndex - homeContents.size
                val article = generateArticle[articleIndex]
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    if (!article.isPlay) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(article.image)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Publication image",
                            contentScale = ContentScale.FillBounds,
                            colorFilter = ColorFilter.colorMatrix(
                                ColorMatrix().apply { setToSaturation(0.7f) }
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { show = false }
                                .liquefiable(liquidState)
                        )
                    } else {
                        VideoPlayer(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { show = false }
                                .liquefiable(liquidState),
                            url = article.video,
                            isActive && pagerState.currentPage == pageIndex,
                            showControls = true,
                        )
                    }

                    Column(
                        Modifier.fillMaxSize().padding(2.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconColumnPub(
                                ExtraContent(comment = article.comments.size),
                                eventComment = { show = true }
                            )
                        }
                        ContentPublication(article.author, content = article.content)
                    }
                }
            }
            if (show && !isCreatedPage) {
                CommentsSheet(comments = generateArticle[pageIndex - homeContents.size].comments) {
                    show = false
                }
            }
        }
    }
}

@Composable
private fun CreatedHomeContentPage(
    content: UserGeneratedContent,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .background(Color(0xFF0B1220))
            .padding(14.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF111827))
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Nouveau contenu Home",
                    color = Color(0xFF93C5FD),
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = content.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 26.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                )
                Text(
                    text = content.description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
                content.attachment?.let { attachment ->
                    Spacer(Modifier.height(10.dp))
                    if (attachment.isImageLike()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(attachment.uri)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(10.dp)
                        ) {
                            Text(
                                "Piece jointe: ${attachment.name}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Text(
                text = "Par ${content.author} · ${content.createdAt}",
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 12.sp
            )
        }
    }
}

private fun emy.partners.lawapp.domain.models.ContentAttachment.isImageLike(): Boolean {
    val mime = mimeType.orEmpty().lowercase()
    val value = uri.lowercase()
    return mime.startsWith("image/") ||
        value.endsWith(".png") ||
        value.endsWith(".jpg") ||
        value.endsWith(".jpeg") ||
        value.endsWith(".webp")
}

@Composable
@Preview(showBackground = true)
fun HomePreview(){
    HomeBuild()
}