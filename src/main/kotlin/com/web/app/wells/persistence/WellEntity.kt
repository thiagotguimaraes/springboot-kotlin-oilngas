package com.web.app.wells.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
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
)
