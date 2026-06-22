package emy.partners.lawapp.presentation.components.basics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.verify
import org.jetbrains.compose.resources.painterResource

@Composable
fun StepPager(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, label ->
            val active = index == currentStep
            val completed = index < currentStep
            val nextCompleted = index < currentStep - 1

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    StepItem(
                        number = index + 1,
                        active = active,
                        completed = completed
                    )
                    if (index != steps.lastIndex) {
                        Box(
                            Modifier
                                .padding(horizontal = 6.dp)
                                .width(30.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    if (completed || nextCompleted) {
                                        Color(0xFF2563EB).copy(alpha = 0.85f)
                                    } else {
                                        Color(0xFFCBD5E1)
                                    }
                                )
                        )
                    }
                }
                Text(
                    text = label,
                    color = if (active || completed) Color(0xFF0F172A) else Color(0xFF64748B),
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StepItem(
    number: Int = 1,
    active: Boolean = false,
    completed: Boolean = false
) {
    val bgColor = when {
        completed -> Color(0xFF2563EB)
        active -> Color(0xFF0F172A)
        else -> Color(0xFFE2E8F0)
    }
    val textColor = if (active || completed) Color.White else Color(0xFF64748B)

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (completed) {
            Icon(
                painter = painterResource(Res.drawable.verify),
                contentDescription = null,
                tint = Color.White
            )
        } else {
            Text(
                text = number.toString(),
                color = textColor,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun StepPagerPreview() {
    StepPager(
        steps = listOf("Infos", "Questions", "Validation"),
        currentStep = 1
    )
}
