package com.example.pianotrackopus.ui.progress

import com.example.pianotrackopus.data.entities.Activity
import com.example.pianotrackopus.data.entities.PieceOrTechnique

data class ActivityWithPiece(
    val activity: Activity,
    val pieceOrTechnique: PieceOrTechnique
)