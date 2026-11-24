
package com.mdve.app.data.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mdve.app.data.model.DocumentItem
import com.mdve.app.data.repository.DocumentRepository
import java.util.UUID

class DocumentImporter(context: Context) {

    private val appContext = context.applicationContext
    private val storage = DocumentStorage(appContext)
    private val repository = DocumentRepository(appContext)

    fun importDocument(
        uri: Uri,
        permissionFlags: Int
    ): DocumentItem {

        val fileName = storage.getDisplayName(uri)
        val extension = fileName.substringAfterLast(
            ".",
            ""
        ).lowercase()

        require(extension == "md" || extension == "markdown") {
            "Only Markdown files are supported."
        }

        storage.rememberPermission(
            uri,
            permissionFlags
        )

        val existing = repository.getDocuments()
            .find { it.uri == uri.toString() }

        val document = DocumentItem(
            id = existing?.id ?: UUID.randomUUID().toString(),
            name = fileName,
            uri = uri.toString(),
            mimeType = storage.getMimeType(uri),
            lastModified = System.currentTimeMillis(),
            isFavorite = existing?.isFavorite ?: false
        )

        repository.addDocument(document)

        return document
    }
}
