package com.web.app.wells.web.dto

data class WellRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val collection: String
)