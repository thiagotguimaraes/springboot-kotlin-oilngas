package com.web.app.wells.web.controller

import com.web.app.wells.application.service.TimeseriesService
import com.web.app.wells.domain.model.TimeseriesPoint
import com.web.app.wells.web.dto.TimeseriesInsertRequest
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
    ) = timeseriesService.insertData(id, request)


    @GetMapping("/{id}/timeseries")
    fun getTimeseries(
        @PathVariable id: UUID, @RequestParam from: Long, @RequestParam to: Long
    ): List<TimeseriesPoint> = timeseriesService.getTimeseries(id, from, to)

}