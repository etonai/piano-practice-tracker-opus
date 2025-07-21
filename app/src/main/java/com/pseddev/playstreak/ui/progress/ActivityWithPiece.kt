package com.pseddev.playstreak.ui.progress

import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.PieceOrTechnique

data class ActivityWithPiece(
    val activity: Activity,
    val pieceOrTechnique: PieceOrTechnique
)