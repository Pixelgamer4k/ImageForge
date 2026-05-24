package com.imageforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.imageforge.app.ui.screens.MainScreen
import com.imageforge.app.ui.screens.MainViewModel
import com.imageforge.app.ui.theme.ImageForgeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the layout draw fully behind the system status and navigation bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ImageForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.imageforge.app.ui.theme.ForestVoid
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
