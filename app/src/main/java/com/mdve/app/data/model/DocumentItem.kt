
package com.mdve.app.data.model

data class DocumentItem(
    val id: String,
    val name: String,
    val uri: String,
    val mimeType: String = "text/markdown",
    val lastModified: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
