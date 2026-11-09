package com.example.videodownloader

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // របារស្ថានភាពថ្លា ជាមួយអក្សរពណ៌ស (ដោយសារក្បាលកម្មវិធីជាពណ៌ gradient)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        handleIntent(intent)
        setContent {
            // រក្សាអេក្រង់ភ្លឺ ពេលកំពុងទាញយក (កុំឱ្យទូរស័ព្ទដេកហើយឈប់ទាញយក)
            LaunchedEffect(vm.running) {
                if (vm.running) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
            AppTheme { MainScreen(vm) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /** ទទួលតំណដែលបានចែករំលែក (Share) ពីកម្មវិធីផ្សេង */
    private fun handleIntent(i: Intent?) {
        if (i?.action == Intent.ACTION_SEND) {
            i.getStringExtra(Intent.EXTRA_TEXT)?.let { vm.addLinks(it) }
        }
    }
}
