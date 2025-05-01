package com.web.app.wells.web.dto

import java.util.*

data class WellResponse(
    val id: UUID,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val collection: String,
    val startMs: Long?,
    val endMs: Long?
)