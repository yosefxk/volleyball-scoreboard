package com.volleyball.scoreboard

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

object ScoreboardLogger {

    private const val TAG = "ScoreboardLogger"
    const val FILE_NAME = "volleyball-scoreboard.log"
    private val executor = Executors.newSingleThreadExecutor()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun log(context: Context, tag: String, message: String) {
        val timestamp = synchronized(dateFormat) { dateFormat.format(Date()) }
        val line = "[$timestamp] [$tag] $message\n"
        Log.i("VBScoreboard", line.trimEnd())

        executor.execute {
            try {
                // 1. Append to local app external files dir (reliable, no permissions needed)
                val localFile = File(context.getExternalFilesDir(null), FILE_NAME)
                FileOutputStream(localFile, true).use { fos ->
                    fos.write(line.toByteArray(Charsets.UTF_8))
                    fos.flush()
                }

                // 2. Sync to public Downloads folder so user can easily open it in Samsung "My Files" -> "Downloads"
                syncToDownloads(context, localFile)
            } catch (e: Exception) {
                Log.e(TAG, "Failed writing log line", e)
            }
        }
    }

    private fun syncToDownloads(context: Context, localFile: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val projection = arrayOf(MediaStore.MediaColumns._ID)
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
                val selectionArgs = arrayOf(FILE_NAME, "${Environment.DIRECTORY_DOWNLOADS}%")

                var uri = resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                        ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                    } else null
                }

                if (uri == null) {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                }

                if (uri != null) {
                    resolver.openOutputStream(uri, "rwt")?.use { os ->
                        localFile.inputStream().use { input ->
                            input.copyTo(os)
                        }
                        os.flush()
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadsDir.mkdirs()
                val destFile = File(downloadsDir, FILE_NAME)
                localFile.copyTo(destFile, overwrite = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing to Downloads folder", e)
        }
    }

    fun shareLog(context: Context) {
        try {
            val localFile = File(context.getExternalFilesDir(null), FILE_NAME)
            if (!localFile.exists()) {
                log(context, "INFO", "Log file initialized.")
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", localFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Volleyball Scoreboard Log")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share Volleyball Scoreboard Log").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share log", e)
        }
    }

    fun openLog(context: Context) {
        try {
            val localFile = File(context.getExternalFilesDir(null), FILE_NAME)
            if (!localFile.exists()) {
                log(context, "INFO", "Log file initialized.")
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", localFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/plain")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fall back to share sheet if direct viewer intent fails
            shareLog(context)
        }
    }
}
