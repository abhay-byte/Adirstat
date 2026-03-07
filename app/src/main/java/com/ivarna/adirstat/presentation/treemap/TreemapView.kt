package com.ivarna.adirstat.presentation.treemap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.ivarna.adirstat.domain.model.FileCategory
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.presentation.theme.TreemapColors
import com.ivarna.adirstat.util.Rect
import com.ivarna.adirstat.util.TreemapLayoutEngine
import com.ivarna.adirstat.util.TreemapRect
import kotlin.math.max
import kotlin.math.min

/**
 * Composable for rendering an interactive treemap visualization
 */
@Composable
fun TreemapView(
    fileNode: FileNode.Directory,
    modifier: Modifier = Modifier,
    padding: Float = 2f,
    minFileSize: Long = 0,
    onItemClick: (FileNode) -> Unit = {},
    onItemLongClick: (FileNode) -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    
    // Calculate layout
    val rects = remember(fileNode, scale, offsetX, offsetY, padding, minFileSize) {
        calculateScaledLayout(fileNode, scale, offsetX, offsetY, padding, minFileSize)
    }
    
    // Handle gestures
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(rects) {
                    detectTapGestures(
                        onTap = { offset ->
                            val tappedRect = findRectAtOffset(rects, offset)
                            tappedRect?.let { onItemClick(it.node) }
                        },
                        onLongPress = { offset ->
                            val tappedRect = findRectAtOffset(rects, offset)
                            tappedRect?.let { onItemLongClick(it.node) }
                        }
                    )
                }
        ) {
            // Draw all rectangles
            for (treemapRect in rects) {
                drawTreemapRect(treemapRect, padding)
            }
        }
    }
}

/**
 * Calculate scaled layout based on zoom and pan
 */
private fun calculateScaledLayout(
    rootNode: FileNode.Directory,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    padding: Float,
    minFileSize: Long
): List<TreemapRect> {
    // Base layout at scale 1
    val baseLayout = TreemapLayoutEngine.calculateLayout(
        items = rootNode.children,
        bounds = Rect(0f, 0f, 1080f, 1920f), // Base canvas size
        minSize = minFileSize
    )
    
    // Apply scale and offset
    return baseLayout.map { rect ->
        rect.copy(
            x = rect.x * scale + offsetX,
            y = rect.y * scale + offsetY,
            width = rect.width * scale,
            height = rect.height * scale
        )
    }
}

/**
 * Draw a single treemap rectangle
 */
private fun DrawScope.drawTreemapRect(
    rect: TreemapRect,
    padding: Float
) {
    val color = getColorForNode(rect.node)
    val adjustedRect = rect.copy(
        x = rect.x + padding / 2,
        y = rect.y + padding / 2,
        width = (rect.width - padding).coerceAtLeast(0f),
        height = (rect.height - padding).coerceAtLeast(0f)
    )
    
    // Only draw if visible and large enough
    if (adjustedRect.width < 2 || adjustedRect.height < 2) return
    
    // Draw filled rectangle
    drawRect(
        color = color,
        topLeft = Offset(adjustedRect.x, adjustedRect.y),
        size = Size(adjustedRect.width, adjustedRect.height)
    )
    
    // Draw border
    drawRect(
        color = Color.Black.copy(alpha = 0.3f),
        topLeft = Offset(adjustedRect.x, adjustedRect.y),
        size = Size(adjustedRect.width, adjustedRect.height),
        style = Stroke(width = 1f)
    )
}

/**
 * Get color for a file node based on its category
 */
private fun getColorForNode(node: FileNode): Color {
    return when (node) {
        is FileNode.File -> {
            when (node.getCategory()) {
                FileCategory.IMAGES -> TreemapColors.images
                FileCategory.VIDEOS -> TreemapColors.videos
                FileCategory.AUDIO -> TreemapColors.audio
                FileCategory.DOCUMENTS -> TreemapColors.documents
                FileCategory.ARCHIVES -> TreemapColors.archives
                FileCategory.APK -> TreemapColors.apk
                FileCategory.CODE -> TreemapColors.code
                FileCategory.OTHER -> TreemapColors.other
            }
        }
        is FileNode.Directory -> {
            // Calculate average color from children or use a default
            TreemapColors.other
        }
    }
}

/**
 * Find the rectangle at a given screen offset
 */
private fun findRectAtOffset(
    rects: List<TreemapRect>,
    offset: Offset
): TreemapRect? {
    // Search in reverse order (largest on top in z-order)
    return rects.reversed().find { rect ->
        offset.x >= rect.x && offset.x <= rect.x + rect.width &&
        offset.y >= rect.y && offset.y <= rect.y + rect.height
    }
}

/**
 * Simple treemap view without gestures for preview/testing
 */
@Composable
fun SimpleTreemapView(
    fileNode: FileNode.Directory,
    modifier: Modifier = Modifier,
    padding: Float = 2f,
    minFileSize: Long = 0,
    onItemClick: (FileNode) -> Unit = {}
) {
    val rects = remember(fileNode, padding, minFileSize) {
        TreemapLayoutEngine.calculateLayout(
            items = fileNode.children,
            bounds = Rect(0f, 0f, 1080f, 1920f),
            minSize = minFileSize
        )
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .pointerInput(rects) {
                detectTapGestures { offset ->
                    val tappedRect = findRectAtOffset(rects, offset)
                    tappedRect?.let { onItemClick(it.node) }
                }
            }
    ) {
        for (treemapRect in rects) {
            drawTreemapRect(treemapRect, padding)
        }
    }
}
