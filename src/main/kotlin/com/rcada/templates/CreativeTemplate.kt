package com.rcada.templates

import java.util.UUID

enum class TemplateStatus {
    DRAFT,
    PUBLISHED
}

data class CreativeTemplate(
    val id: UUID,
    val name: String,
    val headline: String,
    val width: Int,
    val height: Int,
    val status: TemplateStatus = TemplateStatus.DRAFT,
    val version: Long = 1
)
