package com.example.videodownloader

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast

/** បើកឯកសារដែលបានទាញយក ដោយកម្មវិធីចាក់វីដេអូ/សំឡេងរបស់ទូរស័ព្ទ */
fun openDownloadedFile(context: Context, uriString: String?) {
    if (uriString.isNullOrEmpty()) {
        toast(context, "រកមិនឃើញឯកសារ")
        return
    }
    val uri = Uri.parse(uriString)
    val mime = context.contentResolver.getType(uri) ?: "*/*"
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, mime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        toast(context, "គ្មានកម្មវិធីសម្រាប់បើកឯកសារនេះ ឬឯកសារត្រូវបានលុបចោល")
    }
}

/** បើកថត Downloads/VideoDownloader (បើមិនបាន បើកថត Downloads ទូទៅ) */
fun openDownloadFolder(context: Context) {
    val folder = DocumentsContract.buildDocumentUri(
        "com.android.externalstorage.documents",
        "primary:Download/VideoDownloader"
    )
    val first = Intent(Intent.ACTION_VIEW)
        .setDataAndType(folder, DocumentsContract.Document.MIME_TYPE_DIR)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(first)
        return
    } catch (e: Exception) {
        // ទូរស័ព្ទខ្លះមិនគាំទ្រការបើកថតដោយផ្ទាល់ សាកល្បងវិធីបន្ទាប់
    }
    try {
        context.startActivity(
            Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        toast(context, "មិនអាចបើកថតបានទេ សូមរកក្នុង Files → Downloads → VideoDownloader")
    }
}

/** ឈ្មោះគេហទំព័រប្រភព សម្រាប់បង្ហាញលើកាតនីមួយៗ */
fun hostLabel(url: String): String {
    val host = runCatching { Uri.parse(url).host.orEmpty().lowercase() }.getOrDefault("")
    return when {
        host.isEmpty() -> "Link"
        "tiktok" in host -> "TikTok"
        "youtu" in host -> "YouTube"
        "facebook" in host || "fb.watch" in host -> "Facebook"
        "instagram" in host -> "Instagram"
        "twitter" in host || host == "x.com" || host.endsWith(".x.com") -> "X"
        else -> host.removePrefix("www.")
    }
}

private fun toast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}
