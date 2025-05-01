package com.web.app.wells.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "well_boundaries")
data class WellBoundariesEntity(
    @Id
    val wellId: UUID,

    val startMs: Long?,

    val endMs: Long?
)