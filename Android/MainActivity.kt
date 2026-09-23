package com.stockagecloud.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.stockagecloud.app.ui.navigation.NavGraph
import com.stockagecloud.app.ui.theme.StockageCloudTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as StockageCloudApp

        setContent {
            StockageCloudTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph(app = app)
                }
            }
        }
    }
}
