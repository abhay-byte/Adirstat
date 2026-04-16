package com.ivarna.adirstat.presentation.treemap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.ivarna.adirstat.domain.model.FileNode
import com.ivarna.adirstat.util.FileSizeFormatter
import com.ivarna.adirstat.util.FileTypeColorMapper
import com.ivarna.adirstat.util.Rect
import com.ivarna.adirstat.util.TreemapLayoutEngine
import com.ivarna.adirstat.util.TreemapRect

@Composable
fun TreemapView(
    nodes: List<FileNode>,
    zoomScale: Float = 1f,
    zoomOffset: Offset = Offset.Zero,
    onItemClick: (FileNode) -> Unit,
    onItemLongClick: (FileNode) -> Unit,
    onTransformGesture: (centroid: Offset, pan: Offset, zoom: Float) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val baseNodes = remember(nodes) { nodes.sortedByDescending { it.sizeBytes } }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(baseNodes) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    onTransformGesture(centroid, pan, zoom)
                }
            }
            .pointerInput(baseNodes, zoomScale, zoomOffset) {
                detectTapGestures(
                    onTap = { offset ->
                        val localTap = (offset - zoomOffset) / zoomScale
                        val displayNodes = groupSmallNodes(baseNodes, size.width.toFloat(), size.height.toFloat(), density.density)
                        val rects = layoutRects(displayNodes, size.width.toFloat(), size.height.toFloat())
                        val tappedRect = rects.find { rect ->
                            localTap.x >= rect.x && localTap.x <= rect.x + rect.width &&
                            localTap.y >= rect.y && localTap.y <= rect.y + rect.height
                        }
                        tappedRect?.let { onItemClick(it.node) }
                    },
                    onLongPress = { offset ->
                        val localTap = (offset - zoomOffset) / zoomScale
                        val displayNodes = groupSmallNodes(baseNodes, size.width.toFloat(), size.height.toFloat(), density.density)
                        val rects = layoutRects(displayNodes, size.width.toFloat(), size.height.toFloat())
                        val tappedRect = rects.find { rect ->
                            localTap.x >= rect.x && localTap.x <= rect.x + rect.width &&
                            localTap.y >= rect.y && localTap.y <= rect.y + rect.height
                        }
                        tappedRect?.let { onItemLongClick(it.node) }
                    }
                )
            }
    ) {
        val displayNodes = groupSmallNodes(baseNodes, size.width, size.height, density.density)
        val rects = layoutRects(displayNodes, size.width, size.height)
        val currentLevelTotalBytes = displayNodes.sumOf { it.sizeBytes }

        withTransform({
            translate(zoomOffset.x, zoomOffset.y)
            scale(zoomScale, zoomScale, pivot = Offset.Zero)
        }) {
            rects.forEach { rect ->
                if (rect.width > 0 && rect.height > 0) {
                    drawTreemapBlock(
                        rect = rect,
                        density = density.density,
                        parentSizeBytes = currentLevelTotalBytes
                    )
                }
            }
        }
    }
}

private const val MIN_BLOCK_WIDTH_DP = 48f
private const val MIN_BLOCK_HEIGHT_DP = 32f

private fun groupSmallNodes(
    nodes: List<FileNode>,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    density: Float
): List<FileNode> {
    if (nodes.isEmpty() || canvasWidthPx <= 0f || canvasHeightPx <= 0f) return nodes

    val minW = MIN_BLOCK_WIDTH_DP * density
    val minH = MIN_BLOCK_HEIGHT_DP * density
    val minArea = minW * minH
    val totalArea = canvasWidthPx * canvasHeightPx
    val totalBytes = nodes.sumOf { it.sizeBytes }.toFloat().coerceAtLeast(1f)

    val visible = mutableListOf<FileNode>()
    val tooSmall = mutableListOf<FileNode>()

    nodes.forEach { node ->
        val nodeArea = totalArea * (node.sizeBytes.toFloat() / totalBytes)
        if (nodeArea >= minArea) {
            visible.add(node)
        } else {
            tooSmall.add(node)
        }
    }

    if (tooSmall.isEmpty()) return visible

    val othersBytes = tooSmall.sumOf { it.sizeBytes }
    val othersNode = FileNode.Directory(
        name = "Others (${tooSmall.size})",
        path = "virtual://others/layout/${tooSmall.hashCode()}",
        children = tooSmall,
        size = othersBytes,
        lastModified = 0L,
        isVirtual = true,
        virtualLabel = "Others (${tooSmall.size} items)"
    )

    return (visible + othersNode).sortedByDescending { it.sizeBytes }
}

private fun layoutRects(nodes: List<FileNode>, width: Float, height: Float): List<TreemapRect> {
    return TreemapLayoutEngine.squarify(
        nodes = nodes,
        bounds = Rect(0f, 0f, width, height)
    ).map { (node, rect) ->
        TreemapRect(
            node = node,
            x = rect.left,
            y = rect.top,
            width = rect.width,
            height = rect.height
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTreemapBlock(
    rect: TreemapRect,
    density: Float,
    parentSizeBytes: Long
) {
    val node = rect.node
    val blockColor = FileTypeColorMapper.getColorForNode(node)
    val blockWidth = rect.width
    val blockHeight = rect.height
    val cornerRadius = 12f * density

    // Draw base block with rounded corners
    drawRoundRect(
        color = blockColor,
        topLeft = Offset(rect.x, rect.y),
        size = Size(blockWidth, blockHeight),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // Draw subtle gradient overlay (The Precision Mosaic texture)
    drawRoundRect(
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.1f), Color.Transparent),
            start = Offset(rect.x, rect.y),
            end = Offset(rect.x + blockWidth, rect.y + blockHeight)
        ),
        topLeft = Offset(rect.x, rect.y),
        size = Size(blockWidth, blockHeight),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // Special treatment for Apps (Stripe pattern)
    if (node.isAppNode) {
        val stripeColor = Color.Black.copy(alpha = 0.1f)
        var x = rect.x - rect.height
        while (x < rect.x + rect.width + rect.height) {
            drawLine(
                color = stripeColor,
                start = Offset(x, rect.y + rect.height),
                end = Offset(x + rect.height, rect.y),
                strokeWidth = 2f * density
            )
            x += 16f * density
        }
    }

    drawNodeLabel(rect, node, density, parentSizeBytes)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNodeLabel(
    rect: TreemapRect,
    node: FileNode,
    density: Float,
    parentSizeBytes: Long
) {
    val blockWidth = rect.width
    val blockHeight = rect.height
    if (blockWidth < 32f || blockHeight < 20f) return

    val nameFontSizeSp = when {
        blockWidth > 220f && blockHeight > 120f -> 18f
        blockWidth > 140f && blockHeight > 72f -> 14f
        blockWidth > 80f && blockHeight > 44f -> 12f
        blockWidth > 42f && blockHeight > 24f -> 10f
        else -> 8f
    }
    val detailFontSizeSp = nameFontSizeSp * 0.85f

    val padding = 8f * density
    val maxTextWidth = blockWidth - padding * 2

    fun paint(sizeSp: Float, alpha: Int = 255, bold: Boolean = false) = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        this.alpha = alpha
        textSize = sizeSp * density
        // Use custom typeface if possible, otherwise bold default
        typeface = android.graphics.Typeface.create("Space Grotesk", if (bold) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        isAntiAlias = true
    }

    fun fitPaintForText(text: String, startSp: Float, bold: Boolean = false, alpha: Int = 255): android.graphics.Paint? {
        var sizeSp = startSp
        while (sizeSp >= 6f) {
            val candidate = paint(sizeSp, alpha = alpha, bold = bold)
            if (candidate.measureText(text) <= maxTextWidth) {
                return candidate
            }
            sizeSp -= 0.5f
        }
        return null
    }

    fun wrapText(text: String, textPaint: android.graphics.Paint, maxLines: Int): List<String>? {
        if (text.isBlank()) return emptyList()
        if (textPaint.measureText(text) <= maxTextWidth) return listOf(text)

        val tokens = text.split(' ', '_', '-').filter { it.isNotBlank() }
        val lines = mutableListOf<String>()

        if (tokens.size > 1) {
            var current = tokens.first()
            for (token in tokens.drop(1)) {
                val candidate = "$current $token"
                if (textPaint.measureText(candidate) <= maxTextWidth) {
                    current = candidate
                } else {
                    lines.add(current)
                    current = token
                }
            }
            lines.add(current)
            if (lines.size <= maxLines) {
                return lines
            }
        }
        return null
    }

    fun fitWrappedText(text: String, startSp: Float, maxLines: Int, bold: Boolean = false): Pair<android.graphics.Paint, List<String>>? {
        var sizeSp = startSp
        while (sizeSp >= 6f) {
            val candidatePaint = paint(sizeSp, bold = bold)
            val lines = wrapText(text, candidatePaint, maxLines)
            if (lines != null) {
                return candidatePaint to lines
            }
            sizeSp -= 0.5f
        }
        return null
    }

    val canvas = drawContext.canvas.nativeCanvas
    val textX = rect.x + padding
    var y = rect.y + padding

    val baseName = node.virtualLabel ?: node.name
    val wrappedName = fitWrappedText(baseName, nameFontSizeSp, maxLines = 2, bold = true)
    var drewName = false
    if (wrappedName != null) {
        val (namePaint, lines) = wrappedName
        val lineHeight = namePaint.textSize * 1.1f
        lines.forEach { line ->
            y += namePaint.textSize
            canvas.drawText(line, textX, y, namePaint)
            y += (lineHeight - namePaint.textSize)
        }
        y += 2f * density
        drewName = true
    }

    val sizeText = FileSizeFormatter.format(node.sizeBytes)
    val sizePaint = fitPaintForText(sizeText, detailFontSizeSp, alpha = 230, bold = true)
    if (drewName && sizePaint != null && blockHeight >= y - rect.y + sizePaint.textSize + padding) {
        y += sizePaint.textSize
        canvas.drawText(sizeText, textX, y, sizePaint)
    }
}
