package com.example.fleetapp.routes

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {
    @Serializable
    data object Form : Routes()

    @Serializable
    data object ListOfEntry : Routes()

    @Serializable
    data object PDF : Routes()

    @Serializable
    data object Commission : Routes()
}
