package emy.partners.lawapp.presentation.pages.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RegisterPage() {
    RegisterBuild()
}

@Composable
fun RegisterBuild() {
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pseudo by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Masculin") }
    var typeCompte by remember { mutableStateOf("Etudiant") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF020617), Color(0xFF1E3A8A), Color(0xFF38BDF8))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.96f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "Inscription",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Creez un compte en quelques secondes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569)
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = prenom,
                    onValueChange = { prenom = it },
                    label = { Text("Prenom") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = pseudo,
                    onValueChange = { pseudo = it },
                    label = { Text("Pseudo") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { Text("Telephone") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                SelectionRow(
                    options = listOf("Masculin", "Feminin"),
                    selected = gender,
                    onSelected = { gender = it }
                )

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Type de compte",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                SelectionRow(
                    options = listOf("Enseignant", "Etudiant"),
                    selected = typeCompte,
                    onSelected = { typeCompte = it }
                )

                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Creer mon compte", style = MaterialTheme.typography.titleMedium)
                }

                TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("Deja inscrit ? Se connecter")
                }
            }
        }
    }
}

@Composable
private fun SelectionRow(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column {
        options.forEach { option ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = selected == option,
                    onClick = { onSelected(option) }
                )
                Text(option, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun RegisterPreview() {
    RegisterBuild()
}