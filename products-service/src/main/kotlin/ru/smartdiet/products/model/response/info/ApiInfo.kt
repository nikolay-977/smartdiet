package ru.smartdiet.products.model.response.info

import kotlinx.serialization.Serializable

@Serializable
data class ApiInfo(
    val service: String,
    val version: String,
    val description: String,
    val authentication: String,
    val endpoints: List<EndpointInfo>,
    val searchParameters: List<String>
)

@Serializable
data class EndpointInfo(
    val method: String,
    val path: String,
    val description: String
)