package com.rcada.templates

sealed interface EditResult {
    data class Updated(val template: CreativeTemplate) : EditResult
    data object NotFound : EditResult
    data object NotDraft : EditResult
    data object VersionConflict : EditResult
}
