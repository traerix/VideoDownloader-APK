package com.example.videodownloader

import android.app.Application
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class ItemState { PENDING, DOWNLOADING, PROCESSING, DONE, FAILED, STOPPED }

enum class Mode { VIDEO, AUDIO }

data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String? = null,
    val state: ItemState = ItemState.PENDING,
    val progress: Float = 0f,
    val error: String = "",
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val items = mutableStateListOf<DownloadItem>()
    var mode by mutableStateOf(Mode.VIDEO)
    var quality by mutableStateOf("1080")
    var ready by mutableStateOf(false)
    var running by mutableStateOf(false)
    var updating by mutableStateOf(false)
    var status by mutableStateOf("កំពុងរៀបចំម៉ាស៊ីនទាញយក (លើកដំបូងអាចយូរបន្តិច)...")

    @Volatile
    private var stopRequested = false

    @Volatile
    private var currentId: String? = null

    private val urlRegex = Regex("https?://\\S+")
    private val ansiRegex = Regex("\u001B\\[[0-9;]*m")
    private val formatSuffixRegex = Regex("\\.f[0-9A-Za-z-]+$")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().init(getApplication<Application>())
                FFmpeg.getInstance().init(getApplication<Application>())
                val v = YoutubeDL.getInstance().versionName(getApplication<Application>())
                ui {
                    ready = true
                    status = "ត្រៀមខ្លួនរួចរាល់ (yt-dlp $v)"
                }
            } catch (e: Exception) {
                ui { status = "រៀបចំម៉ាស៊ីនមិនបាន៖ ${e.message}" }
            }
        }
    }

    private fun ui(block: () -> Unit) {
        viewModelScope.launch(Dispatchers.Main) { block() }
    }

    private fun update(id: String, f: (DownloadItem) -> DownloadItem) {
        val i = items.indexOfFirst { it.id == id }
        if (i >= 0) items[i] = f(items[i])
    }

    // ------------------------------------------------------------
    //  គ្រប់គ្រងបញ្ជី
    // ------------------------------------------------------------
    /** បន្ថែមតំណពីអត្ថបទ (បានច្រើន) ត្រឡប់ចំនួនតំណថ្មីដែលបានបន្ថែម */
    fun addLinks(raw: String): Int {
        val existing = items.map { it.url }.toMutableSet()
        var added = 0
        var skipped = 0
        for (m in urlRegex.findAll(raw)) {
            val u = m.value.trimEnd('.', ',', ';', ')', '\'', '"')
            if (u in existing) {
                skipped++
                continue
            }
            existing.add(u)
            items.add(DownloadItem(url = u))
            added++
        }
        status = when {
            added == 0 && skipped == 0 -> "រកមិនឃើញតំណ (ត្រូវចាប់ផ្តើមដោយ http:// ឬ https://)"
            skipped > 0 -> "បានបន្ថែម $added តំណ (រំលង $skipped តំណស្ទួន)"
            else -> "បានបន្ថែម $added តំណ"
        }
        return added
    }

    fun remove(id: String) {
        if (running) return
        items.removeAll { it.id == id }
    }

    fun clearAll() {
        if (running) return
        items.clear()
        status = "បញ្ជីទទេ"
    }

    // ------------------------------------------------------------
    //  ទាញយក
    // ------------------------------------------------------------
    fun startAll() {
        if (running || !ready) return
        val queue = items.filter { it.state != ItemState.DONE }.map { it.id to it.url }
        if (queue.isEmpty()) {
            status = "សូមបន្ថែមតំណវីដេអូជាមុនសិន"
            return
        }
        queue.forEach { (id, _) ->
            update(id) { it.copy(state = ItemState.PENDING, progress = 0f, error = "") }
        }
        stopRequested = false
        running = true
        val mode = this.mode
        val quality = this.quality

        viewModelScope.launch(Dispatchers.IO) {
            var ok = 0
            var failed = 0
            var stopped = false

            for ((n, pair) in queue.withIndex()) {
                val (id, url) = pair
                if (stopRequested) {
                    stopped = true
                    break
                }
                ui {
                    status = "កំពុងទាញយក ${n + 1}/${queue.size}"
                    update(id) { it.copy(state = ItemState.DOWNLOADING, progress = 0f) }
                }
                try {
                    downloadOne(id, url, mode, quality)
                    ok++
                    ui { update(id) { it.copy(state = ItemState.DONE, progress = 1f) } }
                } catch (e: YoutubeDL.CanceledException) {
                    stopped = true
                    ui { update(id) { it.copy(state = ItemState.STOPPED) } }
                    break
                } catch (e: Exception) {
                    if (stopRequested) {
                        stopped = true
                        ui { update(id) { it.copy(state = ItemState.STOPPED) } }
                        break
                    }
                    failed++
                    val msg = cleanError(e)
                    ui { update(id) { it.copy(state = ItemState.FAILED, error = msg) } }
                }
            }

            val summary = if (stopped) {
                "បានបញ្ឈប់។ រួចរាល់ $ok បរាជ័យ $failed"
            } else {
                "បញ្ចប់! រួចរាល់ $ok បរាជ័យ $failed  |  រក្សាទុកក្នុង Downloads/VideoDownloader"
            }
            ui {
                currentId = null
                running = false
                status = summary
            }
        }
    }

    fun stop() {
        if (!running) return
        stopRequested = true
        status = "កំពុងបញ្ឈប់..."
        val id = currentId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            YoutubeDL.getInstance().destroyProcessById(id)
        }
    }

    private fun downloadOne(id: String, url: String, mode: Mode, quality: String) {
        val app = getApplication<Application>()
        val tmp = File(app.getExternalFilesDir(null) ?: app.filesDir, "tmp/$id")
        tmp.deleteRecursively()
        tmp.mkdirs()
        try {
            val req = YoutubeDLRequest(url)
            req.addOption("-o", "${tmp.absolutePath}/%(title)s.%(ext)s")
            req.addOption("--trim-filenames", 150)
            req.addOption("--no-playlist")
            req.addOption("--no-mtime")
            if (mode == Mode.AUDIO) {
                req.addOption("-f", "bestaudio/best")
                req.addOption("-x")
                req.addOption("--audio-format", "mp3")
                req.addOption("--audio-quality", 0)
            } else {
                val q = quality
                req.addOption(
                    "-f",
                    "bv*[height<=$q][ext=mp4]+ba[ext=m4a]/b[height<=$q][ext=mp4]/" +
                        "bv*[height<=$q]+ba/b[height<=$q]/b"
                )
                req.addOption("--merge-output-format", "mp4")
            }

            currentId = id
            val callback: (Float, Long, String) -> Unit = { progress, _, line ->
                val dest = line.substringAfter("Destination: ", "")
                if (dest.isNotEmpty()) {
                    val title = File(dest.trim()).nameWithoutExtension
                        .replace(formatSuffixRegex, "")
                    ui { update(id) { it.copy(title = title) } }
                }
                val processing = line.contains("[Merger]") ||
                    line.contains("[ExtractAudio]") ||
                    line.contains("[VideoConvertor]")
                if (processing) {
                    ui { update(id) { it.copy(state = ItemState.PROCESSING, progress = 1f) } }
                } else if (progress >= 0f) {
                    ui {
                        update(id) {
                            it.copy(state = ItemState.DOWNLOADING, progress = progress / 100f)
                        }
                    }
                }
            }
            YoutubeDL.getInstance().execute(request = req, processId = id, callback = callback)

            val files = tmp.listFiles { f ->
                f.isFile && !f.name.endsWith(".part") && !f.name.endsWith(".ytdl") &&
                    !f.name.endsWith(".temp") && !f.name.startsWith(".")
            }?.toList().orEmpty()
            if (files.isEmpty()) throw IllegalStateException("រកមិនឃើញឯកសារដែលបានទាញយក")
            files.forEach { saveToDownloads(it) }
        } finally {
            currentId = null
            tmp.deleteRecursively()
        }
    }

    /** ចម្លងឯកសារទៅ Downloads/VideoDownloader (មិនត្រូវការ permission លើ Android 10+) */
    private fun saveToDownloads(file: File) {
        val resolver = getApplication<Application>().contentResolver
        val mime = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file.extension.lowercase()) ?: "application/octet-stream"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/VideoDownloader"
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("មិនអាចបង្កើតឯកសារក្នុង Downloads")
        resolver.openOutputStream(uri)?.use { out ->
            file.inputStream().use { it.copyTo(out) }
        } ?: throw IllegalStateException("មិនអាចសរសេរឯកសារក្នុង Downloads")
        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }

    private fun cleanError(e: Exception): String {
        val raw = ansiRegex.replace(e.message ?: e.toString(), "")
        val lines = raw.lines()
        val line = lines.lastOrNull { it.contains("ERROR", ignoreCase = true) }
            ?: lines.lastOrNull { it.isNotBlank() }
            ?: raw
        return line.trim().take(300)
    }

    // ------------------------------------------------------------
    //  អាប់ដេត yt-dlp
    // ------------------------------------------------------------
    fun updateYtdlp(nightly: Boolean) {
        if (running || updating || !ready) return
        updating = true
        status = "កំពុងអាប់ដេត yt-dlp..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val channel: YoutubeDL.UpdateChannel =
                    if (nightly) YoutubeDL.UpdateChannel.NIGHTLY else YoutubeDL.UpdateChannel.STABLE
                val result = YoutubeDL.getInstance().updateYoutubeDL(getApplication<Application>(), channel)
                val v = YoutubeDL.getInstance().versionName(getApplication<Application>())
                ui {
                    status = if (result == YoutubeDL.UpdateStatus.DONE) {
                        "អាប់ដេតរួចរាល់ ($v)"
                    } else {
                        "ជាកំណែចុងក្រោយរួចហើយ ($v)"
                    }
                }
            } catch (e: Exception) {
                ui { status = "អាប់ដេតមិនបាន៖ ${e.message}" }
            } finally {
                ui { updating = false }
            }
        }
    }
}
