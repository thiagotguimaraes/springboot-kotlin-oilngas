package com.web.app.wells.application.service

import com.web.app.utils.lineUpTrimIndent
import com.web.app.wells.domain.model.TimeseriesPoint
import com.web.app.wells.persistence.WellRepository
import com.web.app.wells.web.dto.TimeseriesInsertRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import java.util.*

@Service
class TimeseriesService(
    private val wellRepository: WellRepository, private val jdbcTemplate: JdbcTemplate
) {

    fun insertData(wellId: UUID, req: TimeseriesInsertRequest) {
        val well = wellRepository.findById(wellId).orElseThrow { NoSuchElementException("Well not found") }

        val table = well.collection
        WellService.validateTableName(table)

        val sql = """
            INSERT INTO $table (timestamp, pressure, oil_rate, temperature) VALUES (?, ?, ?, ?)
        """.lineUpTrimIndent()

        jdbcTemplate.update(sql, req.timestamp, req.pressure, req.oilRate, req.temperature)
    }

    fun insertDataBatch(wellId: UUID, reqs: List<TimeseriesInsertRequest>) {
        if (reqs.isEmpty()) throw IllegalArgumentException("Request list cannot be empty")

        val well = wellRepository.findById(wellId).orElseThrow { NoSuchElementException("Well not found") }

        val table = well.collection
        WellService.validateTableName(table)

        val sql = """
            INSERT INTO $table (timestamp, pressure, oil_rate, temperature) VALUES (?, ?, ?, ?)
        """.lineUpTrimIndent()

        val batchArgs = reqs.map { req ->
            arrayOf(req.timestamp, req.pressure, req.oilRate, req.temperature)
        }

        jdbcTemplate.batchUpdate(sql, batchArgs)
    }

    internal val timeseriesPointRowMapper = RowMapper { rs, _ ->
        TimeseriesPoint(
            timestamp = rs.getLong("timestamp"),
            pressure = rs.getDouble("pressure"),
            oilRate = rs.getDouble("oil_rate"),
            temperature = rs.getDouble("temperature")
        )
    }

    fun getTimeseries(wellId: UUID, from: Long, to: Long): List<TimeseriesPoint> {
        val well = wellRepository.findById(wellId).orElseThrow { NoSuchElementException("Well not found") }

        val table = well.collection
        WellService.validateTableName(table)

        val sql = """
            SELECT timestamp, pressure, oil_rate, temperature
            FROM $table
            WHERE timestamp BETWEEN ? AND ?
            ORDER BY timestamp
        """.lineUpTrimIndent()

        return jdbcTemplate.query(sql, timeseriesPointRowMapper, from, to)
    }

    fun deleteTimeseriesData(wellId: UUID, from: Long, to: Long) {
        val well = wellRepository.findById(wellId).orElseThrow { NoSuchElementException("Well not found") }

        val table = well.collection
        WellService.validateTableName(table)

        val sql = """
            DELETE FROM $table
            WHERE timestamp BETWEEN ? AND ?
        """.lineUpTrimIndent()

        jdbcTemplate.update(sql, from, to)
    }

    fun deleteAllTimeseriesData(wellId: UUID) {
        val well = wellRepository.findById(wellId).orElseThrow { NoSuchElementException("Well not found") }

        val table = well.collection
        WellService.validateTableName(table)

        val sql = "DELETE FROM $table"

        jdbcTemplate.update(sql)
    }
}