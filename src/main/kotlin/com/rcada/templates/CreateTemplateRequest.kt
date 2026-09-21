package com.rcada.templates

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateTemplateRequest(
    @field:NotBlank val name: String,
    @field:NotBlank val headline: String,
    @field:Positive val width: Int,
    @field:Positive val height: Int
)
