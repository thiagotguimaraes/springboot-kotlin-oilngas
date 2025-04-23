package com.web.app.wells.domain.model

data class TimeseriesPoint(
    val timestamp: Long,
    val pressure: Double,
    val oilRate: Double,
    val temperature: Double
)