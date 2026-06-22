package emy.partners.lawapp.presentation.components.basics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.verify
import org.jetbrains.compose.resources.painterResource

@Composable
@Preview(showBackground = true)
fun StepPager(){
    Row {
        StepItem(number = 1, active = true,true )
        Spacer(Modifier.width(10.dp))
        HorizontalDivider(Modifier.width(80.dp).height(30.dp).absoluteOffset(y = 15.dp), color =Color(0xFF2563EB).copy(alpha = 0.94f), thickness = 2.dp)
        Spacer(Modifier.width(10.dp))
        StepItem(number = 1, active = true,true )
        Spacer(Modifier.width(10.dp))
        HorizontalDivider(Modifier.width(80.dp).height(30.dp).absoluteOffset(y = 15.dp), color = Color(0xFF2563EB).copy(alpha = 0.94f), thickness = 2.dp)
        Spacer(Modifier.width(10.dp))
        StepItem(number = 3, active = false,true )
    }
}

@Composable
fun StepItem(
    number: Int = 1,
    active: Boolean = false,
    completed: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RectangleShape)
            .background(
                when {
                    completed -> Color(0xFF2563EB).copy(alpha = 0.94f)
                    active -> Color(0xFF0C130F)
                    else -> Color(0xFF374C4A)
                }
            )
           ,
        contentAlignment = Alignment.Center
    ) {

        if (completed){
            Icon(painterResource(Res.drawable.verify),null, tint = Color.White)
        } else{
            Text(
                text = number.toString(),
                color = Color.White ,
                fontWeight = FontWeight.Bold
            )
        }
    }
}