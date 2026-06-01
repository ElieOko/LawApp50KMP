package emy.partners.lawapp.presentation.components.basics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
@Preview()
fun TopBarCustom(){
    TopAppBar(
        modifier = Modifier,
        title = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
//                Box(){
//                    Surface(color = Color(0xFF32358D),shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(5.dp).width(200.dp).height(50.dp)){
//                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
//                            Text("LawApp50", color = Color.White, fontWeight = FontWeight.Bold)
//                        }
//                    }
//                }
                SuggestionChip(
                    modifier = Modifier.absoluteOffset(y = (-12).dp),
                    enabled = true,
                    shape = RoundedCornerShape(35.dp),
                    label = {
                        Text("LawApp50", color = Color.White,fontWeight = FontWeight.Bold)
                    },
                    onClick = {},
                    colors = SuggestionChipDefaults.suggestionChipColors()
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {

        },
    )
}