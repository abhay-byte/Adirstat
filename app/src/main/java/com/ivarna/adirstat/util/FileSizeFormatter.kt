package com.ivarna.adirstat.util

import java.text.DecimalFormat

/**
 * Utility for formatting file sizes in human-readable format
 */
object FileSizeFormatter {

    private val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")

    /**
     * Format bytes to human-readable string
     */
    fun format(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())

        return DecimalFormat("#,##0.##").format(value) + " " + units[digitGroups.coerceAtMost(units.size - 1)]
    }

    /**
     * Format bytes to short form (e.g., "1.5 GB")
     */
    fun formatShort(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0)
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024))
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }

    /**
     * Format bytes with unit only (e.g., "GB")
     */
    fun formatUnit(bytes: Long): String {
        if (bytes < 1024) return "B"
        if (bytes < 1024 * 1024) return "KB"
        if (bytes < 1024 * 1024 * 1024) return "MB"
        return "GB"
    }

    /**
     * Parse human-readable size to bytes
     */
    fun parse(sizeString: String): Long? {
        val regex = Regex("([0-9.,]+)\\s*([KMGTP]?B)", RegexOption.IGNORE_CASE)
        val match = regex.find(sizeString) ?: return null

        val value = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
        val unit = match.groupValues[2].uppercase()

        val multiplier = when (unit) {
            "B" -> 1L
            "KB" -> 1024L
            "MB" -> 1024L * 1024
            "GB" -> 1024L * 1024 * 1024
            "TB" -> 1024L * 1024 * 1024 * 1024
            "PB" -> 1024L * 1024 * 1024 * 1024 * 1024
            else -> return null
        }

        return (value * multiplier).toLong()
    }
}

/**
 * Format duration in milliseconds to human-readable string
 */
object DurationFormatter {

    /**
     * Format milliseconds to duration string
     */
    fun format(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            seconds > 0 -> "${seconds}s"
            else -> "< 1s"
        }
    }

    /**
     * Format milliseconds to short duration (e.g., "5m 30s")
     */
    fun formatShort(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60

        return when {
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }
}
