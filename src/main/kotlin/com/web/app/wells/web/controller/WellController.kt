package com.web.app.wells.web.controller

import com.web.app.commom.ErrorWebResponse
import com.web.app.commom.SuccessWebResponse
import com.web.app.commom.WebResponse
import com.web.app.wells.application.service.WellService
import com.web.app.wells.web.dto.WellRequest
import com.web.app.wells.web.dto.WellResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/wells")
class WellController(private val wellService: WellService) {

    @GetMapping
    fun getAll(): WebResponse<List<WellResponse>> = SuccessWebResponse(body = wellService.getAll())

    @PostMapping
    fun create(@RequestBody request: WellRequest): WebResponse<WellResponse> {
        val well = wellService.create(request)
        return SuccessWebResponse(body = well)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): WebResponse<WellResponse> {
        val well = wellService.getWellById(id)

        if (well == null) {
            return ErrorWebResponse(status = HttpStatus.NOT_FOUND.value(), message = "Well not found")
        }

        wellService.delete(id)
        return SuccessWebResponse(body = null)
    }

    @GetMapping("/{id}")
    fun getWellById(@PathVariable id: UUID): WebResponse<WellResponse> {
        val well = wellService.getWellById(id)

        if (well != null) return SuccessWebResponse(body = well)

        return ErrorWebResponse(status = HttpStatus.NOT_FOUND.value(), message = "Well not found")
    }
}
