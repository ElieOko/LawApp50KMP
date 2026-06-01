package emy.partners.lawapp.presentation.pages

import VideoPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
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
import emy.partners.lawapp.PlatformVideoPlayer
import emy.partners.lawapp.data.Constants.generateArticle
import emy.partners.lawapp.presentation.components.basics.ContentPublication
import emy.partners.lawapp.presentation.components.basics.IconColumnPub

@Composable
fun HomePage(){
    HomeBuild()
}

@Composable
fun HomeBuild(){
    val pagerState = rememberPagerState(pageCount = { generateArticle.size })
    Column(Modifier.fillMaxSize()) {
        VerticalPager(state = pagerState){
            Box(Modifier.background(Color.Black)){
                if (!generateArticle[it].isPlay){
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(generateArticle[it].image)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Translated description of what the image contains",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else{
                    VideoPlayer(
                        modifier = Modifier.fillMaxSize(),
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
                        IconColumnPub(generateArticle[it].extra)
                    }
                    ContentPublication(generateArticle[it].author, content = generateArticle[it].content)
                    Spacer(Modifier.height(45.dp))
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