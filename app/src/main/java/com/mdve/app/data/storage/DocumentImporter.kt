
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

    fun importDocument(uri: Uri): DocumentItem {

        storage.rememberPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        val name = storage.getDisplayName(uri)
        val mimeType = storage.getMimeType(uri)

        val document = DocumentItem(
            id = UUID.randomUUID().toString(),
            name = name,
            uri = uri.toString(),
            mimeType = mimeType,
            lastModified = System.currentTimeMillis(),
            isFavorite = false
        )

        repository.addDocument(document)

        return document
    }
}
