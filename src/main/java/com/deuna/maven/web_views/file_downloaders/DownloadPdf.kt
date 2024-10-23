package com.deuna.maven.web_views.file_downloaders

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.deuna.maven.web_views.base.*
import java.net.*


enum class FileExtension(val extension: String, val mimeType: String) {
    PDF("pdf", "application/pdf"),
    DOC("doc", "application/msword"),
    DOCX("docx", "application/msword"),
    XLS("xls", "application/vnd.ms-excel"),
    XLSX("xlsx", "application/vnd.ms-excel"),
    PPT("ppt", "application/vnd.ms-powerpoint"),
    PPTX("pptx", "application/vnd.ms-powerpoint"),
    ZIP("zip", "application/zip"),
    RAR("rar", "application/x-rar-compressed"),
    TAR("tar", "application/x-tar"),
    GZ("gz", "application/gzip");

    companion object {
        // Find enum case by file extension
        fun fromExtension(extension: String): FileExtension? {
            return entries.find { it.extension.equals(extension, ignoreCase = true) }
        }

        fun fromMime(mime: String): FileExtension? {
            return entries.find { it.mimeType.equals(mime, ignoreCase = true) }
        }

        // Static property to get the list of all file extensions as strings
        val allAsStrings: List<String> = entries.map { it.extension }
    }
}

val String.isFileDownloadUrl: Boolean
    get() {
        // Extract the part before query parameters (if any)
        val cleanUrl = this.substringBefore("?")
        // Check if the clean URL ends with a valid file extension
        return FileExtension.allAsStrings.any { ext -> cleanUrl.endsWith(".$ext", ignoreCase = true) }
    }

fun String.getFileExtension(): String? {
    // Extract the file extension before the query parameters
    val cleanUrl = this.substringBefore("?")
    // Extract the extension after the last "."
    return cleanUrl.substringAfterLast('.', "").takeIf { it.isNotEmpty() }
}

fun String.getMimeType(): String? {
    // Get the file extension and find the corresponding enum case
    val extension = getFileExtension() ?: return null
    return FileExtension.fromExtension(extension)?.mimeType
}

private fun getFileNameFromUrl(url: String): String {
    val fileName = url.substringAfterLast("/")
        .substringBefore("?")
        .substringBefore("#")

    return if (fileName.isNotEmpty()) fileName else "downloaded_file"
}

fun getMimeTypeFromUrl(url: String): String? {
    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.connect()
        connection.getHeaderField("Content-Type") // Get the Content-Type header
    } catch (e: Exception) {
        null // Return null if an error occurs
    }
}

// Function to download a PDF from an URL and save it to the device
fun BaseWebViewActivity.downloadFile(url: String) {
    if (url.isEmpty()) {
        return
    }

    // Attempt to get the file name and extension
    var fileName = getFileNameFromUrl(url)
    var extension = url.getFileExtension() ?: ""

    val mimeType = url.getMimeType() ?: getMimeTypeFromUrl(url)

    if (mimeType == null) {
        Toast.makeText(this, "No se pudo descargar el archivo", Toast.LENGTH_SHORT).show()
        return
    }

    if (extension.isEmpty()) {
        extension = FileExtension.fromMime(mimeType)?.extension ?: return
        fileName ="$fileName.$extension"
    }

    val request = DownloadManager.Request(Uri.parse(url)).apply {
        setTitle(fileName)
        setMimeType(mimeType)
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
    }

    val manager: DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    manager.enqueue(request)

    Toast.makeText(this, "Descarga iniciada", Toast.LENGTH_SHORT).show()
}