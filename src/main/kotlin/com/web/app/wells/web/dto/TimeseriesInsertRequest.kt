package com.web.app.wells.web.dto

data class TimeseriesInsertRequest(
    val timestamp: Long,
    val pressure: Double,
    val oilRate: Double,
    val temperature: Double
)