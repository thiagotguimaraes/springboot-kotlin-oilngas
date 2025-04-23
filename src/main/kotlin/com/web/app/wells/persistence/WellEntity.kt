package com.web.app.wells.persistence

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "wells")
data class WellEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String,

    val latitude: Double,
    
    val longitude: Double,

    @Column(nullable = false)
    val collection: String,

    val startMs: Long? = null,

    val endMs: Long? = null
)
