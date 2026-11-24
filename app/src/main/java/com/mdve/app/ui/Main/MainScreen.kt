
package com.mdve.app.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdve.app.data.model.DocumentItem
import com.mdve.app.ui.theme.AppBackground
import com.mdve.app.ui.theme.BorderLight
import com.mdve.app.ui.theme.OctoRed
import com.mdve.app.ui.theme.SurfaceWhite
import com.mdve.app.ui.theme.TextMuted
import com.mdve.app.ui.theme.TextPrimary
import com.mdve.app.ui.theme.TextSecondary

@Composable
fun MainScreen(
    onImport: () -> Unit,
    documents: List<DocumentItem> = emptyList(),
    onDocumentClick: (DocumentItem) -> Unit = {}
) {
    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            FloatingNavigation(
                onFiles = {},
                onImport = onImport
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MDVE",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OctoRed
                    )

                    Text(
                        text = "Markdown Viewer & Editor",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = OctoRed.copy(alpha = 0.10f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "M",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = OctoRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Documents",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "${documents.size} documents",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "MD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OctoRed,
                    modifier = Modifier
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyDocumentState(onImport)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = documents,
                        key = { it.id }
                    ) { document ->
                        DocumentCard(
                            document = document,
                            onClick = {
                                onDocumentClick(document)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDocumentState(
    onImport: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 35.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = OctoRed.copy(alpha = 0.08f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "MD",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OctoRed
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "No documents yet",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Import a Markdown file to get started.",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = onImport,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "+  Import Markdown",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FloatingNavigation(
    onFiles: () -> Unit,
    onImport: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 32.dp,
                vertical = 14.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            shadowElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                BorderLight
            )
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationItem(
                    symbol = "▦",
                    label = "Files",
                    selected = true,
                    onClick = onFiles
                )

                NavigationItem(
                    symbol = "＋",
                    label = "Import",
                    selected = false,
                    onClick = onImport
                )
            }
        }
    }
}

@Composable
private fun NavigationItem(
    symbol: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.TextButton(
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = symbol,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) OctoRed else TextMuted
            )

            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                color = if (selected) OctoRed else TextSecondary
            )
        }
    }
}
