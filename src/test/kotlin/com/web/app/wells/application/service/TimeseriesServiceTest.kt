package com.web.app.wells.application.service

import com.web.app.wells.domain.model.TimeseriesPoint
import com.web.app.wells.persistence.WellEntity
import com.web.app.wells.persistence.WellRepository
import com.web.app.wells.web.dto.TimeseriesInsertRequest
import org.apache.coyote.BadRequestException
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
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
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

        val mockedWellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, "well_a_timeseries", 1L, 1L)
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(mockedWellEntity))

        timeseriesService.insertData(wellId, request)

        verify(jdbcTemplate).update(
            eq("INSERT INTO well_a_timeseries (timestamp, pressure, oil_rate, temperature) VALUES (?, ?, ?, ?)"),
            eq(1234567890L), eq(20.0), eq(30.0), eq(10.0)
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

        val mockedWellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, "well_a_timeseries", 1L, 1L)
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(mockedWellEntity))

        timeseriesService.insertDataBatch(wellId, points)

        val argumentCaptorSqlQuery = argumentCaptor<String>()
        val argumentCaptorBatchArgs = argumentCaptor<List<Array<Any>>>()

        verify(jdbcTemplate).batchUpdate(
            argumentCaptorSqlQuery.capture(),
            argumentCaptorBatchArgs.capture()
        )

        assertEquals(
            """
                INSERT INTO well_a_timeseries (timestamp, pressure, oil_rate, temperature)
                VALUES (?, ?, ?, ?)
                """.trimIndent(),
            argumentCaptorSqlQuery.firstValue
        )

        val expectedBatchArgs = listOf(
            arrayOf(1234567890L, 101.3, 25.5, 20.0),
            arrayOf(1234567891L, 102.0, 26.0, 21.0)
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
    fun `insertDataBatch should not call jdbcTemplate if batch is empty`() {
        val wellId = UUID.randomUUID()
        val points = emptyList<TimeseriesInsertRequest>()

        val exception = assertThrows(BadRequestException::class.java) {
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
            TimeseriesPoint(1234567890L, 101.3, 25.5, 20.0),
            TimeseriesPoint(1234567891L, 102.0, 26.0, 21.0)
        )

        val mockedWellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, "well_a_timeseries", 1L, 1L)
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(mockedWellEntity))
        `when`(
            jdbcTemplate.query(
                anyString(),
                any<RowMapper<TimeseriesPoint>>(),
                eq(startTime),
                eq(endTime)
            )
        ).thenReturn(expectedData)

        val result = timeseriesService.getTimeseries(wellId, startTime, endTime)

        val expectedPoints = expectedData.map { TimeseriesPoint(it.timestamp, it.pressure, it.oilRate, it.temperature) }
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
    fun `getTimeseries should return empty list if no data in range`() {
        val wellId = UUID.randomUUID()
        val startTime = 1234567890L
        val endTime = 1234567990L

        val mockedWellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, "well_a_timeseries", 1L, 1L)
        `when`(wellRepository.findById(eq(wellId))).thenReturn(Optional.of(mockedWellEntity))
        `when`(
            jdbcTemplate.query(
                anyString(),
                any<RowMapper<TimeseriesPoint>>(),
                eq(startTime),
                eq(endTime)
            )
        ).thenReturn(emptyList())

        val result = timeseriesService.getTimeseries(wellId, startTime, endTime)

        assertTrue(result.isEmpty())
    }
}