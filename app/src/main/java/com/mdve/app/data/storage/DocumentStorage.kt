
package com.mdve.app.data.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns

class DocumentStorage(
    private val context: Context
) {

    private val resolver = context.contentResolver

    fun rememberPermission(
        uri: Uri,
        flags: Int
    ) {
        val permissionFlags = flags and (
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

        try {
            resolver.takePersistableUriPermission(
                uri,
                permissionFlags
            )
        } catch (_: SecurityException) {
            // Some providers do not support persistable access.
        }
    }

    fun readText(uri: Uri): String {
        return resolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).use {
                it.readText()
            }
        } ?: throw IllegalStateException(
            "Unable to open document"
        )
    }

    fun writeText(
        uri: Uri,
        content: String
    ) {
        resolver.openOutputStream(uri, "wt")?.use { stream ->
            stream.writer(Charsets.UTF_8).use {
                it.write(content)
                it.flush()
            }
        } ?: throw IllegalStateException(
            "Unable to save document"
        )
    }

    fun getDisplayName(uri: Uri): String {
        resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->

            val nameIndex = cursor.getColumnIndex(
                OpenableColumns.DISPLAY_NAME
            )

            if (cursor.moveToFirst() && nameIndex >= 0) {
                return cursor.getString(nameIndex)
            }
        }

        return uri.lastPathSegment ?: "Untitled.md"
    }

    fun getMimeType(uri: Uri): String {
        return resolver.getType(uri) ?: "text/markdown"
    }
}
