package com.stockagecloud.app.data.remote

import com.stockagecloud.app.data.model.AuthRequest
import com.stockagecloud.app.data.model.AuthResponse
import com.stockagecloud.app.data.model.ListeFichiersResponse
import com.stockagecloud.app.data.model.QuotaResponse
import com.stockagecloud.app.data.model.TelechargementResponse
import com.stockagecloud.app.data.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/inscription")
    suspend fun inscription(@retrofit2.http.Body corps: AuthRequest): Response<AuthResponse>

    @POST("api/auth/connexion")
    suspend fun connexion(@retrofit2.http.Body corps: AuthRequest): Response<AuthResponse>

    @GET("api/fichiers")
    suspend fun listerFichiers(@Query("categorie") categorie: String? = null): Response<ListeFichiersResponse>

    @Multipart
    @POST("api/fichiers/upload")
    suspend fun uploader(@Part fichier: MultipartBody.Part): Response<UploadResponse>

    @GET("api/fichiers/{id}/telecharger")
    suspend fun obtenirLienTelechargement(@Path("id") id: String): Response<TelechargementResponse>

    @DELETE("api/fichiers/{id}")
    suspend fun supprimerFichier(@Path("id") id: String): Response<Unit>

    @GET("api/fichiers/quota")
    suspend fun obtenirQuota(): Response<QuotaResponse>
}
