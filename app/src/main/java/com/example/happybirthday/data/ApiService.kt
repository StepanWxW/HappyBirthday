package com.example.happybirthday.data


import com.example.happybirthday.model.MyEvent
import com.example.happybirthday.model.MyStatus
import com.example.happybirthday.model.Question
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("getAll/")
    suspend fun getAll(
        @Query("uid") uid: String
    ): List<MyEvent>

    @POST("event/")
    suspend fun postEvent(
        @Body myEvent: MyEvent,
    ): Response<MyStatus>

    @PATCH("update/")
    suspend fun patchEvent(
        @Body myEvent: MyEvent,
    ): Response<MyStatus>

    @DELETE("/delete/")
    suspend fun deleteEvent(
        @Query("uid") uid: String,
        @Query("id") id: Int,
    ): Response<MyStatus>

    @POST("token/")
    suspend fun postToken(
        @Query("uid") uid: String,
        @Query("token") token: String,
    ): Response<MyStatus>

    @GET("questions/")
    suspend fun getQuestions(
    ): List<Question>

    @DELETE("/delete_token/")
    suspend fun deleteToken(
        @Query("uid") uid: String,
    ): Response<MyStatus>
}