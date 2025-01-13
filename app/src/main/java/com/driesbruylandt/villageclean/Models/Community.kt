package com.driesbruylandt.villageclean.Models

data class Community(
    var id: String = "",
    val municipality: String = "",
    val name: String = "",
    var members: List<String> = listOf(),
    val maxMembers: Int = 0,
    val admin: String = "",
    var cleanedStreets: List<String> = listOf()
)
