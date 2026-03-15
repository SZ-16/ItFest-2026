package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Waypoint(
    val id: Int? = null,
    val name: String,
    val category: String,
    val description: String,
    val lat: Double,
    val lng: Double,
    val hasRamp: Boolean,
    val hasElevator: Boolean,
    val isAccessible: Boolean = true
)