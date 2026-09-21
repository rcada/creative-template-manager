package com.rcada.templates

import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryTemplateRepository : TemplateRepository {
    private val templates = ConcurrentHashMap<UUID, CreativeTemplate>()

    override fun insert(template: CreativeTemplate): CreativeTemplate {
        // An atomic insert prevents an existing template from being overwritten.
        check(templates.putIfAbsent(template.id, template) == null) {
            "A template with this ID already exists"
        }
        return template
    }

    override fun findById(id: UUID): CreativeTemplate? = templates[id]

    override fun findAll(status: TemplateStatus?): List<CreativeTemplate> =
        templates.values.filter { template ->
            status == null || template.status == status
        }

    override fun replace(expected: CreativeTemplate, updated: CreativeTemplate): Boolean {
        require(expected.id == updated.id) { "A template's ID cannot change" }
        return templates.replace(expected.id, expected, updated)
    }
}
