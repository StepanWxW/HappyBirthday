package com.example.happybirthday.data


import com.example.happybirthday.model.MyEvent
import com.example.happybirthday.model.MyStatus
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
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
//
//    @GET("streams/")
//    suspend fun getStreams(
//        @Query("is_active") active: Boolean = true,
//    ): ResponseDto
//
//    @GET("streams/")
//    suspend fun getStreamsSearch(
//        @Query("is_active") active: Boolean = true,
//        @Query("name") name: String,
//        @Query("page") page: Int,
//    ): ResponseDto
//
//    @GET("streams/")
//    suspend fun getStreams(
//        @Query("is_active") active: Boolean = true,
//        @Query("user_id") id: Int,
//        @Query("limit") limit: Int = 1000,
//    ): ResponseDto
//
//    @GET("streams/")
//    suspend fun getLikeStreams(
//        @Query("is_active") active: Boolean = true,
////        @Query("user_id") id: Int,
//        @Query("limit") limit: Int = 1000,
//        @Query("liked") like: Boolean = true,
//    ): ResponseDto
//
//    @GET("streams/{id}/")
//    suspend fun getStream(
//        @Path("id") id: Int
//    ): ResponseDto
//    @GET("streams/")
//    suspend fun getStreamsPagination(
//        @Query("is_active") active: Boolean,
//        @Query("page") page: Int
//    ): ResponseDto
//
//    @GET("playlists/")
//    suspend fun getPlaylists(
//        @Query("exclude_empty") empty: Boolean = true,
//        @Query("recomendations") recomendations: Boolean = false,
//    ): ResponseDto
//
//    @GET("playlists/{id}/")
//    suspend fun getPlaylist(
//        @Path("id") id: Int,
//        @Query("limit") limit: Int = 1000,
//    ): ResponseDto
//
//    @GET("playlists/")
//    suspend fun getPlaylistsSearch(
//        @Query("name") name: String,
//        @Query("page") page: Int,
//    ): ResponseDto
//
//    @GET("albums/{id}/")
//    suspend fun getAlbum(
//        @Path("id") id: Int,
//        @Query("limit") limit: Int = 1000,
//    ): ResponseDto
//
//
//    @GET("playlists/")
//    suspend fun getPlaylists(
//        @Query("user_id") id: Int,
//        @Query("limit") limit: Int = 1000,
//    ): ResponseDto
//
//    @GET("playlists/")
//    suspend fun getLikePlaylists(
////        @Query("user_id") id: Int,
//        @Query("limit") limit: Int = 1000,
//        @Query("liked") like: Boolean = true,
////        @Query("orderid") order: Int = 40,
//    ): ResponseDto
//
//    @GET("playlists/")
//    suspend fun getPlaylistsGenres(
//        @Query("genres") ids: String
//    ): ResponseDto
//
//    @GET("playlists/{playlistId}/tracks/")
//    suspend fun getTracksOfPlaylist(
//        @Path("playlistId") playlistId: String,
//        @Query("limit") limit: Int = 1000
//    ): ResponseDto
//
//    @GET("playlists/")
//    suspend fun getPlaylistsPagination(
//        @Query("is_active") active: Boolean,
//        @Query("page") page: Int
//    ): ResponseDto
//
//    @GET("playlists/")
//    suspend fun getPlaylistsPaginationGenres(
//        @Query("is_active") active: Boolean,
//        @Query("page") page: Int,
//        @Query("genres") ids: String
//    ): ResponseDto
//
//    @GET("tracks/")
//    suspend fun getTracks(
//        @Query("limit") limit: Int,
//        @Query("page") page: Int
//    ): ResponseDto
//
//    @GET("tracks/")
//    suspend fun getTracksRecomend(
//        @Query("limit") limit: Int = 10,
//        @Query("recomendations") recomendations: Boolean = true,
//    ): ResponseDto
//
//    @GET("tracks/")
//    suspend fun getTracks(
//        @Query("limit") limit: Int = 1000,
//        @Query("page") page: Int,
//        @Query("user_id") userId: Int
//    ): ResponseDto
//
//    @GET("tracks/")
//    suspend fun getLikeTracks(
//        @Query("limit") limit: Int = 1000,
////        @Query("page") page: Int,
//        @Query("liked") liked: Boolean = true,
//        @Query("order") order: String = "date_like,DESC"
//    ): ResponseDto
//
//    @GET("tracks/")
//    suspend fun getTracksSearch(
//        @Query("limit") limit: Int = 500,
//        @Query("page") page: Int,
////        @Query("user_id") userId: Int
//        @Query("fullname") name: String
//    ): ResponseDto
//
//    @GET("tracks/")
//    suspend fun getTracksPagination(
//        @Query("limit") limit: Int,
//        @Query("page") page: Int
//    ): ResponseDto
//
//    @GET("tracks/{id}/")
//    suspend fun getTrackInfo(
//        @Path("id") id: Int
//    ): ResponseDto
//
//    @GET("genres/groups/")
//    suspend fun getGenres(): ResponseDto
//
//    @GET("genres/groups/{id}/")
//    suspend fun getGenresId(
//        @Path("id") id: Int
//    ): ResponseDto
//
//    @GET("albums/")
//    suspend fun getAlbums(): ResponseDto
//
//    @GET("albums/")
//    suspend fun getAlbumsSearch(
//        @Query("name") name: String,
//        @Query("page") page: Int,
//    ): ResponseDto
//
//    @GET("albums/{album}/tracks/")
//    suspend fun getTracksOfAlbum(
//        @Path("album") album: String
//    ): ResponseDto
//
//    @GET("albums/")
//    suspend fun getAlbumsPagination(
//        @Query("page") page: String
//    ): ResponseDto
//
//    @POST("user/auth/")
//    suspend fun postAuthorization(
//        @Query("email") email: String,
//        @Query("password") password: String,
//        @Query("long_auth") long_auth: Boolean = true
//    ): Response<ResponseDto>
//
//    @GET("profiles/{id}/")
//    suspend fun getProfile(
//        @Path("id") id: Int
//    ): ResponseDto
//
//    @GET("user/auth/refresh/")
//    suspend fun getToken(
//        @Query("user_id") id: Int,
//        @Query("refresh_token") refreshToken: String
//    ): Response<ResponseDto>
//    @GET("user/auth/refresh/")
//    fun getTokenCallBack(
//        @Query("user_id") id: Int,
//        @Query("refresh_token") refreshToken: String
//    ): Call<ResponseDto>
//
//    @GET("likes/tracks/")
//    suspend fun getLikeTracks(
//        @Query("tracks_id[]") idTracks: List<Int>
//    ): ResponseDto
//
//    @POST("likes/streams/{id}/")
//    suspend fun postLikeStream(
//        @Path("id") id: Int
//    ): ResponseDto
//
//    @POST("likes/playlists/{id}/")
//    suspend fun postLikePlaylist(
//        @Path("id") id: Int
//    ): ResponseDto
//
//    @POST("likes/tracks/{id}/")
//    suspend fun postLikeTrack(
//        @Path("id") id: Int
//    ): ResponseDto
//
//    @POST("likes/profiles/{id}/")
//    suspend fun postLikeUser(
//        @Path("id") id: Int
//    ): ResponseDto
//
//    @POST("likes/albums/{id}/")
//    suspend fun postLikeAlbum(
//        @Path("id") id: Int
//    ): ResponseDto
//
//    @POST("user/logout/")
//    suspend fun postExit(
//    ): StatusDTO
//
//    @POST("user/lostpassword/")
//    suspend fun postLostPassword(
//        @Query("email") email: String
//    ): StatusDTO
//
//    @POST("user/registration/")
//    suspend fun postRegistration(
//        @Query("email") login: String,
//        @Query("password") password: String,
//        @Query("password2") password2: String,
//    ): Response<StatusDTO>
//
//    @Multipart
//    @POST("upload/image/")
//    fun uploadImageAvatar(
//        @Part("type_id") typeIdBody: RequestBody?,
//        @Part image: MultipartBody.Part?,
//    ): Call<ResponseDto>
//
//    @PATCH("user/")
//    suspend fun patchImage(
//        @Query("avatar") avatar: Int,
//    ): StatusDTO
//
//    @PATCH("user/")
//    suspend fun patchImageCover(
//        @Query("cover") cover: Int,
//    ): StatusDTO
//
//    @PATCH("user/")
//    suspend fun patchUserName(
//        @Query("name") name: String,
//    ): Response<StatusDTO>
}