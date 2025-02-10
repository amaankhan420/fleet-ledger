package com.example.fleetapp.dataclasses

import kotlinx.serialization.Serializable


@Serializable
data class FormData(
    val vehicleNumber: String,
    val driverName: String,
    val amount: Float,
    val incentive: Float = 0f
)

@Serializable
data class PartnerMetaData(
    var commission: Float = 0f,
    val remarks: String = ""
)