package com.web.app.wells.application.service

import com.web.app.utils.lineUpTrimIndent
import com.web.app.wells.domain.model.TimeseriesPoint
import com.web.app.wells.persistence.WellEntity
import com.web.app.wells.persistence.WellRepository
import com.web.app.wells.web.dto.TimeseriesInsertRequest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class TimeseriesServiceTest {

    @Mock
    private lateinit var wellRepository: WellRepository

    @Mock
    private lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var timeseriesService: TimeseriesService

    @BeforeEach
    fun setup() {
        timeseriesService = TimeseriesService(wellRepository, jdbcTemplate)
    }

    @Test
    fun `insertData should call jdbcTemplate with correct parameters`() {
        val wellId = UUID.randomUUID()
        val request = TimeseriesInsertRequest(1234567890L, 20.0, 30.0, 10.0)

        val mockedWellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, WellService.makeTableName(wellId))
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(mockedWellEntity))

        timeseriesService.insertData(wellId, request)

        verify(jdbcTemplate).update(
            eq("""INSERT INTO ${WellService.makeTableName(wellId)} (timestamp, pressure, oil_rate, temperature) VALUES (?, ?, ?, ?)""".lineUpTrimIndent()),
            eq(1234567890L),
            eq(20.0),
            eq(30.0),
            eq(10.0)
        )
    }

    @Test
    fun `insertData should throw exception if well not found`() {
        val wellId = UUID.randomUUID()
        val request = TimeseriesInsertRequest(1234567890L, 20.0, 30.0, 10.0)

        val exception = assertThrows(NoSuchElementException::class.java) {
            timeseriesService.insertData(wellId, request)
        }

        assertEquals("Well not found", exception.message)
    }

    @Test
    fun `insertDataBatch should call jdbcTemplate batchUpdate with correct parameters`() {
        val wellId = UUID.randomUUID()
        val points = listOf(
            TimeseriesInsertRequest(1234567890L, 101.3, 25.5, 20.0),
            TimeseriesInsertRequest(1234567891L, 102.0, 26.0, 21.0)
        )

        val mockedWellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, WellService.makeTableName(wellId))
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(mockedWellEntity))

        timeseriesService.insertDataBatch(wellId, points)

        val argumentCaptorSqlQuery = argumentCaptor<String>()
        val argumentCaptorBatchArgs = argumentCaptor<List<Array<Any>>>()

        verify(jdbcTemplate).batchUpdate(
            argumentCaptorSqlQuery.capture(), argumentCaptorBatchArgs.capture()
        )

        assertEquals(
            """
                INSERT INTO ${WellService.makeTableName(wellId)} (timestamp, pressure, oil_rate, temperature) VALUES (?, ?, ?, ?)
                """.lineUpTrimIndent(), argumentCaptorSqlQuery.firstValue
        )

        val expectedBatchArgs = listOf(
            arrayOf(1234567890L, 101.3, 25.5, 20.0), arrayOf(1234567891L, 102.0, 26.0, 21.0)
        )

        assertEquals(expectedBatchArgs.size, argumentCaptorBatchArgs.firstValue.size)
        argumentCaptorBatchArgs.firstValue.forEachIndexed { index, actualArgs ->
            assertEquals(expectedBatchArgs[index].toList(), actualArgs.toList())
        }
    }

    @Test
    fun `insertDataBatch should throw exception if well not found`() {
        val wellId = UUID.randomUUID()
        val points = listOf(
            TimeseriesInsertRequest(1234567890L, 101.3, 25.5, 20.0)
        )

        val exception = assertThrows(NoSuchElementException::class.java) {
            timeseriesService.insertDataBatch(wellId, points)
        }

        assertEquals("Well not found", exception.message)
    }

    @Test
    fun `insertDataBatch should not call jdbcTemplate if well is not found`() {
        val wellId = UUID.randomUUID()
        val points = listOf(
            TimeseriesInsertRequest(1234567890L, 101.3, 25.5, 20.0)
        )

        assertThrows(NoSuchElementException::class.java) {
            timeseriesService.insertDataBatch(wellId, points)
        }

        verifyNoInteractions(jdbcTemplate)
    }

    @Test
    fun `insertData should not call jdbcTemplate if well is not found`() {
        val wellId = UUID.randomUUID()
        val request = TimeseriesInsertRequest(1234567890L, 20.0, 30.0, 10.0)

        assertThrows(NoSuchElementException::class.java) {
            timeseriesService.insertData(wellId, request)
        }

        verifyNoInteractions(jdbcTemplate)
    }

    @Test
    fun `insertDataBatch should not call jdbcTemplate if batch is empty`() {
        val wellId = UUID.randomUUID()
        val points = emptyList<TimeseriesInsertRequest>()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            timeseriesService.insertDataBatch(wellId, points)
        }

        assertEquals("Request list cannot be empty", exception.message)
        verifyNoInteractions(jdbcTemplate)
    }

    @Test
    fun `getTimeseries should return data when well exists`() {
        val wellId = UUID.randomUUID()
        val startTime = 1234567890L
        val endTime = 1234567990L
        val expectedData = listOf(
            TimeseriesPoint(1234567890L, 101.3, 25.5, 20.0), TimeseriesPoint(1234567891L, 102.0, 26.0, 21.0)
        )

        val mockedWellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, WellService.makeTableName(wellId))
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(mockedWellEntity))
        `when`(
            jdbcTemplate.query(
                anyString(), any<RowMapper<TimeseriesPoint>>(), eq(startTime), eq(endTime)
            )
        ).thenReturn(expectedData)

        val result = timeseriesService.getTimeseries(wellId, startTime, endTime)

        assertEquals(expectedData, result)
    }

    @Test
    fun `getTimeseries should throw exception if well not found`() {
        val wellId = UUID.randomUUID()
        val startTime = 1234567890L
        val endTime = 1234567990L

        val exception = assertThrows(NoSuchElementException::class.java) {
            timeseriesService.getTimeseries(wellId, startTime, endTime)
        }

        assertEquals("Well not found", exception.message)
    }

    @Test
    fun `getTimeseries should not call jdbcTemplate if well is not found`() {
        val wellId = UUID.randomUUID()
        val startTime = 1234567890L
        val endTime = 1234567990L

        assertThrows(NoSuchElementException::class.java) {
            timeseriesService.getTimeseries(wellId, startTime, endTime)
        }

        verifyNoInteractions(jdbcTemplate)
    }

    @Test
    fun `getTimeseries should return empty list if no data in range`() {
        val wellId = UUID.randomUUID()
        val startTime = 1234567890L
        val endTime = 1234567990L

        val mockedWellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, WellService.makeTableName(wellId))
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(mockedWellEntity))
        `when`(
            jdbcTemplate.query(
                anyString(), any<RowMapper<TimeseriesPoint>>(), eq(startTime), eq(endTime)
            )
        ).thenReturn(emptyList())

        val result = timeseriesService.getTimeseries(wellId, startTime, endTime)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getTimeseries should prevent SQL injection in table name`() {
        val wellId = UUID.randomUUID()
        val startTime = 1234567890L
        val endTime = 1234567990L

        val maliciousWellEntity = WellEntity(
            wellId, "Well A", 1.0, 1.0, "malicious_table; DROP TABLE users;"
        )
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(maliciousWellEntity))

        assertThrows(IllegalArgumentException::class.java) {
            timeseriesService.getTimeseries(wellId, startTime, endTime)
        }

        verifyNoInteractions(jdbcTemplate)
    }

    @Test
    fun `getTimeseriesData should call jdbcTemplate with correct parameters`() {
        val wellId = UUID.randomUUID()
        val from = 1234567890L
        val to = 1234567990L

        val mockedWellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, WellService.makeTableName(wellId))
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(mockedWellEntity))

        timeseriesService.getTimeseries(wellId, from, to)

        verify(jdbcTemplate).query(
            eq("""SELECT timestamp, pressure, oil_rate, temperature FROM ${WellService.makeTableName(wellId)} WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp""".lineUpTrimIndent()),
            any<RowMapper<TimeseriesPoint>>(),
            eq(from),
            eq(to)
        )
    }

    @Test
    fun `timeseriesPointRowMapper should map ResultSet to TimeseriesPoint correctly`() {
        // Mock ResultSet
        val resultSet = mock<ResultSet> {
            on { getLong("timestamp") } doReturn 1234567890L
            on { getDouble("pressure") } doReturn 101.3
            on { getDouble("oil_rate") } doReturn 25.5
            on { getDouble("temperature") } doReturn 20.0
        }

        // Invoke the RowMapper
        val timeseriesPoint = timeseriesService.timeseriesPointRowMapper.mapRow(resultSet, 0)

        // Assertions
        assertEquals(1234567890L, timeseriesPoint.timestamp)
        assertEquals(101.3, timeseriesPoint.pressure)
        assertEquals(25.5, timeseriesPoint.oilRate)
        assertEquals(20.0, timeseriesPoint.temperature)
    }

    @Test
    fun `insertData should prevent SQL injection in table name`() {
        val wellId = UUID.randomUUID()
        val request = TimeseriesInsertRequest(1234567890L, 20.0, 30.0, 10.0)

        val maliciousWellEntity = WellEntity(
            wellId, "Well A", 1.0, 1.0, "malicious_table; DROP TABLE users;"
        )
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(maliciousWellEntity))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            timeseriesService.insertData(wellId, request)
        }

        assertEquals("Invalid table name.", exception.message)
        verifyNoInteractions(jdbcTemplate)
    }

    @Test
    fun `insertDataBatch should prevent SQL injection in table name`() {
        val wellId = UUID.randomUUID()
        val points = listOf(
            TimeseriesInsertRequest(1234567890L, 101.3, 25.5, 20.0),
            TimeseriesInsertRequest(1234567891L, 102.0, 26.0, 21.0)
        )

        val maliciousWellEntity = WellEntity(
            wellId, "Well A", 1.0, 1.0, "malicious_table; DROP TABLE users;"
        )
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(maliciousWellEntity))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            timeseriesService.insertDataBatch(wellId, points)
        }

        assertEquals("Invalid table name.", exception.message)
        verifyNoInteractions(jdbcTemplate)
    }

    @Test
    fun `deleteTimeseriesData should call jdbcTemplate with correct parameters`() {
        val wellId = UUID.randomUUID()
        val from = 1234567890L
        val to = 1234567990L

        val mockedWellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, WellService.makeTableName(wellId))
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(mockedWellEntity))

        timeseriesService.deleteTimeseriesData(wellId, from, to)

        verify(jdbcTemplate).update(
            eq("""DELETE FROM ${WellService.makeTableName(wellId)} WHERE timestamp BETWEEN ? AND ?""".lineUpTrimIndent()),
            eq(from),
            eq(to)
        )
    }

    @Test
    fun `deleteTimeseriesData should throw exception if well not found`() {
        val wellId = UUID.randomUUID()
        val from = 1234567890L
        val to = 1234567990L

        val exception = assertThrows(NoSuchElementException::class.java) {
            timeseriesService.deleteTimeseriesData(wellId, from, to)
        }

        assertEquals("Well not found", exception.message)
        verifyNoInteractions(jdbcTemplate)
    }

    @Test
    fun `deleteTimeseriesData should prevent SQL injection in table name`() {
        val wellId = UUID.randomUUID()
        val from = 1234567890L
        val to = 1234567990L

        val maliciousWellEntity = WellEntity(
            wellId, "Well A", 1.0, 1.0, "malicious_table; DROP TABLE users;"
        )
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(maliciousWellEntity))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            timeseriesService.deleteTimeseriesData(wellId, from, to)
        }

        assertEquals("Invalid table name.", exception.message)
        verifyNoInteractions(jdbcTemplate)
    }

    @Test
    fun `deleteAllTimeseriesData should call jdbcTemplate with correct parameters`() {
        val wellId = UUID.randomUUID()

        val mockedWellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, WellService.makeTableName(wellId))
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(mockedWellEntity))

        timeseriesService.deleteAllTimeseriesData(wellId)

        verify(jdbcTemplate).update(eq("DELETE FROM ${WellService.makeTableName(wellId)}"))
    }

    @Test
    fun `deleteAllTimeseriesData should throw exception if well not found`() {
        val wellId = UUID.randomUUID()

        val exception = assertThrows(NoSuchElementException::class.java) {
            timeseriesService.deleteAllTimeseriesData(wellId)
        }

        assertEquals("Well not found", exception.message)
        verifyNoInteractions(jdbcTemplate)
    }

    @Test
    fun `deleteAllTimeseriesData should prevent SQL injection in table name`() {
        val wellId = UUID.randomUUID()

        val maliciousWellEntity = WellEntity(
            wellId, "Well A", 1.0, 1.0, "malicious_table; DROP TABLE users;"
        )
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(maliciousWellEntity))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            timeseriesService.deleteAllTimeseriesData(wellId)
        }

        assertEquals("Invalid table name.", exception.message)
        verifyNoInteractions(jdbcTemplate)
    }
}