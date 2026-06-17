package emy.partners.lawapp.presentation.components.basics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import emy.partners.lawapp.domain.models.DataSelect
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.down
import lawapp.shared.generated.resources.one
import lawapp.shared.generated.resources.up
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectInputField(
    modifier : Modifier = Modifier,
    textValueIn: String,
    onChangeText: (DataSelect) -> Unit = {},
    itemList: List<DataSelect>?,
    icon : Int = 0
) {
    var expanded by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(textValueIn) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {expanded = false}
    ) {
        OutlinedTextField(
            value = textValue,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Black.copy(0.4f),
                focusedBorderColor = Color.Blue.copy(alpha = 0.6f)
            ),
            textStyle = TextStyle(color = Color.Black),
            onValueChange = { textValue = it },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            modifier = modifier
                .fillMaxWidth()
                .menuAnchor()
                .clickable{
                    expanded = !expanded
                }
            ,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        painterResource(if (!expanded) Res.drawable.down else Res.drawable.up),
                        contentDescription = "Sélectionner",
                        tint = Color.Black
                    )
                }
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded=false}
        ) {
            itemList?.forEach { item ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = {
                        Text(text = item.name, color = Color.Black)
                    },
                    onClick = {
                        textValue = item.name
                        onChangeText(item) // Appel de la fonction de changement
                        expanded = false
                        onChangeText(item)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun InputFieldCompose(
    modifier: Modifier = Modifier,
    value : String,
    onValueChange: (String) -> Unit,
    iconLast : DrawableResource? = Res.drawable.one,
    isSingle : Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder : String = "",
    onclickLastIcon : ()-> Unit = {}
) {
    OutlinedTextField(
        value = value,
        keyboardOptions = keyboardOptions,
        onValueChange = onValueChange,
        placeholder = {Text(placeholder)},
        trailingIcon = {
            if (iconLast != null){
                IconButton(onClick = onclickLastIcon) {
                    Icon(painterResource(iconLast),null, modifier= Modifier.size(25.dp))
                }
            }
        },
        maxLines = 1,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Black.copy(0.4f),
            focusedBorderColor = Color.Blue.copy(alpha = 0.6f)
        ),
        textStyle = TextStyle(color = Color.Black),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        singleLine = isSingle
    )
}