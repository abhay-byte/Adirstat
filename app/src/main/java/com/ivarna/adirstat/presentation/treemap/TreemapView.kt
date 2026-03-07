package com.ivarna.adirstat.presentation.treemap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.util.FileSizeFormatter
import com.ivarna.adirstat.util.FileTypeColorMapper
import com.ivarna.adirstat.util.TreemapRect

@Composable
fun TreemapView(
    fileNode: FileNode.Directory,
    rects: List<TreemapRect>,
    onItemClick: (FileNode) -> Unit,
    onItemLongClick: (FileNode) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(rects) {
                detectTapGestures(
                    onTap = { offset ->
                        val tappedRect = rects.find { rect ->
                            offset.x >= rect.x && offset.x <= rect.x + rect.width &&
                            offset.y >= rect.y && offset.y <= rect.y + rect.height
                        }
                        tappedRect?.let { onItemClick(it.node) }
                    },
                    onLongPress = { offset ->
                        val tappedRect = rects.find { rect ->
                            offset.x >= rect.x && offset.x <= rect.x + rect.width &&
                            offset.y >= rect.y && offset.y <= rect.y + rect.height
                        }
                        tappedRect?.let { onItemLongClick(it.node) }
                    }
                )
            }
    ) {
        rects.forEach { rect ->
            if (rect.width > 0 && rect.height > 0) {
                drawTreemapBlock(rect = rect)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTreemapBlock(
    rect: TreemapRect
) {
    val node = rect.node
    val blockColor = FileTypeColorMapper.getColorForNode(node)
    val blockWidth = rect.width
    val blockHeight = rect.height
    
    // Draw filled rectangle
    drawRoundRect(
        color = blockColor,
        topLeft = Offset(rect.x, rect.y),
        size = Size(blockWidth, blockHeight),
        cornerRadius = CornerRadius(4f, 4f)
    )
    
    // Draw border separator
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.25f),
        topLeft = Offset(rect.x, rect.y),
        size = Size(blockWidth, blockHeight),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.5f)
    )
    
    // Draw labels if block is large enough
    val minWidthForName = 60f
    val minHeightForName = 40f
    val minHeightForSize = 60f
    
    if (blockWidth > minWidthForName && blockHeight > minHeightForName) {
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
            isAntiAlias = true
        }
        
        val canvas = drawContext.canvas.nativeCanvas
        
        // Truncate name
        val maxNameLength = ((blockWidth - 12f) / textPaint.textSize).toInt().coerceIn(5, 30)
        val displayName = if (node.name.length > maxNameLength) {
            node.name.take(maxNameLength - 1) + "…"
        } else {
            node.name
        }
        
        canvas.drawText(displayName, rect.x + 6f, rect.y + 16f, textPaint)
        
        // Draw size if tall enough
        if (blockHeight > minHeightForSize) {
            val sizeText = FileSizeFormatter.format(node.size)
            canvas.drawText(sizeText, rect.x + 6f, rect.y + 32f, textPaint)
        }
    }
}
