package com.pseddev.playstreak.ui.progress

import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ItemType

/**
 * Temporary storage for activity data during edit mode.
 * This allows passing activity data between Timeline and Add Activity fragments.
 */
object EditActivityStorage {
    private var editActivity: Activity? = null
    private var pieceName: String? = null
    private var itemType: ItemType? = null
    
    fun setEditActivity(activity: Activity, pieceName: String = "", itemType: ItemType = ItemType.PIECE) {
        editActivity = activity
        this.pieceName = pieceName
        this.itemType = itemType
    }
    
    fun getEditActivity(): Activity? {
        return editActivity
    }
    
    fun getPieceName(): String? {
        return pieceName
    }
    
    fun getItemType(): ItemType? {
        return itemType
    }
    
    fun clearEditActivity() {
        editActivity = null
        pieceName = null
        itemType = null
    }
    
    fun isEditMode(): Boolean {
        return editActivity != null
    }
}