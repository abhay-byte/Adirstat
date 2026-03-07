package com.ivarna.adirstat.presentation.treemap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.util.FileSizeFormatter
import com.ivarna.adirstat.util.FileTypeColorMapper
import com.ivarna.adirstat.util.TreemapRect
import kotlin.math.max

@Composable
fun TreemapView(
    fileNode: FileNode.Directory,
    rects: List<TreemapRect>,
    zoomScale: Float = 1f,
    zoomOffset: Offset = Offset.Zero,
    onItemClick: (FileNode) -> Unit,
    onItemLongClick: (FileNode) -> Unit,
    onTransformGesture: (centroid: Offset, pan: Offset, zoom: Float) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(rects) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    onTransformGesture(centroid, pan, zoom)
                }
            }
            .pointerInput(rects) {
                detectTapGestures(
                    onTap = { offset ->
                        // Reverse transform the tap coordinates
                        val localTap = (offset - zoomOffset) / zoomScale
                        val tappedRect = rects.find { rect ->
                            localTap.x >= rect.x && localTap.x <= rect.x + rect.width &&
                            localTap.y >= rect.y && localTap.y <= rect.y + rect.height
                        }
                        tappedRect?.let { onItemClick(it.node) }
                    },
                    onLongPress = { offset ->
                        val localTap = (offset - zoomOffset) / zoomScale
                        val tappedRect = rects.find { rect ->
                            localTap.x >= rect.x && localTap.x <= rect.x + rect.width &&
                            localTap.y >= rect.y && localTap.y <= rect.y + rect.height
                        }
                        tappedRect?.let { onItemLongClick(it.node) }
                    }
                )
            }
    ) {
        withTransform({
            translate(zoomOffset.x, zoomOffset.y)
            scale(zoomScale, zoomScale, pivot = Offset.Zero)
        }) {
            rects.forEach { rect ->
                if (rect.width > 0 && rect.height > 0) {
                    drawTreemapBlock(rect = rect, density = density.density)
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTreemapBlock(
    rect: TreemapRect,
    density: Float
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
    
    // Draw labels if block is large enough - dynamic sizing
    drawNodeLabel(rect, node, density)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNodeLabel(
    rect: TreemapRect,
    node: FileNode,
    density: Float
) {
    val blockWidth = rect.width
    val blockHeight = rect.height
    
    // Skip label entirely if block is too small to be readable
    if (blockWidth < 48f || blockHeight < 32f) return

    // Font size scales with block size
    val nameFontSizeSp = when {
        blockWidth > 300f && blockHeight > 200f -> 14f
        blockWidth > 150f && blockHeight > 100f -> 12f
        blockWidth > 80f  && blockHeight > 60f  -> 10f
        else                           -> 9f
    }

    val namePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = nameFontSizeSp * density
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        setShadowLayer(3f, 1f, 1f, android.graphics.Color.BLACK)
        isAntiAlias = true
    }

    // Truncate name to fit within block width
    val maxTextWidth = blockWidth - 8f * density
    var displayName = node.name
    while (namePaint.measureText(displayName) > maxTextWidth && displayName.length > 3) {
        displayName = displayName.dropLast(1)
    }
    if (displayName.length < node.name.length) displayName += "…"

    // Y position: vertically centered
    val textX = rect.x + 4f * density
    val textY = if (blockHeight < 60f) {
        rect.y + blockHeight / 2f + namePaint.textSize / 3f
    } else {
        rect.y + namePaint.textSize + 6f * density
    }

    drawContext.canvas.nativeCanvas.drawText(displayName, textX, textY, namePaint)

    // Second line: size — only if block is tall enough
    if (blockHeight > 56f) {
        val sizePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = 210
            textSize = (nameFontSizeSp - 1f) * density
            setShadowLayer(3f, 1f, 1f, android.graphics.Color.BLACK)
            isAntiAlias = true
        }
        val sizeText = FileSizeFormatter.format(node.size)
        if (sizePaint.measureText(sizeText) <= maxTextWidth) {
            drawContext.canvas.nativeCanvas.drawText(
                sizeText,
                textX,
                textY + namePaint.textSize + 3f * density,
                sizePaint
            )
        }
    }
}
