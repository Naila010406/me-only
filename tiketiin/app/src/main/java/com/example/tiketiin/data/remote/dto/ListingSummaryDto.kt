package com.example.tiketiin.data.remote.dto

data class ListingSummaryDto(
    val id: Int,
    val price: Long,
    val quantity: Int,
    val status: String
)