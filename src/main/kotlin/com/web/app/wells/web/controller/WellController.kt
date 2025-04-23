package com.web.app.wells.web.controller

import com.web.app.wells.application.service.WellService
import com.web.app.wells.web.dto.WellRequest
import com.web.app.wells.web.dto.WellResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/wells")
class WellController(private val wellService: WellService) {

    @GetMapping
    fun getAll(): List<WellResponse> = wellService.getAll()

    @PostMapping
    fun create(@RequestBody request: WellRequest): ResponseEntity<WellResponse> {
        val well = wellService.create(request)
        return ResponseEntity.ok(well)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<String> {
        val well = wellService.getWellById(id)

        if (well == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Well id: $id not found")
        }

        wellService.delete(id)
        return ResponseEntity.ok("Well with ID $id deleted successfully.")
    }

    @GetMapping("/{id}")
    fun getWellById(@PathVariable id: UUID): ResponseEntity<WellResponse> {
        val well = wellService.getWellById(id)
        return if (well != null) {
            ResponseEntity.ok(well)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }
}
