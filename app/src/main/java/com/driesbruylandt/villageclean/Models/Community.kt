package com.driesbruylandt.villageclean.Models

data class Community(
    val id: String = "",
    val municipality: String = "",
    val name: String = "",
    val members: List<String> = listOf(),
    val maxMembers: Int = 0,
    val admin: String = "",
)
