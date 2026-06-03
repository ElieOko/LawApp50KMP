package emy.partners.lawapp.presentation.pages

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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import emy.partners.lawapp.data.Constants.generateArticle
import emy.partners.lawapp.domain.models.ExtraContent
import emy.partners.lawapp.presentation.components.basics.ContentPublication
import emy.partners.lawapp.presentation.components.basics.IconColumnPub
import emy.partners.lawapp.presentation.components.basics.CommentsSheet

@Composable
fun HomePage(modifier: Modifier = Modifier) {
    HomeBuild(modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBuild(modifier: Modifier = Modifier) {

    val pagerState = rememberPagerState(pageCount = { generateArticle.size })

//    LaunchedEffect(pagerState.currentPage){
//        show = false
//    }
    Column(modifier.fillMaxSize(),horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {
        VerticalPager(state = pagerState){
            var show by remember { mutableStateOf(false) }
            Box(Modifier.fillMaxSize().background(Color.Black)){
                if (!generateArticle[it].isPlay){
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(generateArticle[it].image)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Translated description of what the image contains",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize().clickable{
                            show = false
                        }
                    )
                }
                else{
                    VideoPlayer(
                        modifier = Modifier.fillMaxSize().clickable{
                            show = false
                        },
                        url = generateArticle[it].video, // Automatically Detect the URL, Wether to Play YouTube Video or .mp4 e.g
                        pagerState.currentPage == it,
                        showControls = true,
                    )
//                    PlatformVideoPlayer(
//                        url = generateArticle[it].video,
//                        modifier = Modifier.fillMaxSize(),
//                        isPlaying = pagerState.currentPage == it
//                    )
                }
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconColumnPub(ExtraContent(comment = generateArticle[it].comments.size), eventComment = {
                            show = true
                        })
                    }
                    ContentPublication(generateArticle[it].author, content = generateArticle[it].content)
                }

            }
            if (show){
                CommentsSheet(comments = generateArticle[it].comments) {
                    show = false
                }
            }

        }

    }
}

@Composable
@Preview(showBackground = true)
fun HomePreview(){
    HomeBuild()
}