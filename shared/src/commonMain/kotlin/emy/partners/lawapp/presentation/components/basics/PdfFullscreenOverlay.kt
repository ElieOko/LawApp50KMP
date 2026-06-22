package emy.partners.lawapp.presentation.components.basics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import emy.partners.lawapp.presentation.themes.BlueDark

@Composable
fun PdfFullscreenOverlay(
    uri: String,
    fileName: String,
    onClose: () -> Unit
) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onClose
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.86f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Lecteur PDF plein ecran", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    Text(fileName, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BlueDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Fermer", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            PlatformPdfViewer(
                uri = uri,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(10.dp)
            )
        }
    }
}

