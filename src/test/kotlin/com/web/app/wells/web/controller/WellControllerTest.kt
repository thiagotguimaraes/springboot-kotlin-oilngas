package com.web.app.wells.web.controller

import com.web.app.wells.application.service.WellService
import com.web.app.wells.web.dto.WellRequest
import com.web.app.wells.web.dto.WellResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.springframework.http.HttpStatus
import java.util.*

@ExtendWith(MockitoExtension::class)
class WellControllerTest {

    @Mock
    private lateinit var wellService: WellService

    private lateinit var wellController: WellController

    @Test
    fun `getAll should return a list of WellResponse when data exists`() {
        val wells = listOf(
            WellResponse(UUID.randomUUID(), "Well A", 1.0, 1.0, "timeseries_1"),
            WellResponse(UUID.randomUUID(), "Well B", 2.0, 2.0, "timeseries_2")
        )
        `when`(wellService.getAll()).thenReturn(wells)

        wellController = WellController(wellService)
        val result = wellController.getAll()

        assertEquals(2, result.size)
        assertEquals("Well A", result[0].name)
        assertEquals("Well B", result[1].name)
        verify(wellService).getAll()
    }

    @Test
    fun `getAll should return an empty list when no data exists`() {
        `when`(wellService.getAll()).thenReturn(emptyList())

        wellController = WellController(wellService)
        val result = wellController.getAll()

        assertTrue(result.isEmpty())
        verify(wellService).getAll()
    }

    @Test
    fun `create should return 200 OK with created WellResponse`() {
        val wellRequest = WellRequest("Well A", 1.0, 1.0)
        val wellResponse = WellResponse(UUID.randomUUID(), "Well A", 1.0, 1.0, "timeseries_1")
        `when`(wellService.create(anyOrNull())).thenReturn(wellResponse)

        wellController = WellController(wellService)
        val response = wellController.create(wellRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(wellResponse, response.body)
        val wellRequestCaptor = argumentCaptor<WellRequest>()
        verify(wellService).create(wellRequestCaptor.capture())
        assertEquals("Well A", wellRequestCaptor.firstValue.name)
    }

    @Test
    fun `delete should return 200 OK when well exists`() {
        val wellId = UUID.randomUUID()
        val wellResponse = WellResponse(wellId, "Well A", 1.0, 1.0, "timeseries_1")
        `when`(wellService.getWellById(wellId)).thenReturn(wellResponse)

        wellController = WellController(wellService)
        val response = wellController.delete(wellId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Well with ID $wellId deleted successfully.", response.body)
        verify(wellService).getWellById(wellId)
        verify(wellService).delete(wellId)
    }

    @Test
    fun `delete should return 400 BAD REQUEST when well does not exist`() {
        val wellId = UUID.randomUUID()
        `when`(wellService.getWellById(wellId)).thenReturn(null)

        wellController = WellController(wellService)
        val response = wellController.delete(wellId)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Well id: $wellId not found", response.body)
        verify(wellService).getWellById(wellId)
        verifyNoMoreInteractions(wellService)
    }

    @Test
    fun `getWellById should return 200 OK when well exists`() {
        val wellId = UUID.randomUUID()
        val wellResponse = WellResponse(wellId, "Well A", 1.0, 1.0, "timeseries_1")
        `when`(wellService.getWellById(wellId)).thenReturn(wellResponse)

        wellController = WellController(wellService)
        val response = wellController.getWellById(wellId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(wellResponse, response.body)
        verify(wellService).getWellById(wellId)
    }

    @Test
    fun `getWellById should return 404 NOT FOUND when well does not exist`() {
        val wellId = UUID.randomUUID()
        `when`(wellService.getWellById(wellId)).thenReturn(null)

        wellController = WellController(wellService)
        val response = wellController.getWellById(wellId)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
        verify(wellService).getWellById(wellId)
    }
}