package com.example.videodownloader

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val scheme = if (Build.VERSION.SDK_INT >= 31) {
        if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    } else {
        if (dark) darkColorScheme() else lightColorScheme()
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun MainScreen(vm: MainViewModel) {
    var input by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(16.dp)
        ) {
            Text(
                "ទាញយកវីដេអូ",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            // ---- ប្រអប់បិទភ្ជាប់តំណ ----
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("បិទភ្ជាប់តំណវីដេអូ (បានច្រើន៖ មួយបន្ទាត់មួយតំណ)") },
                minLines = 3,
                maxLines = 5,
                enabled = !vm.running
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (vm.addLinks(input) > 0) input = "" },
                    enabled = !vm.running && input.isNotBlank()
                ) { Text("➕ បន្ថែម") }

                OutlinedButton(
                    onClick = {
                        val t = clipboard.getText()?.text
                        if (t.isNullOrBlank()) vm.status = "Clipboard គ្មានអ្វីទេ" else vm.addLinks(t)
                    },
                    enabled = !vm.running
                ) { Text("📋 Clipboard") }

                Spacer(Modifier.weight(1f))

                TextButton(
                    onClick = { vm.clearAll() },
                    enabled = !vm.running && vm.items.isNotEmpty()
                ) { Text("លុបទាំងអស់") }
            }

            // ---- ការកំណត់ ----
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = vm.mode == Mode.VIDEO,
                    onClick = { vm.mode = Mode.VIDEO },
                    label = { Text("វីដេអូ MP4") },
                    enabled = !vm.running
                )
                FilterChip(
                    selected = vm.mode == Mode.AUDIO,
                    onClick = { vm.mode = Mode.AUDIO },
                    label = { Text("សំឡេង MP3") },
                    enabled = !vm.running
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QualityPicker(vm)
                UpdatePicker(vm)
            }

            // ---- បញ្ជីតំណ ----
            if (vm.items.isEmpty()) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "បញ្ជីទទេ\nបិទភ្ជាប់តំណខាងលើ ឬចុច Share ពីកម្មវិធី TikTok / YouTube",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(vm.items, key = { it.id }) { item ->
                        ItemCard(item, canDelete = !vm.running) { vm.remove(item.id) }
                    }
                }
            }

            // ---- ស្ថានភាព និងប៊ូតុងទាញយក ----
            Text(
                vm.status,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (input.isNotBlank() && vm.addLinks(input) > 0) input = ""
                        vm.startAll()
                    },
                    enabled = vm.ready && !vm.running && !vm.updating &&
                        (vm.items.isNotEmpty() || input.isNotBlank()),
                    modifier = Modifier.weight(1f)
                ) { Text("⬇ ទាញយកទាំងអស់") }

                if (vm.running) {
                    OutlinedButton(onClick = { vm.stop() }) { Text("⏹ បញ្ឈប់") }
                }
            }
        }
    }
}

@Composable
fun QualityPicker(vm: MainViewModel) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { open = true },
            enabled = !vm.running && vm.mode == Mode.VIDEO
        ) { Text("គុណភាព ≤ ${vm.quality}p ▾") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            listOf("360", "480", "720", "1080", "1440", "2160").forEach { q ->
                DropdownMenuItem(
                    text = { Text("${q}p") },
                    onClick = {
                        vm.quality = q
                        open = false
                    }
                )
            }
        }
    }
}

@Composable
fun UpdatePicker(vm: MainViewModel) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { open = true },
            enabled = vm.ready && !vm.running && !vm.updating
        ) { Text("🔄 អាប់ដេត yt-dlp ▾") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text("Stable (ស្ថិរភាព)") },
                onClick = {
                    open = false
                    vm.updateYtdlp(nightly = false)
                }
            )
            DropdownMenuItem(
                text = { Text("Nightly (ថ្មីបំផុត៖ ជួយដោះស្រាយ TikTok)") },
                onClick = {
                    open = false
                    vm.updateYtdlp(nightly = true)
                }
            )
        }
    }
}

@Composable
fun ItemCard(item: DownloadItem, canDelete: Boolean, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.title ?: item.url,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    stateText(item),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.state == ItemState.FAILED) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (item.state == ItemState.DOWNLOADING || item.state == ItemState.PROCESSING) {
                    LinearProgressIndicator(
                        progress = { item.progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )
                }
                if (item.state == ItemState.FAILED && item.error.isNotBlank()) {
                    Text(
                        item.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (canDelete) {
                TextButton(onClick = onDelete) { Text("✕") }
            }
        }
    }
}

private fun stateText(item: DownloadItem): String = when (item.state) {
    ItemState.PENDING -> "⏳ រង់ចាំ"
    ItemState.DOWNLOADING -> "⬇ ${(item.progress * 100).toInt()}%"
    ItemState.PROCESSING -> "⚙ កំពុងដំណើរការ..."
    ItemState.DONE -> "✅ រួចរាល់ (Downloads/VideoDownloader)"
    ItemState.FAILED -> "❌ បរាជ័យ"
    ItemState.STOPPED -> "⏹ បានបញ្ឈប់"
}
