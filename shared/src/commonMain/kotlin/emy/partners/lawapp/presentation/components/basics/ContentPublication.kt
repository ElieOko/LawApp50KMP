package emy.partners.lawapp.presentation.components.basics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.one
import org.jetbrains.compose.resources.painterResource

@Composable
@Preview(showBackground = false)
fun ContentPublication(
    username : String = "Roni",
    member : String = "Community",
    content : String = "LawApp 50, vulgarisation du droit dans votre poche.",
){
    Column(Modifier.padding(5.dp)) {
        Row {
            Image(painter = painterResource(Res.drawable.one),null, Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(60),
                ),
                contentScale = ContentScale.Crop)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.absoluteOffset(y = 12.dp)){
                Row {
                    Text(username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.width(5.dp))
                    SuggestionChip(
                        modifier = Modifier.absoluteOffset(y = (-12).dp),
                        enabled = true,
                        shape = RoundedCornerShape(35.dp),
                        label = {
                            Text(member, color = Color.White)
                        },
                        onClick = {},
                        colors = SuggestionChipDefaults.suggestionChipColors()
                    )
                }
            }
        }

        Text(content,color = Color.White.copy(0.9f), fontSize = 18.sp,modifier = Modifier.absoluteOffset(y = (-4).dp))
    }
}