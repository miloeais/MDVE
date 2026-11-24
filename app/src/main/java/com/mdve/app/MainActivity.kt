
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
import com.mdve.app.ui.main.MainScreen
import com.mdve.app.ui.theme.MDVETheme
import com.mdve.app.ui.viewer.ViewerScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                LaunchedEffect(Unit) {
                    documents.clear()
                    documents.addAll(
                        repository.getDocuments()
                    )
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

                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    val currentDocument = selectedDocument

                    if (currentDocument == null) {
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
                            }
                        )
                    } else {
                        ViewerScreen(
                            document = currentDocument,
                            content = viewerContent,
                            onBack = {
                                selectedDocument = null
                                viewerContent = ""
                            },
                            onEdit = {
                                // Editor navigation will be added later.
                            }
                        )
                    }
                }
            }
        }
    }
}
