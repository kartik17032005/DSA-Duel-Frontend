package com.example.dsa_duel.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsa_duel.R

@Composable
fun InputTextField(
    modifier: Modifier = Modifier,
    valueState: MutableState<String>,
    labelId: String,
    enabled: Boolean,
    isSingleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onAction: KeyboardActions = KeyboardActions.Default,
    leadingIcon: ImageVector = Icons.Filled.Person
) {
    val neonCyan = Color(0xFF00C2FF)
    val mutedIndigo = Color(0xFF6C6C82)
    val translucentSurface = Color(0xFF141425).copy(alpha = 0.8f)

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        value = valueState.value,
        label = {
            Text(
                text = labelId,
                fontFamily = FontFamily(Font(R.font.rajdhani_medium)),
                letterSpacing = 2.sp
            )
        },
        onValueChange = { newVal ->
            valueState.value = newVal
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null
            )
        },
        textStyle = TextStyle(
            fontSize = 16.sp, 
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.rajdhani_medium))
        ),
        enabled = enabled,
        singleLine = isSingleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = onAction,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = neonCyan,
            unfocusedBorderColor = mutedIndigo.copy(alpha = 0.4f),
            focusedLabelColor = neonCyan,
            unfocusedLabelColor = mutedIndigo,
            cursorColor = neonCyan,
            focusedLeadingIconColor = neonCyan,
            unfocusedLeadingIconColor = mutedIndigo,
            focusedContainerColor = translucentSurface,
            unfocusedContainerColor = translucentSurface.copy(alpha = 0.5f),
        )
    )
}
