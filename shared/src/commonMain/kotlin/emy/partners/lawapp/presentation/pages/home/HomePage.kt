package emy.partners.lawapp.presentation.pages.home

import VideoPlayer
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var feed by remember { mutableStateOf(ContenuRepository.cachedFeed()) }
    var localLikeOverrides by remember { mutableStateOf<Map<Long, LikeOverride>>(emptyMap()) }
    var isInitialLoading by remember { mutableStateOf(feed.isEmpty()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val liquidState = rememberLiquidState()
    val pullState = rememberPullToRefreshState()

    val displayFeed = remember(feed, localLikeOverrides) {
        buildDisplayFeed(feed, localLikeOverrides)
    }
    val initialPage = remember {
        ContenuRepository.indexOfLastViewed(displayFeed)
            .coerceIn(0, (displayFeed.size - 1).coerceAtLeast(0))
    }
    val pagerState: PagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { displayFeed.size.coerceAtLeast(1) },
    )
    var didRestorePage by remember { mutableStateOf(displayFeed.isNotEmpty()) }

    // Conserve le contenu en cours quand on quitte Home (autre onglet / page).
    LaunchedEffect(pagerState.currentPage, displayFeed) {
        displayFeed.getOrNull(pagerState.currentPage)?.let { item ->
            ContenuRepository.rememberLastViewedContenuId(item.id)
        }
    }

    // Si le feed arrive apres le montage, revenir au contenu en cours.
    LaunchedEffect(displayFeed) {
        if (didRestorePage || displayFeed.isEmpty()) return@LaunchedEffect
        val target = ContenuRepository.indexOfLastViewed(displayFeed)
        if (target != pagerState.currentPage) {
            pagerState.scrollToPage(target)
        }
        didRestorePage = true
    }

    // Cache d'abord ; reseau uniquement si le cache est vide (pas de reload a chaque visite/scroll).
    LaunchedEffect(Unit) {
        val cached = ContenuRepository.cachedFeed()
        if (cached.isNotEmpty()) {
            feed = cached
            isInitialLoading = false
            return@LaunchedEffect
        }
        isInitialLoading = true
        ContenuRepository.loadHomeFeed()
            .onSuccess {
                feed = it
                errorMessage = null
            }
            .onFailure {
                errorMessage = it.message ?: "Impossible de charger les contenus"
            }
        isInitialLoading = false
    }

    fun refreshFeed() {
        if (isRefreshing) return
        isRefreshing = true
        scope.launch {
            ContenuRepository.refreshHomeFeed()
                .onSuccess {
                    feed = it
                    errorMessage = null
                    val nextFeed = buildDisplayFeed(it, localLikeOverrides)
                    if (nextFeed.isNotEmpty()) {
                        pagerState.scrollToPage(ContenuRepository.indexOfLastViewed(nextFeed))
                    }
                }
                .onFailure {
                    if (feed.isEmpty()) {
                        errorMessage = it.message ?: "Impossible de rafraichir les contenus"
                    }
                }
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { refreshFeed() },
        state = pullState,
        modifier = modifier.fillMaxSize().background(Color.Black)
    ) {
        when {
            isInitialLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            errorMessage != null && displayFeed.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(errorMessage.orEmpty(), color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            displayFeed.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Aucun contenu disponible", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            else -> {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    VerticalPager(state = pagerState) { pageIndex ->
                        val item = displayFeed[pageIndex]
                        var showComments by remember(item.id) { mutableStateOf(false) }
                        val isRemote = feed.any { it.id == item.id }

                        Box(Modifier.fillMaxSize().background(Color.Black)) {
                            ContenuMedia(
                                item = item,
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
                                            scope.launch {
                                                if (isRemote) {
                                                    ContenuRepository.toggleLike(item.id)
                                                        .onSuccess { updated -> feed = updated }
                                                } else {
                                                    localLikeOverrides = toggleLocalLike(localLikeOverrides, item)
                                                }
                                            }
                                        },
                                        eventComment = { showComments = true }
                                    )
                                }
                                ContentPublication(
                                    username = item.authorName,
                                    member = item.authorUsername,
                                    content = if (item.hasMediaFile) {
                                        listOfNotNull(
                                            item.title.takeIf { it.isNotBlank() && it != "Sans titre" },
                                            item.description.takeIf { it.isNotBlank() },
                                        ).joinToString("\n")
                                    } else {
                                        ""
                                    }
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
}

private data class LikeOverride(
    val liked: Boolean,
    val likeCount: Int,
)

private fun toggleLocalLike(
    current: Map<Long, LikeOverride>,
    item: ContenuFeedItem,
): Map<Long, LikeOverride> {
    val existing = current[item.id]
    val likedNow = existing?.liked ?: item.likedByMe
    val countNow = existing?.likeCount ?: item.likeCount
    val nextLiked = !likedNow
    val nextCount = if (nextLiked) countNow + 1 else (countNow - 1).coerceAtLeast(0)
    return current + (item.id to LikeOverride(nextLiked, nextCount))
}

private fun buildDisplayFeed(
    feed: List<ContenuFeedItem>,
    localLikeOverrides: Map<Long, LikeOverride>,
): List<ContenuFeedItem> {
    // Les publications API n'apparaissent qu'apres pull-to-refresh (pas de doublon local).
    return feed.map { item ->
        val override = localLikeOverrides[item.id] ?: return@map item
        item.copy(likedByMe = override.liked, likeCount = override.likeCount)
    }
}

@Composable
private fun ContenuMedia(
    item: ContenuFeedItem,
    isActivePage: Boolean,
    onMediaClick: () -> Unit,
    liquidModifier: Modifier,
) {
    when {
        item.isVideo && item.hasMediaFile -> {
            VideoPlayer(
                modifier = liquidModifier.clickable(onClick = onMediaClick),
                url = item.fileContent.orEmpty(),
                autoPlay = isActivePage,
                showControls = true,
            )
        }
        item.hasMediaFile -> {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(item.fileContent)
                    .crossfade(true)
                    .build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0.85f) }
                ),
                modifier = liquidModifier.clickable(onClick = onMediaClick),
            )
        }
        else -> {
            Box(modifier = liquidModifier.clickable(onClick = onMediaClick)) {
                Image(
                    painter = painterResource(Res.drawable.justice),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(
                        ColorMatrix().apply { setToSaturation(0.55f) }
                    ),
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Black.copy(alpha = 0.75f),
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (item.title.isNotBlank() && item.title != "Sans titre") {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    Text(
                        text = item.description.ifBlank { "Publication LawApp50" },
                        color = Color.White.copy(alpha = 0.94f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HomePreview() {
    HomeBuild()
}
