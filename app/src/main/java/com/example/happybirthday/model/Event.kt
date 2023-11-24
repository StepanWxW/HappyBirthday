package com.example.happybirthday.model

import java.time.LocalDate

data class Event(
    val eventId: String,
    val eventName: String,
    val eventDate: LocalDate
)