package emy.partners.lawapp.presentation.pages.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.Constants.blog
import emy.partners.lawapp.domain.models.Blog
import emy.partners.lawapp.domain.models.ContentDestination
import emy.partners.lawapp.domain.models.UserGeneratedContent
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel
import emy.partners.lawapp.presentation.settings.LocalAppUiController
import emy.partners.lawapp.presentation.themes.BlueDark
import org.jetbrains.compose.resources.painterResource

private val PageBgLight = Color(0xFFE8EEF7)
private val PageBgDark = Color(0xFF0B1220)

private data class ExploreTopic(
    val id: Int,
    val name: String,
    val keywords: List<String> = emptyList(),
)

@Composable
fun ExplorePage(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState(),
    createdContents: List<UserGeneratedContent> = emptyList(),
    onBlogClick: (Blog) -> Unit = {},
) {
    ExploreBuild(modifier, scrollVertical, createdContents, onBlogClick)
}

@Composable
fun ExploreBuild(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState(),
    createdContents: List<UserGeneratedContent> = emptyList(),
    onBlogClick: (Blog) -> Unit = {},
) {
    val ui = LocalAppUiController.current
    val pageBg = if (ui.settings.darkMode) PageBgDark else PageBgLight
    val exploreContents = remember(createdContents) {
        createdContents.filter { it.destination == ContentDestination.Explore }
    }
    val topics = remember {
        listOf(
            ExploreTopic(0, "Tous"),
            ExploreTopic(2, "Droit Civil", listOf("civil", "moderne", "rdc")),
            ExploreTopic(3, "Numerique", listOf("numerique", "données", "donnees", "electron")),
            ExploreTopic(4, "LawApp50", listOf("lawapp")),
            ExploreTopic(5, "Articles", listOf("article")),
        )
    }
    var selectedTopicId by remember { mutableStateOf(0) }
    val selectedTopic = topics.firstOrNull { it.id == selectedTopicId } ?: topics.first()
    val filteredBlogs = remember(selectedTopicId, blog) {
        if (selectedTopicId == 0) {
            blog
        } else {
            val keys = selectedTopic.keywords
            blog.filter { item ->
                val haystack = "${item.title} ${item.description} ${item.type}".lowercase()
                keys.any { key -> haystack.contains(key.lowercase()) }
            }.ifEmpty { blog }
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(scrollVertical)
            .padding(horizontal = 14.dp)
            .padding(top = 8.dp, bottom = 96.dp)
    ) {
        ExploreHeader()
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            topics.forEach { topic ->
                val selected = topic.id == selectedTopicId
                Text(
                    text = topic.name,
                    color = if (selected) Color.White else AuthColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) AuthColors.AccentBright else Color.White)
                        .clickable { selectedTopicId = topic.id }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (exploreContents.isNotEmpty()) {
            SectionTitle("Vos publications Explore")
            Spacer(Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 4.dp),
            ) {
                items(exploreContents, key = { it.id }) { content ->
                    UserExploreCard(content = content)
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        AuthFormPanel {
            SectionTitle(
                title = if (selectedTopicId == 0) "A la une" else selectedTopic.name,
                dark = true,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${filteredBlogs.size} contenus a decouvrir",
                color = AuthColors.TextSecondary,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredBlogs, key = { it.id }) { item ->
                    ExploreStoryCard(blog = item, onClick = { onBlogClick(item) })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        AuthFormPanel {
            SectionTitle("Lecture recommandee", dark = true)
            Spacer(Modifier.height(12.dp))
            filteredBlogs.forEachIndexed { index, item ->
                ExploreListRow(blog = item, onClick = { onBlogClick(item) })
                if (index != filteredBlogs.lastIndex) {
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ExploreHeader() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        BlueDark.copy(alpha = 0.96f),
                        Color(0xFF08092B).copy(alpha = 0.94f),
                    )
                )
            )
            .padding(18.dp)
    ) {
        Text("Explorer", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Articles, guides et decouvertes juridiques pour progresser rapidement.",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 14.sp,
            lineHeight = 19.sp,
        )
    }
}

@Composable
private fun SectionTitle(title: String, dark: Boolean = false) {
    Text(
        text = title,
        color = if (dark) AuthColors.TextPrimary else Color.White,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
    )
}

@Composable
private fun ExploreStoryCard(blog: Blog, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(blog.background),
            contentDescription = blog.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp),
        )
        Column(Modifier.padding(12.dp)) {
            Text(
                text = blog.type,
                color = AuthColors.AccentBright,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = blog.title,
                color = AuthColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = blog.description,
                color = AuthColors.TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun ExploreListRow(blog: Blog, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF8FAFC))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(blog.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                blog.title,
                color = AuthColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                blog.description,
                color = AuthColors.TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Par ${blog.author.username}",
                color = AuthColors.AccentBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun UserExploreCard(content: UserGeneratedContent) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        Text("Nouveau", color = AuthColors.AccentBright, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        Text(
            content.title,
            color = AuthColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            content.description,
            color = AuthColors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.heightIn(max = 56.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Par ${content.author} · ${content.createdAt}",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ExplorePreview() {
    ExploreBuild()
}
