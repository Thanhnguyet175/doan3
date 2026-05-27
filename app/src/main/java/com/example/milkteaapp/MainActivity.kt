    package com.example.milkteaapp

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.material3.Surface
    import androidx.compose.ui.Modifier
    import com.example.milkteaapp.ui.theme.MilkteaappTheme
    import com.example.milkteaapp.view.navigation.AppNavGraph
    import dagger.hilt.android.AndroidEntryPoint


    @AndroidEntryPoint
    class MainActivity : ComponentActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Cho phép nội dung vẽ sau status bar và navigation bar (edge-to-edge)
            enableEdgeToEdge()

            setContent {
                MilkteaappTheme {
                    // Surface bao ngoài để lấy màu nền từ MaterialTheme
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavGraph()
                    }
                }
            }
        }
    }