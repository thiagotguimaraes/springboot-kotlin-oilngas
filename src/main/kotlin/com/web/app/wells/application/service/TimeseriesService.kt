package com.web.app.wells.application.service

import com.web.app.wells.domain.model.TimeseriesPoint
import com.web.app.wells.persistence.WellRepository
import com.web.app.wells.web.dto.TimeseriesInsertRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class TimeseriesService(
    private val wellRepository: WellRepository, private val jdbcTemplate: JdbcTemplate
) {

    fun insertData(wellId: UUID, req: TimeseriesInsertRequest) {
        val well = wellRepository.findById(wellId).orElseThrow { NoSuchElementException("Well not found") }

        // collection is already validated since it came from the DB.
        // No SQL injection risk unless user input is used
        val table = well.collection

        val sql = """
            INSERT INTO $table (timestamp, pressure, oil_rate, temperature) VALUES (?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.update(sql, req.timestamp, req.pressure, req.oilRate, req.temperature)
    }

    fun insertDataBatch(wellId: UUID, reqs: List<TimeseriesInsertRequest>) {
        if (reqs.isEmpty()) return

        val well = wellRepository.findById(wellId).orElseThrow { NoSuchElementException("Well not found") }

        // collection is already validated since it came from the DB.
        // No SQL injection risk unless user input is used
        val table = well.collection

        val sql = """
            INSERT INTO $table (timestamp, pressure, oil_rate, temperature)
            VALUES (?, ?, ?, ?)
        """.trimIndent()

        val batchArgs = reqs.map { req ->
            arrayOf(req.timestamp, req.pressure, req.oilRate, req.temperature)
        }

        jdbcTemplate.batchUpdate(sql, batchArgs)
    }


    fun getTimeseries(wellId: UUID, from: Long, to: Long): List<TimeseriesPoint> {
        val well = wellRepository.findById(wellId).orElseThrow { NoSuchElementException("Well not found") }

        val sql = """
            SELECT timestamp, pressure, oil_rate, temperature
            FROM ${well.collection}
            WHERE timestamp BETWEEN ? AND ?
            ORDER BY timestamp
        """.trimIndent()

        return jdbcTemplate.query(sql, { rs, _ ->
            TimeseriesPoint(
                timestamp = rs.getLong("timestamp"),
                pressure = rs.getDouble("pressure"),
                oilRate = rs.getDouble("oil_rate"),
                temperature = rs.getDouble("temperature")

            )
        }, from, to)
    }
}