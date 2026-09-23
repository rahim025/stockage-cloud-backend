package com.stockagecloud.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Couleurs = lightColorScheme()

@Composable
fun StockageCloudTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Couleurs,
        content = content
    )
}
