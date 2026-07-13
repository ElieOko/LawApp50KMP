package emy.partners.lawapp.presentation.pages

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.Constants
import emy.partners.lawapp.presentation.themes.BlueDark
import emy.partners.lawapp.presentation.themes.BlueDarkEffect
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.one
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProfilPage(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState(),
    onConnectClick: () -> Unit = {},
) {
    ProfilBuild(
        modifier = modifier,
        scrollVertical = scrollVertical,
        onConnectClick = onConnectClick,
    )
}

@Composable
fun ProfilBuild(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState(),
    onConnectClick: () -> Unit = {},
) {
    val evaluations = Constants.evaluations
    val completedCount = evaluations.count { it.score != null }
    val averageScore = evaluations.mapNotNull { it.score }.average().takeIf { !it.isNaN() }?.toInt() ?: 0

    Column(
        Modifier.fillMaxSize()
            .verticalScroll(scrollVertical)
    ) {
        Column(
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            BlueDark.copy(alpha = 0.94f),
                            BlueDarkEffect.copy(alpha = 0.92f)
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = painterResource(Res.drawable.one),
                    contentDescription = "Photo de profil",
                    modifier = Modifier
                        .size(94.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("Emy Mayumbi", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
            Text("@lawapp_member", color = Color.White.copy(alpha = 0.65f), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileMetric("Evaluations", evaluations.size.toString(), Modifier.weight(1f))
                ProfileMetric("Terminees", completedCount.toString(), Modifier.weight(1f))
                ProfileMetric("Moyenne", "$averageScore%", Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(16.dp))
        ProfileConnectCard(onConnectClick = onConnectClick)
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(18.dp)
        ) {
            Text("Progression globale", color = Color.Black.copy(alpha = 0.88f), fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                "Tu progresses surtout en droit civil et constitutionnel. Continue les quiz courts pour garder le rythme.",
                color = Color.Black.copy(alpha = 0.56f),
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { 0.72f },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50.dp)),
                color = Color(0xFF2563EB),
                trackColor = Color.Black.copy(alpha = 0.08f)
            )
            Spacer(Modifier.height(8.dp))
            Text("72% du parcours initial", color = BlueDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text("Activite recente", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 21.sp)
        Spacer(Modifier.height(10.dp))
        ProfileActivity("Diagnostic droit civil repris", "16 questions completees")
        Spacer(Modifier.height(10.dp))
        ProfileActivity("Quiz juridique", "2 bonnes reponses sur 3")
        Spacer(Modifier.height(10.dp))
        ProfileActivity("Lecture explore", "Droit moderne en RDC")
        Spacer(Modifier.height(16.dp))
        Text("Raccourcis", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 21.sp)
        Spacer(Modifier.height(10.dp))
        ProfileShortcut(
            title = "Connectez-vous",
            subtitle = "Ouvrir la page de connexion",
            onClick = onConnectClick,
        )
        Spacer(Modifier.height(10.dp))
        ProfileShortcut("Badges et certificats", "Consulte tes preuves de progression")
        Spacer(Modifier.height(10.dp))
        ProfileShortcut("Parametres d'apprentissage", "Objectifs, rappels et preferences")
        Spacer(Modifier.height(90.dp))
    }
}

@Composable
private fun ProfileConnectCard(onConnectClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .padding(18.dp)
    ) {
        Text(
            "Connectez-vous",
            color = Color.Black.copy(alpha = 0.9f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 21.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Connectez-vous pour synchroniser votre progression, votre profil et vos evaluations.",
            color = Color.Black.copy(alpha = 0.56f),
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onConnectClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2F6FED),
                contentColor = Color.White
            )
        ) {
            Text("Se connecter", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(label, color = Color.White.copy(alpha = 0.68f), fontSize = 11.sp)
    }
}

@Composable
private fun ProfileActivity(title: String, subtitle: String) {
    ProfileListItem(title = title, subtitle = subtitle, pill = "Recent")
}

@Composable
private fun ProfileShortcut(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
) {
    ProfileListItem(
        title = title,
        subtitle = subtitle,
        pill = "Ouvrir",
        onClick = onClick,
    )
}

@Composable
private fun ProfileListItem(
    title: String,
    subtitle: String,
    pill: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White.copy(alpha = 0.62f), fontSize = 12.sp)
        }
        Text(
            pill,
            color = BlueDark,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ProfilPreview(){
    ProfilBuild()
}