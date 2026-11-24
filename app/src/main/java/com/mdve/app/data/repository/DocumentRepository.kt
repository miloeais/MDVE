
package com.mdve.app.data.repository

import android.content.Context
import android.net.Uri
import com.mdve.app.data.model.DocumentItem
import org.json.JSONArray
import org.json.JSONObject

class DocumentRepository(context: Context) {

    private val preferences = context.getSharedPreferences(
        "mdve_documents",
        Context.MODE_PRIVATE
    )

    fun getDocuments(): List<DocumentItem> {
        val json = preferences.getString("documents", "[]")
            ?: "[]"

        return try {
            val array = JSONArray(json)

            List(array.length()) { index ->
                val item = array.getJSONObject(index)

                DocumentItem(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    uri = item.getString("uri"),
                    mimeType = item.optString(
                        "mimeType",
                        "text/markdown"
                    ),
                    lastModified = item.optLong(
                        "lastModified",
                        System.currentTimeMillis()
                    ),
                    isFavorite = item.optBoolean(
                        "isFavorite",
                        false
                    )
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addDocument(document: DocumentItem) {
        val documents = getDocuments().toMutableList()

        documents.removeAll {
            it.uri == document.uri
        }

        documents.add(0, document)
        saveDocuments(documents)
    }

    fun removeDocument(id: String) {
        val documents = getDocuments()
            .filterNot { it.id == id }

        saveDocuments(documents)
    }

    fun updateDocument(document: DocumentItem) {
        val documents = getDocuments().map {
            if (it.id == document.id) document else it
        }

        saveDocuments(documents)
    }

    fun findDocument(id: String): DocumentItem? {
        return getDocuments().find {
            it.id == id
        }
    }

    private fun saveDocuments(
        documents: List<DocumentItem>
    ) {
        val array = JSONArray()

        documents.forEach { document ->
            val item = JSONObject().apply {
                put("id", document.id)
                put("name", document.name)
                put("uri", document.uri)
                put("mimeType", document.mimeType)
                put("lastModified", document.lastModified)
                put("isFavorite", document.isFavorite)
            }

            array.put(item)
        }

        preferences.edit()
            .putString("documents", array.toString())
            .apply()
    }
}
