package emy.partners.lawapp.presentation.pages.explore

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.Constants.blog
import emy.partners.lawapp.domain.models.Blog
import emy.partners.lawapp.domain.models.Category
import emy.partners.lawapp.domain.models.ContentDestination
import emy.partners.lawapp.domain.models.UserGeneratedContent
import emy.partners.lawapp.presentation.components.basics.LiquidGlassDemo
import emy.partners.lawapp.presentation.themes.BlueDark
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun ExplorePage(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState(),
    createdContents: List<UserGeneratedContent> = emptyList(),
    onBlogClick: (Blog) -> Unit = {}
) {
    ExploreBuild(modifier, scrollVertical, createdContents, onBlogClick)
}

@Composable
fun ExploreBuild(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState(),
    createdContents: List<UserGeneratedContent> = emptyList(),
    onBlogClick: (Blog) -> Unit = {}
){
    val exploreContents = remember(createdContents) {
        createdContents.filter { it.destination == ContentDestination.Explore }
    }
    val categories = remember {
        mutableStateListOf(
            Category(0, "Tous", true),
            Category(2, "Droit Civil"),
            Category(3, "Droit commerciale"),
            Category(4, "Droit penal"),
            Category(5, "Droit constitutionnel"),
            Category(6, "Droit administratif"),
            Category(7, "Droit du travail"),
        )}
    val colorState = remember { mutableStateOf(true) }
    val activeChip = remember { mutableStateOf(true) }
    val scrollHorizontal = rememberScrollState()
    val scrollHorizontal2 = rememberScrollState()
    Column(Modifier.fillMaxSize().verticalScroll(scrollVertical)) {
        Row(modifier.horizontalScroll(scrollHorizontal).fillMaxWidth()) {
            categories.forEachIndexed {indice,it->
               // colorState.value = it.isActive
                SuggestionChip(
                    enabled = activeChip.value,
                    shape = RoundedCornerShape(topEnd = 15.dp, bottomStart = 15.dp),
                    label = {
                        Text(it.name, color = Color.White)
                    },
                    onClick = {
                        categories.forEachIndexed { i, it ->
                            categories[i] = it.copy(isActive = i == indice)
                        }
//                        positionChannel.intValue = it.id
                    },
                    colors =  if(it.isActive) SuggestionChipDefaults.suggestionChipColors(BlueDark, labelColor = if (!activeChip.value) BlueDark else Color.Unspecified, disabledContainerColor = if (!activeChip.value) BlueDark else Color.Unspecified) else SuggestionChipDefaults.suggestionChipColors()
                )
                Spacer(Modifier.width(10.dp))
            }
        }
        Column(Modifier.padding(5.dp)) {
            if (exploreContents.isNotEmpty()) {
                Text("Vos nouveaux contenus", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.horizontalScroll(scrollHorizontal2).fillMaxWidth()) {
                    exploreContents.forEach { content ->
                        UserExploreCard(content = content)
                        Spacer(Modifier.width(10.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Text("Top Stories", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(scrollHorizontal2).fillMaxWidth()) {
                blog.forEachIndexed { index, blog ->
                    ExploreBlogCard(blog, onBlogClick)
                    Spacer(Modifier.width(10.dp))
                }

            }
            Spacer(Modifier.height(10.dp))
            Text("Droit Civil", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(scrollHorizontal2).fillMaxWidth()) {

                blog.sortedBy { it.id }.forEachIndexed { index, blog ->
                    ExploreBlogCard(blog, onBlogClick)
                    Spacer(Modifier.width(10.dp))
                }

            }
            Spacer(Modifier.height(10.dp))
            Text("Droit Civil", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(scrollHorizontal2).fillMaxWidth()) {

                blog.sortedBy { it.id }.forEachIndexed { index, blog ->
                    ExploreBlogCard(blog, onBlogClick)
                    Spacer(Modifier.width(10.dp))
                }

            }
            Spacer(Modifier.height(10.dp))
            Text("Droit Civil", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Droit Civil", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Droit Civil", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Droit Civil", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

}

@Composable
private fun ExploreBlogCard(blog: Blog, onBlogClick: (Blog) -> Unit) {
    Box(Modifier.clickable { onBlogClick(blog) }) {
        LiquidGlassDemo(blog)
    }
}

@Composable
private fun UserExploreCard(content: UserGeneratedContent) {
    OutlinedCard(
        modifier = Modifier.width(250.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.92f))
                .padding(12.dp)
        ) {
            Text("Nouveau contenu", color = BlueDark, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            Text(
                content.title,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                content.description,
                color = Color(0xFF475569),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.heightIn(max = 66.dp)
            )
            content.attachment?.let { attachment ->
                Spacer(Modifier.height(8.dp))
                if (attachment.isImageLike()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(attachment.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(8.dp)
                    ) {
                        Text("Fichier: ${attachment.name}", color = Color(0xFF334155), fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Par ${content.author} · ${content.createdAt}",
                color = Color(0xFF64748B),
                fontSize = 11.sp
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
fun ExplorePreview(){
    ExploreBuild()
}