package com.web.app.wells.web.controller

import com.web.app.wells.application.service.TimeseriesService
import com.web.app.wells.domain.model.TimeseriesPoint
import com.web.app.wells.web.dto.TimeseriesInsertRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.springframework.http.HttpStatus
import java.util.*

@ExtendWith(MockitoExtension::class)
class TimeseriesControlerTest {

    @Mock
    private lateinit var timeseriesService: TimeseriesService

    private lateinit var timeseriesControler: TimeseriesControler

    @Test
    fun `insertData should call TimeseriesService and return 200 OK`() {
        val wellId = UUID.randomUUID()
        val request = TimeseriesInsertRequest(1234567890L, 20.0, 30.0, 10.0)

        timeseriesControler = TimeseriesControler(timeseriesService)
        val response = timeseriesControler.insertData(wellId, request)

        val wellIdCaptor = argumentCaptor<UUID>()
        val requestCaptor = argumentCaptor<TimeseriesInsertRequest>()
        verify(timeseriesService).insertData(wellIdCaptor.capture(), requestCaptor.capture())

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(wellId, wellIdCaptor.firstValue)
        assertEquals(1234567890L, requestCaptor.firstValue.timestamp)
    }

    @Test
    fun `insertData should return 404 NOT FOUND when well is not found`() {
        val wellId = UUID.randomUUID()
        val request = TimeseriesInsertRequest(1234567890L, 20.0, 30.0, 10.0)

        doThrow(NoSuchElementException("Well not found")).`when`(timeseriesService).insertData(any(), any())

        timeseriesControler = TimeseriesControler(timeseriesService)
        val exception = assertThrows(NoSuchElementException::class.java) {
            timeseriesControler.insertData(wellId, request)
        }

        assertEquals("Well not found", exception.message)
        verify(timeseriesService).insertData(any(), any())
    }

    @Test
    fun `insertDataBatch should call TimeseriesService and return 200 OK`() {
        val wellId = UUID.randomUUID()
        val requests = listOf(
            TimeseriesInsertRequest(1234567890L, 20.0, 30.0, 10.0),
            TimeseriesInsertRequest(1234567891L, 21.0, 31.0, 11.0)
        )

        timeseriesControler = TimeseriesControler(timeseriesService)
        val response = timeseriesControler.insertDataBatch(wellId, requests)

        val wellIdCaptor = argumentCaptor<UUID>()
        val requestCaptor = argumentCaptor<List<TimeseriesInsertRequest>>()
        verify(timeseriesService).insertDataBatch(wellIdCaptor.capture(), requestCaptor.capture())

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(wellId, wellIdCaptor.firstValue)
        assertEquals(requests, requestCaptor.firstValue)
    }

    @Test
    fun `insertDataBatch should return 400 BAD REQUEST when request list is empty`() {
        val wellId = UUID.randomUUID()
        val requests = emptyList<TimeseriesInsertRequest>()

        doThrow(IllegalArgumentException("Request list cannot be empty")).`when`(timeseriesService)
            .insertDataBatch(any(), any())

        timeseriesControler = TimeseriesControler(timeseriesService)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            timeseriesControler.insertDataBatch(wellId, requests)
        }

        assertEquals("Request list cannot be empty", exception.message)
    }

    @Test
    fun `getTimeseries should call TimeseriesService and return 200 OK with data`() {
        val wellId = UUID.randomUUID()
        val from = 1234567890L
        val to = 1234567990L
        val timeseriesPoints = listOf(
            TimeseriesPoint(1234567890L, 20.0, 30.0, 10.0), TimeseriesPoint(1234567891L, 21.0, 31.0, 11.0)
        )

        `when`(timeseriesService.getTimeseries(any(), any(), any())).thenReturn(timeseriesPoints)

        timeseriesControler = TimeseriesControler(timeseriesService)
        val response = timeseriesControler.getTimeseries(wellId, from, to)

        val wellIdCaptor = argumentCaptor<UUID>()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(timeseriesPoints, response.body)
        verify(timeseriesService).getTimeseries(wellIdCaptor.capture(), eq(from), eq(to))
        assertEquals(wellId, wellIdCaptor.firstValue)
    }

    @Test
    fun `getTimeseries should return 404 NOT FOUND when well is not found`() {
        val wellId = UUID.randomUUID()
        val from = 1234567890L
        val to = 1234567990L

        `when`(
            timeseriesService.getTimeseries(
                any(), any(), any()
            )
        ).thenThrow(NoSuchElementException("Well not found"))

        timeseriesControler = TimeseriesControler(timeseriesService)
        val exception = assertThrows(NoSuchElementException::class.java) {
            timeseriesControler.getTimeseries(wellId, from, to)
        }

        val wellIdCaptor = argumentCaptor<UUID>()

        assertEquals("Well not found", exception.message)
        verify(timeseriesService).getTimeseries(wellIdCaptor.capture(), eq(from), eq(to))
        assertEquals(wellId, wellIdCaptor.firstValue)
    }
}