package emy.partners.lawapp.presentation.pages.explore

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import emy.partners.lawapp.data.Constants.blog
import emy.partners.lawapp.domain.models.Blog
import emy.partners.lawapp.domain.models.Category
import emy.partners.lawapp.presentation.components.basics.LiquidGlassDemo
import emy.partners.lawapp.presentation.themes.BlueDark
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ExplorePage(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState(),
    onBlogClick: (Blog) -> Unit = {}
) {
    ExploreBuild(modifier, scrollVertical, onBlogClick)
}

@Composable
fun ExploreBuild(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState(),
    onBlogClick: (Blog) -> Unit = {}
){
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
@Preview(showBackground = true)
fun ExplorePreview(){
    ExploreBuild()
}