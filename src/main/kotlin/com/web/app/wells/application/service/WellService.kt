package com.web.app.wells.application.service

import com.web.app.wells.persistence.WellEntity
import com.web.app.wells.persistence.WellRepository
import com.web.app.wells.web.dto.WellRequest
import com.web.app.wells.web.dto.WellResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class WellService(private val wellRepository: WellRepository) {

    fun getAll(): List<WellResponse> =
        wellRepository.findAll().map { it.toDto() }

    fun create(request: WellRequest): WellResponse {
        val entity = WellEntity(
            name = request.name,
            latitude = request.latitude,
            longitude = request.longitude,
            collection = request.collection
        )
        return wellRepository.save(entity).toDto()
    }

    private fun WellEntity.toDto(): WellResponse = WellResponse(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        collection = collection,
        startMs = startMs,
        endMs = endMs
    )
}
