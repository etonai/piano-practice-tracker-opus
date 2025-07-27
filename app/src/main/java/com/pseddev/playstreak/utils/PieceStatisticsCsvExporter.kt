package com.pseddev.playstreak.utils

import com.opencsv.CSVWriter
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for exporting piece statistics to CSV format
 * Per DevCycle 2025-0004 Phase 1: Export piece statistics separately in CSV format
 */
object PieceStatisticsCsvExporter {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * Exports piece statistics to CSV format
     * @param writer The Writer to write CSV data to
     * @param pieces List of pieces with statistics to export
     * @param includeRecentActivities Whether to include JSON arrays of recent activity timestamps
     */
    fun exportPieceStatistics(
        writer: Writer, 
        pieces: List<PieceOrTechnique>,
        includeRecentActivities: Boolean = true
    ) {
        val csvWriter = CSVWriter(writer)
        
        // Write header
        val header = if (includeRecentActivities) {
            arrayOf(
                "id",
                "name", 
                "type",
                "dateCreated",
                "practiceCount",
                "performanceCount", 
                "lastPracticeDate",
                "lastPerformanceDate",
                "totalPracticeDuration",
                "averagePracticeLevel",
                "lastUpdated",
                "lastThreePractices",
                "lastThreePerformances"
            )
        } else {
            arrayOf(
                "id",
                "name",
                "type", 
                "dateCreated",
                "practiceCount",
                "performanceCount",
                "lastPracticeDate", 
                "lastPerformanceDate",
                "totalPracticeDuration",
                "averagePracticeLevel",
                "lastUpdated"
            )
        }
        csvWriter.writeNext(header)
        
        // Write piece data
        pieces.forEach { piece ->
            val row = if (includeRecentActivities) {
                arrayOf(
                    piece.id.toString(),
                    piece.name,
                    piece.type.name,
                    formatTimestamp(piece.dateCreated),
                    piece.practiceCount.toString(),
                    piece.performanceCount.toString(),
                    formatTimestamp(piece.lastPracticeDate),
                    formatTimestamp(piece.lastPerformanceDate),
                    piece.totalPracticeDuration.toString(),
                    piece.averagePracticeLevel?.toString() ?: "",
                    formatTimestamp(piece.lastUpdated),
                    piece.lastThreePractices ?: "",
                    piece.lastThreePerformances ?: ""
                )
            } else {
                arrayOf(
                    piece.id.toString(),
                    piece.name,
                    piece.type.name,
                    formatTimestamp(piece.dateCreated),
                    piece.practiceCount.toString(),
                    piece.performanceCount.toString(),
                    formatTimestamp(piece.lastPracticeDate),
                    formatTimestamp(piece.lastPerformanceDate), 
                    piece.totalPracticeDuration.toString(),
                    piece.averagePracticeLevel?.toString() ?: "",
                    formatTimestamp(piece.lastUpdated)
                )
            }
            csvWriter.writeNext(row)
        }
        
        csvWriter.flush()
    }
    
    /**
     * Exports piece favorites to CSV format
     * @param writer The Writer to write CSV data to
     * @param favorites List of favorite pieces with their metadata
     */
    fun exportPieceFavorites(
        writer: Writer,
        favorites: List<Pair<PieceOrTechnique, Long>> // Pair of (piece, dateAdded)
    ) {
        val csvWriter = CSVWriter(writer)
        
        // Write header
        csvWriter.writeNext(arrayOf(
            "pieceId",
            "pieceName",
            "pieceType", 
            "dateAdded"
        ))
        
        // Write favorite data
        favorites.forEach { (piece, dateAdded) ->
            csvWriter.writeNext(arrayOf(
                piece.id.toString(),
                piece.name,
                piece.type.name,
                formatTimestamp(dateAdded)
            ))
        }
        
        csvWriter.flush()
    }
    
    /**
     * Formats a timestamp for CSV export
     */
    private fun formatTimestamp(timestamp: Long?): String {
        return if (timestamp != null) {
            dateFormat.format(Date(timestamp))
        } else {
            ""
        }
    }
}