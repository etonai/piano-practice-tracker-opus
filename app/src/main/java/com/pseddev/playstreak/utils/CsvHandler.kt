package com.pseddev.playstreak.utils

import android.util.Log
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.utils.TextNormalizer
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.Reader
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

class CsvHandler {
    
    companion object {
        private const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
        private val datetimeFormatter = SimpleDateFormat(DATETIME_FORMAT, Locale.US)
        
        // CSV Headers
        private const val HEADER_DATE = "DateTime"
        private const val HEADER_LENGTH = "Length"
        private const val HEADER_ACTIVITY_TYPE = "ActivityType"
        private const val HEADER_PIECE = "Piece"
        private const val HEADER_LEVEL = "Level"
        private const val HEADER_PERFORMANCE_TYPE = "PerformanceType"
        private const val HEADER_NOTES = "Notes"
        
        fun exportActivitiesToCsv(
            writer: Writer,
            activities: List<Activity>,
            pieces: Map<Long, PieceOrTechnique>
        ) {
            Log.d("ExportDebug", "CsvHandler.exportActivitiesToCsv called with ${activities.size} activities")
            
            try {
                // Write CSV manually to avoid OpenCSV issues
                Log.d("ExportDebug", "Creating buffered writer")
                val bufferedWriter = writer.buffered()
                
                // Write header
                Log.d("ExportDebug", "Writing CSV header")
                val header = listOf(
                    HEADER_DATE,
                    HEADER_LENGTH,
                    HEADER_ACTIVITY_TYPE,
                    HEADER_PIECE,
                    HEADER_LEVEL,
                    HEADER_PERFORMANCE_TYPE,
                    HEADER_NOTES
                )
                bufferedWriter.write(header.joinToString(",") { escapeCSVField(it) })
                bufferedWriter.write("\n")
                Log.d("ExportDebug", "CSV header written")
                
                // Write activities
                Log.d("ExportDebug", "Writing ${activities.size} activity rows")
                var rowCount = 0
                activities.forEach { activity ->
                    val piece = pieces[activity.pieceOrTechniqueId]
                    if (piece != null) {
                        val datetime = datetimeFormatter.format(Date(activity.timestamp))
                        val row = listOf(
                            datetime,
                            activity.minutes.toString(),
                            activity.activityType.toString(),
                            TextNormalizer.normalizePieceName(piece.name),
                            activity.level.toString(),
                            activity.performanceType,
                            TextNormalizer.normalizeUserInput(activity.notes)
                        )
                        bufferedWriter.write(row.joinToString(",") { escapeCSVField(it) })
                        bufferedWriter.write("\n")
                        rowCount++
                        
                        if (rowCount % 10 == 0) {
                            Log.d("ExportDebug", "Written $rowCount rows so far")
                        }
                    }
                }
                Log.d("ExportDebug", "Finished writing $rowCount activity rows")
                
                // Just flush, don't close
                Log.d("ExportDebug", "Flushing buffered writer")
                bufferedWriter.flush()
                Log.d("ExportDebug", "CsvHandler.exportActivitiesToCsv completed successfully")
                
            } catch (e: Exception) {
                Log.e("ExportDebug", "Exception in CsvHandler.exportActivitiesToCsv: ${e.javaClass.simpleName} - ${e.message}", e)
                throw e
            }
        }
        
        private fun escapeCSVField(field: String): String {
            return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                "\"${field.replace("\"", "\"\"")}\""
            } else {
                field
            }
        }
        
        fun importActivitiesFromCsv(reader: Reader): ImportResult {
            val csvReader = CSVReader(reader)
            val importedActivities = mutableListOf<ImportedActivity>()
            val errors = mutableListOf<String>()
            val uniquePieces = mutableSetOf<String>()
            
            try {
                // Skip header
                val header = csvReader.readNext()
                if (header == null || !validateHeader(header)) {
                    errors.add("Invalid CSV header format")
                    return ImportResult(emptyList(), emptySet(), errors)
                }
                
                var lineNumber = 2 // Start from 2 (after header)
                var line: Array<String>?
                
                while (csvReader.readNext().also { line = it } != null) {
                    try {
                        val row = line!!
                        if (row.size < 6) {
                            errors.add("Line $lineNumber: Invalid number of columns")
                            lineNumber++
                            continue
                        }
                        
                        // Parse datetime
                        val timestamp = try {
                            datetimeFormatter.parse(row[0])?.time ?: throw Exception("Invalid datetime")
                        } catch (e: Exception) {
                            errors.add("Line $lineNumber: Invalid datetime format '${row[0]}' (expected: yyyy-MM-dd HH:mm:ss)")
                            lineNumber++
                            continue
                        }
                        
                        // Parse minutes
                        val minutes = try {
                            row[1].toInt()
                        } catch (e: Exception) {
                            errors.add("Line $lineNumber: Invalid minutes value '${row[1]}'")
                            lineNumber++
                            continue
                        }
                        
                        // Parse activity type
                        val activityType = try {
                            ActivityType.valueOf(row[2])
                        } catch (e: Exception) {
                            errors.add("Line $lineNumber: Invalid activity type '${row[2]}'")
                            lineNumber++
                            continue
                        }
                        
                        // Parse piece name and normalize it
                        val originalName = row[3]
                        val pieceName = TextNormalizer.normalizePieceName(originalName)
                        
                        Log.d("CsvImport", "Line $lineNumber: Original='$originalName', Normalized='$pieceName'")
                        Log.d("CsvImport", "Line $lineNumber: Original bytes: ${originalName.toByteArray().joinToString { it.toString() }}")
                        Log.d("CsvImport", "Line $lineNumber: Normalized bytes: ${pieceName.toByteArray().joinToString { it.toString() }}")
                            
                        if (pieceName.isEmpty()) {
                            errors.add("Line $lineNumber: Empty piece name")
                            lineNumber++
                            continue
                        }
                        
                        // Parse level
                        val level = try {
                            row[4].toInt()
                        } catch (e: Exception) {
                            errors.add("Line $lineNumber: Invalid level '${row[4]}'")
                            lineNumber++
                            continue
                        }
                        
                        // Validate level range
                        when (activityType) {
                            ActivityType.PRACTICE -> {
                                if (level !in 1..4) {
                                    errors.add("Line $lineNumber: Invalid practice level $level (must be 1-4)")
                                    lineNumber++
                                    continue
                                }
                            }
                            ActivityType.PERFORMANCE -> {
                                if (level !in 1..3) {
                                    errors.add("Line $lineNumber: Invalid performance level $level (must be 1-3)")
                                    lineNumber++
                                    continue
                                }
                            }
                        }
                        
                        val performanceType = row[5]
                        val notes = if (row.size > 6) TextNormalizer.normalizeUserInput(row[6]) else ""
                        
                        // Explicitly check for duplicates to work around Set issues
                        val alreadyExists = uniquePieces.any { existing ->
                            val isEqual = existing == pieceName
                            Log.d("CsvImport", "Comparing existing '$existing' == new '$pieceName': $isEqual")
                            if (!isEqual) {
                                Log.d("CsvImport", "Existing bytes: ${existing.toByteArray().joinToString { it.toString() }}")
                                Log.d("CsvImport", "New bytes: ${pieceName.toByteArray().joinToString { it.toString() }}")
                            }
                            isEqual
                        }
                        
                        if (!alreadyExists) {
                            uniquePieces.add(pieceName)
                            Log.d("CsvImport", "Added new piece '$pieceName' to set")
                        } else {
                            Log.d("CsvImport", "Piece '$pieceName' already exists, skipping")
                        }
                        
                        importedActivities.add(
                            ImportedActivity(
                                timestamp = timestamp,
                                pieceName = pieceName,
                                activityType = activityType,
                                level = level,
                                performanceType = performanceType,
                                minutes = minutes,
                                notes = notes
                            )
                        )
                        
                    } catch (e: Exception) {
                        errors.add("Line $lineNumber: ${e.message}")
                    }
                    
                    lineNumber++
                }
                
            } finally {
                csvReader.close()
            }
            
            Log.d("CsvImport", "Final results: ${importedActivities.size} activities, ${uniquePieces.size} unique pieces")
            Log.d("CsvImport", "Unique pieces: ${uniquePieces.toList()}")
            
            return ImportResult(importedActivities, uniquePieces, errors)
        }
        
        private fun validateHeader(header: Array<String>): Boolean {
            if (header.size < 6) return false
            
            return header[0] == HEADER_DATE &&
                    header[1] == HEADER_LENGTH &&
                    header[2] == HEADER_ACTIVITY_TYPE &&
                    header[3] == HEADER_PIECE &&
                    header[4] == HEADER_LEVEL &&
                    header[5] == HEADER_PERFORMANCE_TYPE
        }
    }
    
    data class ImportedActivity(
        val timestamp: Long,
        val pieceName: String,
        val activityType: ActivityType,
        val level: Int,
        val performanceType: String,
        val minutes: Int,
        val notes: String
    )
    
    data class ImportResult(
        val activities: List<ImportedActivity>,
        val uniquePieceNames: Set<String>,
        val errors: List<String>
    )
}