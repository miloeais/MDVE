
package com.mdve.app.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdve.app.data.model.DocumentItem
import com.mdve.app.ui.theme.AppBackground
import com.mdve.app.ui.theme.OctoRed
import com.mdve.app.ui.theme.SurfaceWhite
import com.mdve.app.ui.theme.TextPrimary
import com.mdve.app.ui.theme.TextSecondary

@Composable
fun ViewerScreen(
    document: DocumentItem,
    content: String,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Surface(
            color = SurfaceWhite,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onBack) {
                        Text("‹  Back")
                    }

                    Button(onClick = onEdit) {
                        Text("Edit")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = document.name,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Markdown Viewer",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceWhite
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    if (content.isBlank()) {
                        Text(
                            text = "This document is empty.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    } else {
                        var inCodeBlock = false

                        content.lines().forEach { line ->

                            if (line.trimStart().startsWith("```")) {
                                inCodeBlock = !inCodeBlock
                            } else if (inCodeBlock) {
                                Text(
                                    text = line.ifEmpty { " " },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color(0xFFF1F1F1),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        )
                                )
                            } else {
                                MarkdownLine(line)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MarkdownLine(line: String) {
    val trimmed = line.trimStart()

    when {
        line.isBlank() -> {
            Spacer(modifier = Modifier.height(9.dp))
        }

        trimmed.matches(Regex("^#{1,6}\\s+.*")) -> {
            val level = trimmed.takeWhile { it == '#' }.length
            val heading = trimmed.drop(level).trim()

            Text(
                text = parseInlineMarkdown(heading),
                fontSize = when (level) {
                    1 -> 26.sp
                    2 -> 22.sp
                    3 -> 19.sp
                    4 -> 17.sp
                    else -> 15.sp
                },
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(
                    top = 10.dp,
                    bottom = 6.dp
                )
            )
        }

        trimmed.startsWith(">") -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Spacer(
                    modifier = Modifier
                        .height(28.dp)
                        .fillMaxWidth(0.015f)
                        .background(OctoRed)
                )

                Text(
                    text = parseInlineMarkdown(
                        trimmed.removePrefix(">").trim()
                    ),
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }

        trimmed.matches(Regex("^([-*_])\\1{2,}$")) -> {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color(0xFFE0E0E0)
            )
        }

        trimmed.startsWith("- ") ||
            trimmed.startsWith("* ") ||
            trimmed.startsWith("+ ") -> {
            Text(
                text = "•  " + parseInlineMarkdown(trimmed.drop(2)),
                fontSize = 15.sp,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }

        trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
            Text(
                text = parseInlineMarkdown(trimmed),
                fontSize = 15.sp,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }

        else -> {
            Text(
                text = parseInlineMarkdown(line),
                fontSize = 15.sp,
                color = TextPrimary,
                lineHeight = 23.sp
            )
        }
    }
}

private fun parseInlineMarkdown(
    source: String
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val pattern = Regex(
        """(\*\*[^*]+\*\*|__[^_]+__|\*[^*]+\*|_[^_]+_|`[^`]+`)"""
    )

    var currentIndex = 0

    pattern.findAll(source).forEach { match ->
        builder.append(
            source.substring(currentIndex, match.range.first)
        )

        val token = match.value

        when {
            token.startsWith("**") || token.startsWith("__") -> {
                builder.pushStyle(
                    SpanStyle(fontWeight = FontWeight.Bold)
                )
                builder.append(token.substring(2, token.length - 2))
                builder.pop()
            }

            token.startsWith("`") -> {
                builder.pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFFF0F0F0)
                    )
                )
                builder.append(token.substring(1, token.length - 1))
                builder.pop()
            }

            else -> {
                builder.pushStyle(
                    SpanStyle(fontWeight = FontWeight.Medium)
                )
                builder.append(token.substring(1, token.length - 1))
                builder.pop()
            }
        }

        currentIndex = match.range.last + 1
    }

    builder.append(source.substring(currentIndex))

    return builder.toAnnotatedString()
}
