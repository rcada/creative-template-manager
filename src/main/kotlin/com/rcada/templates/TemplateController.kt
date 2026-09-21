package com.rcada.templates

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/templates")
class TemplateController(private val service: TemplateService) {
    @PostMapping
    fun create(@Valid @RequestBody request: CreateTemplateRequest): ResponseEntity<CreativeTemplate> {
        val template = service.create(request.name, request.headline, request.width, request.height)
        return ResponseEntity.created(URI.create("/templates/${template.id}")).body(template)
    }

    @GetMapping
    fun findAll(@RequestParam(required = false) status: TemplateStatus?): List<CreativeTemplate> =
        service.findAll(status)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): CreativeTemplate =
        service.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found")

    @PutMapping("/{id}")
    fun edit(
        @PathVariable id: UUID,
        @Valid @RequestBody request: EditTemplateRequest
    ): CreativeTemplate =
        when (val result = service.edit(
            id, request.name, request.headline, request.width, request.height, request.version
        )) {
            is EditResult.Updated -> result.template
            EditResult.NotFound -> throw ResponseStatusException(
                HttpStatus.NOT_FOUND, "Template not found"
            )
            EditResult.NotDraft -> throw ResponseStatusException(
                HttpStatus.CONFLICT, "Published templates cannot be edited"
            )
            EditResult.VersionConflict -> throw ResponseStatusException(
                HttpStatus.CONFLICT, "Template changed; reload and review it before retrying"
            )
        }

    @PostMapping("/{id}/publish")
    fun publish(@PathVariable id: UUID): CreativeTemplate =
        when (val result = service.publish(id)) {
            is PublishResult.Published -> result.template
            PublishResult.NotFound -> throw ResponseStatusException(
                HttpStatus.NOT_FOUND, "Template not found"
            )
            PublishResult.Conflict -> throw ResponseStatusException(
                HttpStatus.CONFLICT, "Template is already published or changed during publishing"
            )
        }
}
