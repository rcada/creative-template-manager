package com.rcada.templates

sealed interface PublishResult {
    data class Published(val template: CreativeTemplate) : PublishResult
    data object NotFound : PublishResult
    data object Conflict : PublishResult
}
