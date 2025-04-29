package com.web.app.wells.application.service

import com.web.app.wells.persistence.WellEntity
import com.web.app.wells.persistence.WellRepository
import com.web.app.wells.web.dto.WellRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.secondValue
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*

@ExtendWith(MockitoExtension::class)
class WellServiceTest {

    @Mock
    private lateinit var wellRepository: WellRepository

    @Mock
    private lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var wellService: WellService

    @Captor
    private lateinit var sqlCaptor: ArgumentCaptor<String>

    @BeforeEach
    fun setup() {
        wellService = WellService(wellRepository, jdbcTemplate)
    }

    // Test cases for getAll()
    @Test
    fun `getAll should return a list of WellResponse when data exists`() {
        val wellEntities = listOf(
            WellEntity(UUID.randomUUID(), "Well A", 1.0, 1.0, "well_a_timeseries"),
            WellEntity(UUID.randomUUID(), "Well B", 2.0, 2.0, "well_b_timeseries")
        )
        `when`(wellRepository.findAll()).thenReturn(wellEntities)

        val result = wellService.getAll()

        assertEquals(2, result.size)
        assertEquals("Well A", result[0].name)
        assertEquals("Well B", result[1].name)
    }

    @Test
    fun `getAll should return an empty list when no data exists`() {
        `when`(wellRepository.findAll()).thenReturn(emptyList())

        val result = wellService.getAll()

        assertTrue(result.isEmpty())
    }

    // Test cases for getWellById()
    @Test
    fun `getWellById should return WellResponse when well exists`() {
        val wellId = UUID.randomUUID()
        val wellEntity = WellEntity(wellId, "Well A", 1.0, 1.0, "well_a_timeseries")
        `when`(wellRepository.findById(wellId)).thenReturn(Optional.of(wellEntity))

        val result = wellService.getWellById(wellId)

        assertNotNull(result)
        assertEquals("Well A", result?.name)
    }

    @Test
    fun `getWellById should return throw exception when well does not exist`() {
        val wellId = UUID.randomUUID()
        `when`(wellRepository.findById(wellId)).thenReturn(Optional.empty())

        val exception = assertThrows(NoSuchElementException::class.java) {
            wellService.getWellById(wellId)
        }

        assertEquals(exception.message, "Well not found")
    }

    // Test cases for create()
    @Test
    fun `create should save a new WellEntity and return WellResponse`() {
        val wellRequest = WellRequest("Well A", 1.0, 1.0)
        val wellEntity = WellEntity(UUID.randomUUID(), "Well A", 1.0, 1.0, "well_a_timeseries")
        `when`(wellRepository.save(any(WellEntity::class.java))).thenReturn(wellEntity)

        val result = wellService.create(wellRequest)

        assertNotNull(result)
        assertEquals("Well A", result.name)
        verify(wellRepository).save(any(WellEntity::class.java))
    }

    @Test
    fun `create should execute correct SQL commands`() {
        val wellRequest = WellRequest("Well A", 1.0, 1.0)
        val wellEntity = WellEntity(UUID.randomUUID(), "Well A", 1.0, 1.0, "well_a_timeseries")
        `when`(wellRepository.save(any(WellEntity::class.java))).thenReturn(wellEntity)

        wellService.create(wellRequest)

        verify(jdbcTemplate, times(2)).execute(sqlCaptor.capture())

        assertEquals(
            sqlCaptor.firstValue,
            "CREATE TABLE IF NOT EXISTS well_a_timeseries (LIKE timeseries_template INCLUDING ALL);"
        )

        assertEquals(
            sqlCaptor.secondValue,
            "SELECT create_hypertable('well_a_timeseries', 'timestamp', if_not_exists => TRUE);"
        )
    }

    // Test cases for delete()
    @Test
    fun `delete should delete the well when it exists`() {
        val wellId = UUID.randomUUID()

        wellService.delete(wellId)

        verify(wellRepository).deleteById(wellId)
    }

    @Test
    fun `delete should not throw exception when well does not exist`() {
        val wellId = UUID.randomUUID()
        doNothing().`when`(wellRepository).deleteById(wellId)

        assertDoesNotThrow { wellService.delete(wellId) }
    }

    // Test cases for WellEntity.toDto()
    @Test
    fun `WellEntity toDto should map WellEntity to WellResponse`() {
        val wellEntity = WellEntity(UUID.randomUUID(), "Well A", 1.0, 1.0, "well_a_timeseries")

        val result = WellService.toDto(wellEntity)

        assertEquals("Well A", result.name)
        assertEquals(1.0, result.latitude)
        assertEquals(1.0, result.longitude)
    }
}