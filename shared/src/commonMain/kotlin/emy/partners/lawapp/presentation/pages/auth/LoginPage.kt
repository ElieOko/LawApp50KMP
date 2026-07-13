package emy.partners.lawapp.presentation.pages.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.presentation.pages.session.CreationField
import emy.partners.lawapp.presentation.themes.BlueDark
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.app_name
import lawapp.shared.generated.resources.login_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginPage(){

}

@Composable
@Preview(showBackground = true)
fun LoginBuild(){
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Scaffold {
        Column(Modifier.fillMaxSize().background(color = Color(0xFF336DF1))) {
            Text(stringResource(Res.string.app_name), fontSize = 24.sp, )
            Spacer(Modifier.height(100.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {
                Box(Modifier.clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .fillMaxWidth().background(color = Color.White)){
                    Column(Modifier,horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(stringResource(Res.string.login_title), fontSize = 24.sp)
                        Text("Completez vos identifiant pour acceder", fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        Column(Modifier.padding(10.dp)) {
                            CreationField(
                                "Username",
                                username,
                                { username = it },
                                singleLine = true,
                                placeHolder = "niko"
                            )
                            CreationField(
                                "Password",
                                password,
                                { password = it },
                                singleLine = true,
                                placeHolder = "Interro 3, chapitre 5"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LoginPreview(){

}