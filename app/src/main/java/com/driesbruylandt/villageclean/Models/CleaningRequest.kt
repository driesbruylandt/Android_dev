package com.driesbruylandt.villageclean.Models

import java.util.Date

data class CleaningRequest(
    val id: String = "",
    val streetName: String = "",
    val municipality: String = "",
    val requestedAt: Date = Date(),
    val status: String = "pending",
    val adminId: String = "",
    val userId: String = "",
)