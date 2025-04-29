package com.web.app.wells.application.service

import com.web.app.wells.persistence.WellEntity
import com.web.app.wells.persistence.WellRepository
import com.web.app.wells.web.dto.WellRequest
import com.web.app.wells.web.dto.WellResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class WellService(
    private val wellRepository: WellRepository,
    private val jdbcTemplate: JdbcTemplate
) {

    fun getAll(): List<WellResponse> = wellRepository.findAll().map { toDto(it) }

    fun getWellById(wellId: UUID): WellResponse? {
        val wellEntity = wellRepository.findById(wellId).orElseThrow {
            NoSuchElementException("Well not found")
        }
        return toDto(wellEntity)
    }

    fun create(request: WellRequest): WellResponse {
        val wellId = UUID.randomUUID()
        val tableName = makeTableName(wellId)

        val entity = WellEntity(
            id = wellId,
            name = request.name,
            latitude = request.latitude,
            longitude = request.longitude,
            collection = tableName
        )

        val saved = wellRepository.save(entity)

        createTimeseriesTable(saved.collection)

        return toDto(saved)
    }

    fun delete(wellId: UUID) {
        wellRepository.deleteById(wellId)
    }

    internal fun createTimeseriesTable(table: String) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS $table (LIKE timeseries_template INCLUDING ALL);")
        jdbcTemplate.execute("SELECT create_hypertable('$table', 'timestamp', if_not_exists => TRUE);")
    }

    companion object {
        internal fun makeTableName(wellId: UUID): String {
            val wellIdCleaned = wellId.toString().replace("-", "")
            return "timeseries_$wellIdCleaned"
        }

        internal fun validateTableName(tableName: String) {
            var error = false
            if (!tableName.startsWith("timeseries_")) error = true

            val uuidRegex = Regex("^[a-fA-F0-9]{32}$") // Matches a UUID without dashes
            val uuidPart = tableName.removePrefix("timeseries_")
            if (!uuidRegex.matches(uuidPart)) error = true

            if (error) throw IllegalArgumentException("Invalid table name.")
        }

        internal fun toDto(well: WellEntity): WellResponse = WellResponse(
            id = well.id,
            name = well.name,
            latitude = well.latitude,
            longitude = well.longitude,
            collection = well.collection,
            startMs = well.startMs,
            endMs = well.endMs
        )
    }
}


