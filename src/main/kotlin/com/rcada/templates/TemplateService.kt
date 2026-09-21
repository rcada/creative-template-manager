package com.rcada.templates

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TemplateService(private val repository: TemplateRepository) {
    fun create(name: String, headline: String, width: Int, height: Int): CreativeTemplate {
        val template = CreativeTemplate(
            id = UUID.randomUUID(),
            name = name.trim(),
            headline = headline.trim(),
            width = width,
            height = height
        )
        return repository.insert(template)
    }

    fun findById(id: UUID): CreativeTemplate? = repository.findById(id)

    fun findAll(status: TemplateStatus?): List<CreativeTemplate> = repository.findAll(status)

    fun edit(
        id: UUID,
        name: String,
        headline: String,
        width: Int,
        height: Int,
        expectedVersion: Long
    ): EditResult {
        val template = repository.findById(id) ?: return EditResult.NotFound

        if (template.status != TemplateStatus.DRAFT) {
            return EditResult.NotDraft
        }
        if (template.version != expectedVersion) {
            return EditResult.VersionConflict
        }

        val updated = template.copy(
            name = name.trim(),
            headline = headline.trim(),
            width = width,
            height = height,
            version = template.version + 1
        )

        // Another edit or publish may occur after our read, so the save must also check.
        return if (repository.replace(template, updated)) {
            EditResult.Updated(updated)
        } else {
            EditResult.VersionConflict
        }
    }

    fun publish(id: UUID): PublishResult {
        val template = repository.findById(id) ?: return PublishResult.NotFound

        if (template.status != TemplateStatus.DRAFT) {
            return PublishResult.Conflict
        }

        val published = template.copy(
            status = TemplateStatus.PUBLISHED,
            version = template.version + 1
        )
        return if (repository.replace(template, published)) {
            PublishResult.Published(published)
        } else {
            PublishResult.Conflict
        }
    }
}
