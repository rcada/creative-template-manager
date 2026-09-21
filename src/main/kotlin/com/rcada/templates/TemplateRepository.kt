package com.rcada.templates

import java.util.UUID

interface TemplateRepository {
    fun insert(template: CreativeTemplate): CreativeTemplate
    fun findById(id: UUID): CreativeTemplate?
    fun findAll(status: TemplateStatus?): List<CreativeTemplate>

    // Atomically replace only if the stored template still equals the expected value.
    fun replace(expected: CreativeTemplate, updated: CreativeTemplate): Boolean
}
