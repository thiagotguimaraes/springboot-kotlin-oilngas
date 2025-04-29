package com.web.app.wells.web.controller

import com.web.app.wells.application.service.TimeseriesService
import com.web.app.wells.domain.model.TimeseriesPoint
import com.web.app.wells.web.dto.TimeseriesInsertRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/wells")
class TimeseriesControler(
    private val timeseriesService: TimeseriesService
) {

    @PostMapping("/{id}/timeseries")
    fun insertData(
        @PathVariable id: UUID, @RequestBody request: TimeseriesInsertRequest
    ): ResponseEntity<String?> {
        timeseriesService.insertData(id, request)
        return ResponseEntity.ok("Data inserted successfully.")
    }

    @PostMapping("/{id}/timeseries/batch")
    fun insertDataBatch(
        @PathVariable id: UUID, @RequestBody requests: List<TimeseriesInsertRequest>
    ): ResponseEntity<String?> {
        timeseriesService.insertDataBatch(id, requests)
        return ResponseEntity.ok("Data inserted successfully.")
    }

    @GetMapping("/{id}/timeseries")
    fun getTimeseries(
        @PathVariable id: UUID, @RequestParam from: Long, @RequestParam to: Long
    ): ResponseEntity<List<TimeseriesPoint>?> {
        val data = timeseriesService.getTimeseries(id, from, to)
        return ResponseEntity.ok(data)
    }

}