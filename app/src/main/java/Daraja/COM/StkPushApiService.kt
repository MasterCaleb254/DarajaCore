package daraja.com.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import daraja.com.model.StkPushRequest

interface StkPushApiService {
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("mpesa/stkpush/v1/processrequest")
    fun sendStkPush(@Body request: StkPushRequest): Call<Void>
}


