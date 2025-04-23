package com.web.app.wells.web.controller

import com.web.app.wells.application.service.WellService
import com.web.app.wells.web.dto.WellRequest
import com.web.app.wells.web.dto.WellResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/wells")
class WellController(private val wellService: WellService) {

    @GetMapping
    fun getAll(): List<WellResponse> = wellService.getAll()

    @PostMapping
    fun create(@RequestBody request: WellRequest): WellResponse =
        wellService.create(request)
}
