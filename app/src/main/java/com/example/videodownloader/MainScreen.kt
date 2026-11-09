package com.example.videodownloader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val HeaderBrush = Brush.linearGradient(
    listOf(Color(0xFF4B3BCB), Color(0xFF6E4FE8), Color(0xFF2D7FE6))
)

@Composable
fun MainScreen(vm: MainViewModel) {
    var input by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Header(vm)

        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InputCard(
                    vm = vm,
                    input = input,
                    onInputChange = { input = it },
                    onAdded = { input = "" }
                )
            }
            item { OptionsCard(vm) }
            if (vm.items.isNotEmpty()) {
                item { ListHeader(vm) }
                items(vm.items, key = { it.id }) { item ->
                    ItemCard(item, canDelete = !vm.running) { vm.remove(item.id) }
                }
            } else {
                item { EmptyState() }
            }
        }

        BottomBar(vm, input) { input = "" }
    }
}

// =====================================================================
//  ក្បាល
// =====================================================================
@Composable
private fun Header(vm: MainViewModel) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(HeaderBrush)
    ) {
        Column(
            Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_download),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "ទាញយកវីដេអូ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "YouTube · TikTok · Facebook និងច្រើនទៀត",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (vm.ready) Color(0xFF6FE3A8) else Color(0xFFFFD166))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (vm.ready) "ម៉ាស៊ីនទាញយកត្រៀមខ្លួនរួចរាល់" else "កំពុងរៀបចំម៉ាស៊ីនទាញយក...",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }
    }
}

// =====================================================================
//  កាតបញ្ចូលតំណ
// =====================================================================
@Composable
private fun InputCard(
    vm: MainViewModel,
    input: String,
    onInputChange: (String) -> Unit,
    onAdded: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val cs = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_link),
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "បន្ថែមតំណវីដេអូ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(10.dp))
            TextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("បិទភ្ជាប់តំណនៅទីនេះ (បានច្រើន៖ មួយបន្ទាត់មួយតំណ)")
                },
                minLines = 2,
                maxLines = 5,
                enabled = !vm.running,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = cs.surfaceVariant,
                    unfocusedContainerColor = cs.surfaceVariant,
                    disabledContainerColor = cs.surfaceVariant,
                )
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        val t = clipboard.getText()?.text
                        if (t.isNullOrBlank()) vm.status = "Clipboard គ្មានអ្វីទេ" else vm.addLinks(t)
                    },
                    enabled = !vm.running,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_paste),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("ពី Clipboard")
                }
                Button(
                    onClick = { if (vm.addLinks(input) > 0) onAdded() },
                    enabled = !vm.running && input.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("បន្ថែម")
                }
            }
        }
    }
}

// =====================================================================
//  កាតជម្រើស
// =====================================================================
@Composable
private fun OptionsCard(vm: MainViewModel) {
    val cs = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "ទម្រង់ទាញយក",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))

            // ជ្រើស MP4 / MP3
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cs.surfaceVariant)
                    .padding(4.dp)
            ) {
                ModeOption(
                    label = "វីដេអូ MP4",
                    icon = R.drawable.ic_video,
                    selected = vm.mode == Mode.VIDEO,
                    enabled = !vm.running,
                    modifier = Modifier.weight(1f)
                ) { vm.mode = Mode.VIDEO }
                ModeOption(
                    label = "សំឡេង MP3",
                    icon = R.drawable.ic_music,
                    selected = vm.mode == Mode.AUDIO,
                    enabled = !vm.running,
                    modifier = Modifier.weight(1f)
                ) { vm.mode = Mode.AUDIO }
            }

            AnimatedVisibility(visible = vm.mode == Mode.VIDEO) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "គុណភាពអតិបរមា",
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("360", "480", "720", "1080", "1440", "2160").forEach { q ->
                            FilterChip(
                                selected = vm.quality == q,
                                onClick = { vm.quality = q },
                                label = { Text("${q}p") },
                                enabled = !vm.running,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                UpdatePicker(vm)
            }
        }
    }
}

@Composable
private fun ModeOption(
    label: String,
    icon: Int,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) cs.primary else Color.Transparent, label = "bg")
    val fg by animateColorAsState(
        if (selected) cs.onPrimary else cs.onSurfaceVariant, label = "fg"
    )
    Row(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun UpdatePicker(vm: MainViewModel) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(
            onClick = { open = true },
            enabled = vm.ready && !vm.running && !vm.updating
        ) {
            Icon(
                painterResource(R.drawable.ic_refresh),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("អាប់ដេត yt-dlp")
        }
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

// =====================================================================
//  បញ្ជី
// =====================================================================
@Composable
private fun ListHeader(vm: MainViewModel) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "បញ្ជីទាញយក",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(cs.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 1.dp)
        ) {
            Text(
                "${vm.items.size}",
                style = MaterialTheme.typography.labelMedium,
                color = cs.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = { vm.clearAll() }, enabled = !vm.running) {
            Icon(
                painterResource(R.drawable.ic_delete),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("លុបទាំងអស់")
        }
    }
}

@Composable
private fun EmptyState() {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(cs.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(R.drawable.ic_download),
                contentDescription = null,
                tint = cs.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "មិនទាន់មានតំណក្នុងបញ្ជី",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "បិទភ្ជាប់តំណខាងលើ ឬចុច Share ពីកម្មវិធី TikTok / YouTube ដើម្បីបន្ថែមភ្លាមៗ",
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun ItemCard(item: DownloadItem, canDelete: Boolean, onDelete: () -> Unit) {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val success = successColor()
    val successBg = successContainer()

    val (iconRes, iconBg, iconFg) = when (item.state) {
        ItemState.PENDING ->
            Triple(R.drawable.ic_schedule, cs.surfaceVariant, cs.onSurfaceVariant)
        ItemState.DOWNLOADING, ItemState.PROCESSING ->
            Triple(R.drawable.ic_download, cs.primaryContainer, cs.primary)
        ItemState.DONE ->
            Triple(R.drawable.ic_check_circle, successBg, success)
        ItemState.FAILED ->
            Triple(R.drawable.ic_error, cs.errorContainer, cs.error)
        ItemState.STOPPED ->
            Triple(R.drawable.ic_stop, cs.surfaceVariant, cs.onSurfaceVariant)
    }
    val statusColor = when (item.state) {
        ItemState.DONE -> success
        ItemState.FAILED -> cs.error
        ItemState.DOWNLOADING, ItemState.PROCESSING -> cs.primary
        else -> cs.onSurfaceVariant
    }
    val animatedProgress by animateFloatAsState(
        targetValue = item.progress.coerceIn(0f, 1f), label = "progress"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(iconRes),
                        contentDescription = null,
                        tint = iconFg,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        hostLabel(item.url),
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        item.title ?: item.url,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        stateText(item),
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                }
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            painterResource(R.drawable.ic_close),
                            contentDescription = "លុប",
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (item.state == ItemState.DOWNLOADING || item.state == ItemState.PROCESSING) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    trackColor = cs.surfaceVariant
                )
            }

            if (item.state == ItemState.FAILED && item.error.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cs.errorContainer)
                        .padding(10.dp)
                ) {
                    Text(
                        item.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onErrorContainer,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ប៊ូតុងបើកឯកសារ / បើកថត ពេលទាញយករួច
            if (item.state == ItemState.DONE) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { openDownloadedFile(ctx, item.uri) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_play),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("បើកឯកសារ")
                    }
                    OutlinedButton(
                        onClick = { openDownloadFolder(ctx) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_folder),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("បើកថត")
                    }
                }
            }
        }
    }
}

private fun stateText(item: DownloadItem): String = when (item.state) {
    ItemState.PENDING -> "រង់ចាំ"
    ItemState.DOWNLOADING -> "កំពុងទាញយក ${(item.progress * 100).toInt()}%"
    ItemState.PROCESSING -> "កំពុងដំណើរការឯកសារ..."
    ItemState.DONE -> "ទាញយករួចរាល់ · Downloads/VideoDownloader"
    ItemState.FAILED -> "បរាជ័យ"
    ItemState.STOPPED -> "បានបញ្ឈប់"
}

// =====================================================================
//  របារខាងក្រោម
// =====================================================================
@Composable
private fun BottomBar(vm: MainViewModel, input: String, onAdded: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val remaining = vm.items.count { it.state != ItemState.DONE }

    Surface(
        color = cs.surface,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                vm.status,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        if (input.isNotBlank() && vm.addLinks(input) > 0) onAdded()
                        vm.startAll()
                    },
                    enabled = vm.ready && !vm.running && !vm.updating &&
                        (vm.items.isNotEmpty() || input.isNotBlank()),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_download),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (remaining > 0) "ទាញយកទាំងអស់ ($remaining)" else "ទាញយកទាំងអស់",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (vm.running) {
                    OutlinedButton(
                        onClick = { vm.stop() },
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_stop),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("បញ្ឈប់")
                    }
                }
            }
        }
    }
}
