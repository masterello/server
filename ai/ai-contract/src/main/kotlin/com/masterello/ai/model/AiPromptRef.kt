package com.masterello.ai.model

data class AiPromptRef(
        val promptRef: String,
        val promptRefVersion: String?,
        val params: Map<String, String>?
)