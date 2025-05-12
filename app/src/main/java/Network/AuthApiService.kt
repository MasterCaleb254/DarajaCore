package daraja.COM.network

import daraja.COM.model.AuthResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface AuthApiService {
    @GET("oauth/v1/generate?grant_type=client_credentials")
    fun getAccessToken(@Header("Authorization") authHeader: String): Call<AuthResponse>
}

