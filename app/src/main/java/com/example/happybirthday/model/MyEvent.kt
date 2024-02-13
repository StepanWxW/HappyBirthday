package com.example.happybirthday.model

data class MyEvent(
    var id: Int? = null,
    var uid: String,
    var firstName: String,
    var lastName: String,
    var patronymic: String,
    var telephone: Long,
    var year: Long,
    var month: Long,
    var day: Long,
    var hour: Long,
) {
    constructor(uid: String) : this(
        id = 0,
        uid = uid,
        firstName = "",
        lastName = "",
        patronymic = "",
        telephone = 0,
        year = 0,
        month = 0,
        day = 0,
        hour = 0
    )
}