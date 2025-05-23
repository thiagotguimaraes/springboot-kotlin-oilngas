package com.web.app.wells.application.service

import com.web.app.utils.Json
import com.web.app.wells.persistence.WellBoundariesEntity
import com.web.app.wells.persistence.WellBoundariesRepository
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
    private lateinit var wellBoundariesRepository: WellBoundariesRepository

    @Mock
    private lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var wellService: WellService

    @Captor
    private lateinit var sqlCaptor: ArgumentCaptor<String>

    @BeforeEach
    fun setup() {
        wellService = WellService(wellRepository, wellBoundariesRepository, jdbcTemplate)
    }

    // Test cases for getAll()
    @Test
    fun `getAll should return a list of WellResponse when data exists`() {
        val wellEntities = listOf(
            WellEntity(UUID.randomUUID(), "Well A", 1.0, 1.0, "well_a_timeseries"),
            WellEntity(UUID.randomUUID(), "Well B", 2.0, 2.0, "well_b_timeseries")
        )

        val wellBoundariesEntities = listOf(
            WellBoundariesEntity(wellEntities[0].id, 1000L, 2000L),
            WellBoundariesEntity(wellEntities[1].id, 3000L, 4000L)
        )

        `when`(wellRepository.findAll()).thenReturn(wellEntities)
        `when`(wellBoundariesRepository.findAll()).thenReturn(wellBoundariesEntities)

        val result = wellService.getAll()

        verify(wellRepository).findAll()
        verify(wellBoundariesRepository).findAll()

        assertEquals(2, result.size)
        assertEquals(
            "{\n" +
                    "  \"id\": \"${wellEntities[0].id}\",\n" +
                    "  \"name\": \"Well A\",\n" +
                    "  \"latitude\": 1.0,\n" +
                    "  \"longitude\": 1.0,\n" +
                    "  \"collection\": \"well_a_timeseries\",\n" +
                    "  \"startMs\": 1000,\n" +
                    "  \"endMs\": 2000\n" +
                    "}", Json.toJson(result[0])
        )
        assertEquals(
            "{\n" +
                    "  \"id\": \"${wellEntities[1].id}\",\n" +
                    "  \"name\": \"Well B\",\n" +
                    "  \"latitude\": 2.0,\n" +
                    "  \"longitude\": 2.0,\n" +
                    "  \"collection\": \"well_b_timeseries\",\n" +
                    "  \"startMs\": 3000,\n" +
                    "  \"endMs\": 4000\n" +
                    "}", Json.toJson(result[1])
        )
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
        val wellBoundariesEntity = WellBoundariesEntity(wellId, 1000L, 2000L)

        `when`(wellRepository.findById(wellId)).thenReturn(Optional.of(wellEntity))
        `when`(wellBoundariesRepository.findById(wellId)).thenReturn(Optional.of(wellBoundariesEntity))

        val result = wellService.getWellById(wellId)

        verify(wellRepository).findById(wellId)
        verify(wellBoundariesRepository).findById(wellId)

        assertNotNull(result)
        assertEquals(
            "{\n" +
                    "  \"id\": \"${wellId}\",\n" +
                    "  \"name\": \"Well A\",\n" +
                    "  \"latitude\": 1.0,\n" +
                    "  \"longitude\": 1.0,\n" +
                    "  \"collection\": \"well_a_timeseries\",\n" +
                    "  \"startMs\": 1000,\n" +
                    "  \"endMs\": 2000\n" +
                    "}", Json.toJson(result)
        )
    }

    @Test
    fun `getWellById should not throw exception when well does not exist`() {
        val wellId = UUID.randomUUID()
        `when`(wellRepository.findById(wellId)).thenReturn(Optional.empty())

        assertDoesNotThrow {
            wellService.getWellById(wellId)
        }
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
        verifyNoInteractions(wellBoundariesRepository)
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

        val result = WellService.toDto(wellEntity, null, null)

        assertEquals("Well A", result.name)
        assertEquals(1.0, result.latitude)
        assertEquals(1.0, result.longitude)
    }
}