package emy.partners.lawapp.presentation.pages.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.Constants
import emy.partners.lawapp.domain.models.Blog
import emy.partners.lawapp.presentation.themes.BlueDark
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import org.jetbrains.compose.resources.painterResource

@Composable
fun ExploreDetailPage(
    blog: Blog,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState()
) {
    ExploreDetailBuild(
        blog = blog,
        modifier = modifier,
        onBack = onBack,
        scrollVertical
    )
}

@Composable
fun ExploreDetailBuild(
    blog: Blog,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState()
) {
    val liquidState = rememberLiquidState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollVertical)
    ) {
        Column(
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                text = "< Retour a Explore",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            )
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(330.dp)
                    .clip(RoundedCornerShape(34.dp))
            ) {
                Image(
                    painter = painterResource(blog.background),
                    contentDescription = blog.title,
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(
                        ColorMatrix().apply { setToSaturation(0.62f) }
                    ),
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .liquefiable(liquidState)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.78f)
                                )
                            )
                        )
                )
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = blog.type,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = blog.title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 31.sp,
                        lineHeight = 34.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Par ${blog.author.username}",
                        color = Color.White.copy(alpha = 0.74f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(18.dp)
            ) {
                Text(
                    "A propos",
                    color = Color.Black.copy(alpha = 0.88f),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = blog.description.ifBlank {
                        "Un contenu selectionne pour apprendre le droit avec une approche simple, pratique et orientee cas concrets."
                    },
                    color = Color.Black.copy(alpha = 0.58f),
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExploreDetailMetric("Lecture", "6 min", Modifier.weight(1f))
                    ExploreDetailMetric("Niveau", "Clair", Modifier.weight(1f))
                    ExploreDetailMetric("Theme", blog.type, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueDark)
            ) {
                Text("Commencer la lecture", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun ExploreDetailMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(BlueDark.copy(alpha = 0.08f))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = BlueDark, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        Text(label, color = Color.Black.copy(alpha = 0.46f), fontSize = 11.sp)
    }
}

@Composable
@Preview(showBackground = true)
fun ExploreDetailPreview() {
    ExploreDetailBuild(Constants.blog.first())
}
