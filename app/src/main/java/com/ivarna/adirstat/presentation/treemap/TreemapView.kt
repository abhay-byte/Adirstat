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
        virtualLabel = "🔒 Others (${tooSmall.size} apps)"
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

    if (node.isVirtual) {
        drawRect(
            color = blockColor.copy(alpha = 0.75f),
            topLeft = Offset(rect.x, rect.y),
            size = Size(blockWidth, blockHeight)
        )
        val stripeColor = Color.Black.copy(alpha = 0.12f)
        var x = rect.x - rect.height
        while (x < rect.x + rect.width + rect.height) {
            drawLine(
                color = stripeColor,
                start = Offset(x, rect.y + rect.height),
                end = Offset(x + rect.height, rect.y),
                strokeWidth = 1.5f
            )
            x += 12f
        }
    } else {
        drawRoundRect(
            color = blockColor,
            topLeft = Offset(rect.x, rect.y),
            size = Size(blockWidth, blockHeight),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }

    drawRect(
        color = Color.Black.copy(alpha = 0.2f),
        topLeft = Offset(rect.x, rect.y),
        size = Size(blockWidth, blockHeight),
        style = Stroke(width = 1.dp.toPx())
    )

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
    if (blockWidth < 32f || blockHeight < 18f) return

    val nameFontSizeSp = when {
        blockWidth > 200f && blockHeight > 120f -> 13f
        blockWidth > 120f && blockHeight > 70f -> 11f
        blockWidth > 70f && blockHeight > 45f -> 10f
        blockWidth > 45f && blockHeight > 30f -> 8.5f
        else -> 7.5f
    }
    val detailFontSizeSp = nameFontSizeSp * 0.80f

    val padding = 4f * density
    val maxTextWidth = blockWidth - padding * 2

    fun paint(sizeSp: Float, alpha: Int = 255, bold: Boolean = false) = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        this.alpha = alpha
        textSize = sizeSp * density
        typeface = if (bold) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
        setShadowLayer(3f, 1f, 1f, android.graphics.Color.BLACK)
        isAntiAlias = true
    }

    fun clip(text: String, textPaint: android.graphics.Paint): String {
        var clipped = text
        while (textPaint.measureText(clipped) > maxTextWidth && clipped.length > 3) {
            clipped = clipped.dropLast(1)
        }
        return if (clipped.length < text.length) "$clipped…" else clipped
    }

    val namePaint = paint(nameFontSizeSp, bold = true)
    val detailPaint = paint(detailFontSizeSp, alpha = 205)
    val canvas = drawContext.canvas.nativeCanvas
    val textX = rect.x + padding
    var y = rect.y + namePaint.textSize + padding

    val baseName = node.virtualLabel ?: node.name
    val displayName = if (node.isVirtual && !baseName.startsWith("🔒")) "🔒 $baseName" else baseName
    if (blockHeight >= namePaint.textSize + padding * 2) {
        canvas.drawText(clip(displayName, namePaint), textX, y, namePaint)
        y += namePaint.textSize + 2f * density
    }

    if (blockHeight >= y - rect.y + detailPaint.textSize + padding) {
        val sizeText = FileSizeFormatter.format(node.sizeBytes)
        canvas.drawText(clip(sizeText, detailPaint), textX, y, detailPaint)
        y += detailPaint.textSize + 1f * density
    }

    if (blockHeight >= y - rect.y + detailPaint.textSize + padding && parentSizeBytes > 0L) {
        val pct = node.sizeBytes * 100f / parentSizeBytes.toFloat()
        val pctText = if (pct >= 1f) "${pct.toInt()}%" else "<1%"
        canvas.drawText(clip(pctText, detailPaint), textX, y, detailPaint)
    }
}
