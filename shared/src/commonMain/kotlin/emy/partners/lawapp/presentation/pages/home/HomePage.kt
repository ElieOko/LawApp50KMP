package emy.partners.lawapp.presentation.pages.home

import VideoPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import emy.partners.lawapp.data.remote.contenu.ContenuFeedItem
import emy.partners.lawapp.data.remote.contenu.ContenuRepository
import emy.partners.lawapp.domain.models.Comment
import emy.partners.lawapp.domain.models.ExtraContent
import emy.partners.lawapp.domain.models.User
import emy.partners.lawapp.domain.models.UserGeneratedContent
import emy.partners.lawapp.presentation.components.basics.CommentsSheet
import emy.partners.lawapp.presentation.components.basics.ContentPublication
import emy.partners.lawapp.presentation.components.basics.IconColumnPub
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import kotlinx.coroutines.launch
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.justice
import org.jetbrains.compose.resources.painterResource

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
) {
    var feed by remember { mutableStateOf<List<ContenuFeedItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val liquidState = rememberLiquidState()

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        ContenuRepository.loadHomeFeed()
            .onSuccess { feed = it }
            .onFailure { errorMessage = it.message ?: "Impossible de charger les contenus" }
        isLoading = false
    }

    val pagerState: PagerState = rememberPagerState(pageCount = { feed.size.coerceAtLeast(1) })

    when {
        isLoading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        errorMessage != null && feed.isEmpty() -> {
            Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(errorMessage.orEmpty(), color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
        feed.isEmpty() -> {
            Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Aucun contenu disponible", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
        else -> {
            Column(
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                VerticalPager(state = pagerState) { pageIndex ->
                    val item = feed[pageIndex]
                    var showComments by remember(item.id) { mutableStateOf(false) }

                    Box(Modifier.fillMaxSize().background(Color.Black)) {
                        ContenuMedia(
                            fileContent = item.fileContent,
                            isVideo = item.isVideo,
                            title = item.title,
                            isActivePage = isActive && pagerState.currentPage == pageIndex,
                            onMediaClick = { showComments = false },
                            liquidModifier = Modifier.fillMaxSize().liquefiable(liquidState),
                        )

                        Column(
                            Modifier.fillMaxSize().padding(2.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconColumnPub(
                                    extra = ExtraContent(
                                        like = item.likeCount,
                                        comment = item.commentCount,
                                    ),
                                    liked = item.likedByMe,
                                    onLikeClick = {
                                        if (item.likedByMe) return@IconColumnPub
                                        scope.launch {
                                            ContenuRepository.like(item.id)
                                                .onSuccess {
                                                    feed = feed.map { current ->
                                                        if (current.id != item.id) current
                                                        else current.copy(
                                                            likeCount = current.likeCount + 1,
                                                            likedByMe = true,
                                                        )
                                                    }
                                                }
                                        }
                                    },
                                    eventComment = { showComments = true }
                                )
                            }
                            ContentPublication(
                                username = item.authorName,
                                member = item.authorUsername,
                                content = listOfNotNull(
                                    item.title.takeIf { it.isNotBlank() && it != "Sans titre" },
                                    item.description.takeIf { it.isNotBlank() },
                                ).joinToString("\n")
                            )
                        }
                    }

                    if (showComments) {
                        CommentsSheet(
                            comments = item.comments.map { comment ->
                                Comment(
                                    id = comment.id,
                                    comment = comment.text,
                                    user = User(
                                        userId = 0,
                                        username = comment.authorName,
                                        profile = "",
                                    )
                                )
                            },
                            onDismiss = { showComments = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContenuMedia(
    fileContent: String?,
    isVideo: Boolean,
    title: String,
    isActivePage: Boolean,
    onMediaClick: () -> Unit,
    liquidModifier: Modifier,
) {
    when {
        isVideo && !fileContent.isNullOrBlank() -> {
            VideoPlayer(
                modifier = liquidModifier.clickable(onClick = onMediaClick),
                url = fileContent,
                autoPlay = isActivePage,
                showControls = true,
            )
        }
        !fileContent.isNullOrBlank() -> {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(fileContent)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0.85f) }
                ),
                modifier = liquidModifier.clickable(onClick = onMediaClick),
            )
        }
        else -> {
            Image(
                painter = painterResource(Res.drawable.justice),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0.7f) }
                ),
                modifier = liquidModifier.clickable(onClick = onMediaClick),
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HomePreview() {
    HomeBuild()
}
