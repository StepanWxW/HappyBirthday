package com.example.happybirthday.model

import com.google.gson.annotations.SerializedName


data class MyStatus (
    @SerializedName ("status")
    val status: Boolean
        )