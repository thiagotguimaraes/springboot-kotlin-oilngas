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

    fun getAll(): List<WellResponse> = wellRepository.findAll().map { it.toDto() }

    fun getWellById(wellId: UUID): WellResponse? {
        return wellRepository.findById(wellId).orElse(null).toDto()
    }

    fun create(request: WellRequest): WellResponse {
        val wellId = UUID.randomUUID()
        val wellIdCleaned = wellId.toString().replace("-", "")
        val tableName = "timeseries_$wellIdCleaned"

        val entity = WellEntity(
            id = wellId,
            name = request.name,
            latitude = request.latitude,
            longitude = request.longitude,
            collection = tableName
        )

        val saved = wellRepository.save(entity)

        createTimeseriesTable(tableName)

        return saved.toDto()
    }

    fun delete(wellId: UUID) {
        wellRepository.deleteById(wellId)
    }

    private fun createTimeseriesTable(table: String) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS $table (LIKE timeseries_template INCLUDING ALL);")
        jdbcTemplate.execute("SELECT create_hypertable('$table', 'timestamp', if_not_exists => TRUE);")
        jdbcTemplate.execute(
            """
            CREATE TRIGGER update_boundaries_trigger
            BEFORE INSERT OR UPDATE ON $table
            FOR EACH ROW
            EXECUTE FUNCTION update_well_boundaries();
        """.trimIndent()
        )
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


