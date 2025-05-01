package com.web.app.wells.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface WellBoundariesRepository : JpaRepository<WellBoundariesEntity, UUID>