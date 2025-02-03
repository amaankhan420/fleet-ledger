package com.example.fleetapp.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class FormData(
    val vehicleNumber: String,
    val driverName: String,
    val amount: Float,
    val incentive: Float = 0f,
    var commission: Float = 0f
)
