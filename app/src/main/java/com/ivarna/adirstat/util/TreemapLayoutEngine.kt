package com.ivarna.adirstat.util

import com.ivarna.adirstat.domain.model.FileNode
import kotlin.math.max
import kotlin.math.min

/**
 * Squarified Treemap Layout Engine
 */
object TreemapLayoutEngine {

    fun calculateLayout(
        items: List<FileNode>,
        bounds: Rect,
        minSize: Long = 0
    ): List<TreemapRect> {
        if (items.isEmpty() || bounds.width <= 0f || bounds.height <= 0f) {
            return emptyList()
        }

        val filteredItems = items.filter { it.size >= minSize }
        if (filteredItems.isEmpty()) {
            return emptyList()
        }

        val totalSize = filteredItems.sumOf { it.size }
        if (totalSize == 0L) {
            return emptyList()
        }

        val area = bounds.width * bounds.height
        val scale = area / totalSize.toFloat()

        val scaledItems = filteredItems.map { item ->
            val scaledSize = (item.size * scale).toLong().coerceAtLeast(1L)
            ScaledItem(item, scaledSize)
        }

        return squarify(scaledItems, bounds)
    }

    private fun squarify(
        items: List<ScaledItem>,
        bounds: Rect
    ): List<TreemapRect> {
        if (items.isEmpty()) return emptyList()
        
        if (items.size == 1) {
            return listOf(
                TreemapRect(
                    node = items[0].node,
                    x = bounds.left,
                    y = bounds.top,
                    width = bounds.width,
                    height = bounds.height
                )
            )
        }

        val sortedItems = items.sortedByDescending { it.scaledSize }
        val rows = layoutRow(sortedItems, bounds)
        
        val result = mutableListOf<TreemapRect>()
        
        var currentY = bounds.top
        for (row in rows) {
            val rowHeight = calculateRowHeight(row, bounds.width)
            
            var currentX = bounds.left
            for (item in row) {
                val itemWidth = (item.scaledSize.toFloat() / row.sumOf { it.scaledSize }) * bounds.width
                
                result.add(
                    TreemapRect(
                        node = item.node,
                        x = currentX,
                        y = currentY,
                        width = itemWidth,
                        height = rowHeight
                    )
                )
                currentX += itemWidth
            }
            
            currentY += rowHeight
        }
        
        return result
    }

    private fun layoutRow(
        items: List<ScaledItem>,
        bounds: Rect
    ): List<List<ScaledItem>> {
        if (items.isEmpty()) return emptyList()

        val rows = mutableListOf<List<ScaledItem>>()
        var remaining = items.toMutableList()
        
        while (remaining.isNotEmpty()) {
            val row = mutableListOf<ScaledItem>()
            var currentRow = remaining.toMutableList()
            
            while (currentRow.isNotEmpty()) {
                val testRow = row + currentRow.first()
                if (row.isEmpty() || isBetterAspectRatio(testRow, row, bounds)) {
                    row.add(currentRow.removeAt(0))
                } else {
                    break
                }
            }
            
            rows.add(row)
            remaining = currentRow
        }
        
        return rows
    }

    private fun isBetterAspectRatio(
        newRow: List<ScaledItem>,
        oldRow: List<ScaledItem>,
        bounds: Rect
    ): Boolean {
        if (oldRow.isEmpty()) return true
        
        val newWorst = worstAspectRatio(newRow, bounds)
        val oldWorst = worstAspectRatio(oldRow, bounds)
        
        return newWorst <= oldWorst
    }

    private fun worstAspectRatio(
        row: List<ScaledItem>,
        bounds: Rect
    ): Float {
        if (row.isEmpty()) return Float.MAX_VALUE
        
        val rowTotal = row.sumOf { it.scaledSize }
        val rowWidth = bounds.width
        val rowHeight = calculateRowHeight(row, bounds.width)
        
        var worst = 0f
        for (item in row) {
            val itemArea = item.scaledSize.toFloat()
            val itemWidth = (itemArea / rowTotal) * rowWidth
            val itemHeight = rowHeight
            
            if (itemWidth > 0f && itemHeight > 0f) {
                val ratio = max(itemWidth / itemHeight, itemHeight / itemWidth)
                worst = max(worst, ratio)
            }
        }
        
        return worst
    }

    private fun calculateRowHeight(row: List<ScaledItem>, containerWidth: Float): Float {
        if (row.isEmpty()) return 0f
        
        val rowTotal = row.sumOf { it.scaledSize }
        if (rowTotal == 0L) return 0f
        
        return rowTotal.toFloat() / containerWidth
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
)

private data class ScaledItem(
    val node: FileNode,
    val scaledSize: Long
)
