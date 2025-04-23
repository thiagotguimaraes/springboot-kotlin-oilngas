package com.example.wells.web.dto

data class TimeseriesInsertRequest(
    val timestamp: Long,
    val pressure: Double,
    val oilRate: Double,
    val temperature: Double
)