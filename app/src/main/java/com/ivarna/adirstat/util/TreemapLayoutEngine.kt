package com.ivarna.adirstat.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.ivarna.adirstat.domain.model.FileNode

/**
 * Squarified Treemap Layout Engine
 */
object TreemapLayoutEngine {

    data class LayoutRect(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    ) {
        val width: Float get() = right - left
        val height: Float get() = bottom - top
        val area: Float get() = width * height
        val topLeft: Offset get() = Offset(left, top)
        val size: Size get() = Size(width, height)
    }

    fun calculateLayout(
        items: List<FileNode>,
        bounds: Rect,
        minSize: Long = 0
    ): List<TreemapRect> {
        return squarify(
            nodes = items.filter { it.sizeBytes >= minSize },
            bounds = bounds
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

    fun squarify(nodes: List<FileNode>, bounds: Rect): Map<FileNode, LayoutRect> {
        if (nodes.isEmpty() || bounds.width <= 0f || bounds.height <= 0f) return emptyMap()

        val sorted = nodes
            .filter { it.sizeBytes > 0L }
            .sortedByDescending { it.sizeBytes }
        if (sorted.isEmpty()) return emptyMap()

        val result = linkedMapOf<FileNode, LayoutRect>()
        squarifyRecursive(
            nodes = sorted,
            bounds = LayoutRect(bounds.left, bounds.top, bounds.right, bounds.bottom),
            result = result
        )
        return result
    }

    private fun squarifyRecursive(
        nodes: List<FileNode>,
        bounds: LayoutRect,
        result: MutableMap<FileNode, LayoutRect>
    ) {
        if (nodes.isEmpty() || bounds.width <= 0f || bounds.height <= 0f) return
        if (nodes.size == 1) {
            result[nodes[0]] = bounds
            return
        }

        val totalBytes = nodes.sumOf { it.sizeBytes }.toDouble()
        if (totalBytes <= 0.0) return

        val totalArea = bounds.area.toDouble()
        val shortSide = minOf(bounds.width, bounds.height).toDouble()

        var row = listOf(nodes.first())
        var remaining = nodes.drop(1)
        var bestRatio = worstAspectRatio(row, shortSide, totalBytes, totalArea)

        while (remaining.isNotEmpty()) {
            val candidate = row + remaining.first()
            val candidateRatio = worstAspectRatio(candidate, shortSide, totalBytes, totalArea)
            if (candidateRatio > bestRatio && row.size > 1) break
            row = candidate
            remaining = remaining.drop(1)
            bestRatio = candidateRatio
        }

        val rowRects = layoutRow(row, bounds, totalBytes, totalArea)
        rowRects.forEachIndexed { index, rect ->
            result[row[index]] = rect
        }

        val remainingBounds = subtractRow(bounds, rowRects, isHorizontal = bounds.width >= bounds.height)
        squarifyRecursive(remaining, remainingBounds, result)
    }

    private fun worstAspectRatio(
        row: List<FileNode>,
        shortSide: Double,
        totalBytes: Double,
        totalArea: Double
    ): Double {
        if (row.isEmpty() || shortSide <= 0.0) return Double.MAX_VALUE
        val rowBytes = row.sumOf { it.sizeBytes }.toDouble()
        val rowArea = totalArea * (rowBytes / totalBytes)
        val rowWidth = rowArea / shortSide
        var worst = 0.0

        row.forEach { node ->
            val nodeArea = totalArea * (node.sizeBytes.toDouble() / totalBytes)
            val nodeHeight = if (rowWidth > 0.0) nodeArea / rowWidth else 0.0
            val aspect = if (nodeHeight > 0.0 && rowWidth > 0.0) {
                maxOf(rowWidth / nodeHeight, nodeHeight / rowWidth)
            } else {
                Double.MAX_VALUE
            }
            if (aspect > worst) worst = aspect
        }

        return worst
    }

    private fun layoutRow(
        row: List<FileNode>,
        bounds: LayoutRect,
        totalBytes: Double,
        totalArea: Double
    ): List<LayoutRect> {
        val rowBytes = row.sumOf { it.sizeBytes }.toDouble()
        val rowArea = totalArea * (rowBytes / totalBytes)
        val isHorizontal = bounds.width >= bounds.height
        val rects = mutableListOf<LayoutRect>()

        if (isHorizontal) {
            val rowWidth = (rowArea / bounds.height).toFloat().coerceAtMost(bounds.width)
            var top = bounds.top
            row.forEach { node ->
                val nodeArea = totalArea * (node.sizeBytes.toDouble() / totalBytes)
                val nodeHeight = if (rowWidth > 0f) (nodeArea / rowWidth).toFloat() else 0f
                rects.add(LayoutRect(bounds.left, top, bounds.left + rowWidth, top + nodeHeight))
                top += nodeHeight
            }
        } else {
            val rowHeight = (rowArea / bounds.width).toFloat().coerceAtMost(bounds.height)
            var left = bounds.left
            row.forEach { node ->
                val nodeArea = totalArea * (node.sizeBytes.toDouble() / totalBytes)
                val nodeWidth = if (rowHeight > 0f) (nodeArea / rowHeight).toFloat() else 0f
                rects.add(LayoutRect(left, bounds.top, left + nodeWidth, bounds.top + rowHeight))
                left += nodeWidth
            }
        }

        return rects
    }

    private fun subtractRow(
        bounds: LayoutRect,
        rowRects: List<LayoutRect>,
        isHorizontal: Boolean
    ): LayoutRect {
        if (rowRects.isEmpty()) return bounds
        return if (isHorizontal) {
            val usedWidth = rowRects.maxOf { it.right } - bounds.left
            LayoutRect(bounds.left + usedWidth, bounds.top, bounds.right, bounds.bottom)
        } else {
            val usedHeight = rowRects.maxOf { it.bottom } - bounds.top
            LayoutRect(bounds.left, bounds.top + usedHeight, bounds.right, bounds.bottom)
        }
    }
}

data class TreemapRect(
    val node: FileNode,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

data class Rect(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
) {
    val right: Float get() = left + width
    val bottom: Float get() = top + height
}
