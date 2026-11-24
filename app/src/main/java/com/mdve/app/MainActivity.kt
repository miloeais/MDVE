
package com.mdve.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.mdve.app.data.storage.DocumentImporter
import com.mdve.app.ui.main.MainScreen
import com.mdve.app.ui.theme.MDVETheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MDVETheme {

                val context = LocalContext.current

                val importer = remember {
                    DocumentImporter(context)
                }

                val importLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->

                        if (result.resultCode == RESULT_OK) {
                            val data = result.data
                            val uri: Uri? = data?.data

                            if (uri != null) {
                                val flags = data.flags and (
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    )

                                runCatching {
                                    importer.importDocument(
                                        uri = uri,
                                        permissionFlags = flags
                                    )
                                }
                            }
                        }
                    }

                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
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
                        }
                    )
                }
            }
        }
    }
}
