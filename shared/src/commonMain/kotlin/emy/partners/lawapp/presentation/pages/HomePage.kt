package emy.partners.lawapp.presentation.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import emy.partners.lawapp.presentation.components.basics.ContentPublication
import emy.partners.lawapp.presentation.components.basics.IconColumnPub
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.preview
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomePage(){
    HomeBuild()
}

@Composable
fun HomeBuild(){
    val scrollVertical = rememberScrollState()

    Column(Modifier.fillMaxSize()) {
        Box{
                Image(painterResource(Res.drawable.preview),null, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconColumnPub()
                    }
                    ContentPublication()
                    Spacer(Modifier.height(45.dp))
                }
            }
    }

}

@Composable
@Preview(showBackground = true)
fun HomePreview(){
    HomeBuild()
}