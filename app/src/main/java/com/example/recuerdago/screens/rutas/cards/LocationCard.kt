package com.example.recuerdago.screens.rutas.cards

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LocationCard(
    address: String,
    modifier: Modifier = Modifier,
    cardBackgroundColor: Color = if (isSystemInDarkTheme()) Color(0xFF2D2D44) else Color.White,
    textColor: Color = if (isSystemInDarkTheme()) Color.White else Color.Black
) {
    androidx.compose.material3.Card(
        modifier = modifier
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        androidx.compose.material3.Text(
            text = address,
            modifier = Modifier.padding(12.dp),
            color = textColor
        )
    }
}
