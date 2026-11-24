
package com.mdve.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.mdve.app.data.model.DocumentItem
import com.mdve.app.data.repository.DocumentRepository
import com.mdve.app.data.storage.DocumentImporter
import com.mdve.app.data.storage.DocumentStorage
import com.mdve.app.ui.editor.EditorScreen
import com.mdve.app.ui.main.MainScreen
import com.mdve.app.ui.theme.MDVETheme
import com.mdve.app.ui.viewer.ViewerScreen
import java.util.UUID

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val incomingIntent = intent

        setContent {
            MDVETheme {

                val context = LocalContext.current

                val importer = remember {
                    DocumentImporter(context)
                }

                val repository = remember {
                    DocumentRepository(context)
                }

                val storage = remember {
                    DocumentStorage(context)
                }

                val documents = remember {
                    mutableStateListOf<DocumentItem>()
                }

                var selectedDocument by remember {
                    mutableStateOf<DocumentItem?>(null)
                }

                var viewerContent by remember {
                    mutableStateOf("")
                }

                var editorContent by remember {
                    mutableStateOf("")
                }

                var isEditing by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(Unit) {
                    documents.clear()
                    documents.addAll(repository.getDocuments())

                    if (
                        incomingIntent.action == Intent.ACTION_VIEW &&
                        incomingIntent.data != null
                    ) {
                        val uri = incomingIntent.data!!

                        val flags = incomingIntent.flags and (
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            )

                        storage.rememberPermission(
                            uri,
                            flags
                        )

                        val document = runCatching {
                            val existing = repository.getDocuments()
                                .find { it.uri == uri.toString() }

                            val newDocument = DocumentItem(
                                id = existing?.id
                                    ?: UUID.randomUUID().toString(),
                                name = storage.getDisplayName(uri),
                                uri = uri.toString(),
                                mimeType = storage.getMimeType(uri),
                                lastModified = System.currentTimeMillis(),
                                isFavorite = existing?.isFavorite ?: false
                            )

                            repository.addDocument(newDocument)

                            newDocument
                        }.getOrNull()

                        if (document != null) {
                            documents.clear()
                            documents.addAll(repository.getDocuments())

                            viewerContent = runCatching {
                                storage.readText(uri)
                            }.getOrElse {
                                "Unable to read this document."
                            }

                            selectedDocument = document
                        }
                    }
                }

                val importLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->

                        if (result.resultCode == RESULT_OK) {
                            val data = result.data
                            val uri: Uri? = data?.data

                            if (uri != null && data != null) {
                                val flags = data.flags and (
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    )

                                runCatching {
                                    importer.importDocument(
                                        uri = uri,
                                        permissionFlags = flags
                                    )
                                }.onSuccess {
                                    documents.clear()
                                    documents.addAll(
                                        repository.getDocuments()
                                    )
                                }
                            }
                        }
                    }

                val saveAsLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->

                        if (result.resultCode == RESULT_OK) {
                            val uri = result.data?.data

                            if (uri != null) {
                                runCatching {
                                    storage.writeText(
                                        uri,
                                        editorContent
                                    )

                                    val newDocument = DocumentItem(
                                        id = UUID.randomUUID().toString(),
                                        name = storage.getDisplayName(uri),
                                        uri = uri.toString(),
                                        mimeType = storage.getMimeType(uri),
                                        lastModified = System.currentTimeMillis()
                                    )

                                    repository.addDocument(newDocument)

                                    newDocument
                                }.onSuccess { newDocument ->
                                    documents.clear()
                                    documents.addAll(
                                        repository.getDocuments()
                                    )

                                    selectedDocument = newDocument
                                    viewerContent = editorContent
                                    isEditing = false
                                }
                            }
                        }
                    }

                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    val currentDocument = selectedDocument

                    when {
                        currentDocument == null -> {
                            MainScreen(
                                documents = documents,
                                onImport = {
                                    val intent = Intent(
                                        Intent.ACTION_OPEN_DOCUMENT
                                    ).apply {
                                        addCategory(
                                            Intent.CATEGORY_OPENABLE
                                        )

                                        type = "*/*"

                                        putExtra(
                                            Intent.EXTRA_MIME_TYPES,
                                            arrayOf(
                                                "text/markdown",
                                                "text/x-markdown",
                                                "text/plain",
                                                "application/octet-stream"
                                            )
                                        )

                                        addFlags(
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                        )
                                    }

                                    importLauncher.launch(intent)
                                },
                                onDocumentClick = { document ->
                                    viewerContent = runCatching {
                                        storage.readText(
                                            Uri.parse(document.uri)
                                        )
                                    }.getOrElse {
                                        "Unable to read this document."
                                    }

                                    selectedDocument = document
                                },
                                onDocumentDelete = { document ->
                                    repository.removeDocument(document.id)

                                    documents.removeAll {
                                        it.id == document.id
                                    }
                                }
                            )
                        }

                        isEditing -> {
                            EditorScreen(
                                documentName = currentDocument.name,
                                content = editorContent,
                                onContentChange = {
                                    editorContent = it
                                },
                                onBack = {
                                    isEditing = false
                                },
                                onSave = {
                                    runCatching {
                                        storage.writeText(
                                            Uri.parse(currentDocument.uri),
                                            editorContent
                                        )

                                        val updatedDocument =
                                            currentDocument.copy(
                                                lastModified = System.currentTimeMillis()
                                            )

                                        repository.updateDocument(
                                            updatedDocument
                                        )

                                        updatedDocument
                                    }.onSuccess { updatedDocument ->
                                        documents.clear()
                                        documents.addAll(
                                            repository.getDocuments()
                                        )

                                        selectedDocument = updatedDocument
                                        viewerContent = editorContent
                                        isEditing = false
                                    }
                                },
                                onSaveAs = {
                                    val intent = Intent(
                                        Intent.ACTION_CREATE_DOCUMENT
                                    ).apply {
                                        addCategory(
                                            Intent.CATEGORY_OPENABLE
                                        )

                                        type = "text/markdown"

                                        putExtra(
                                            Intent.EXTRA_TITLE,
                                            currentDocument.name
                                        )

                                        addFlags(
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                        )
                                    }

                                    saveAsLauncher.launch(intent)
                                }
                            )
                        }

                        else -> {
                            ViewerScreen(
                                document = currentDocument,
                                content = viewerContent,
                                onBack = {
                                    selectedDocument = null
                                    viewerContent = ""
                                },
                                onEdit = {
                                    editorContent = viewerContent
                                    isEditing = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
