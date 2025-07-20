package com.pseddev.practicetracker.ui.progress

import com.pseddev.practicetracker.data.entities.Activity
import com.pseddev.practicetracker.data.entities.PieceOrTechnique

data class ActivityWithPiece(
    val activity: Activity,
    val pieceOrTechnique: PieceOrTechnique
)