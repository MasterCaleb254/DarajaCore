package daraja.COM

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import daraja.COM.model.AuthResponse
import daraja.COM.network.ApiClient
import daraja.COM.network.AuthApiService
import daraja.com.model.StkPushRequest
import daraja.com.network.StkPushApiService

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val consumerKey = "7NRRCvTmNkwxDmx19BGqJLmvYH02dC7Bapj3WCAoHqjjV0LQ"
    private val consumerSecret ="zGVqDF4lxhTzPbOovlGkHMKDrOm2YH2F0iSUBnROOrAFRn0gHuK203y06d48zGdX"
    private val passkey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
    private val businessShortCode = "174379"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val authButton = findViewById<Button>(R.id.authButton)
        val stkButton = findViewById<Button>(R.id.stkPushButton)
        val tokenText = findViewById<TextView>(R.id.tokenText)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val payButton = findViewById<Button>(R.id.payButton)

        authButton.setOnClickListener {
            getAccessToken { token ->
                tokenText.text = "Access Token: $token"
                stkButton.isEnabled = token != null
            }
        }

        stkButton.setOnClickListener {
            Toast.makeText(this, "STK Push coming soon!", Toast.LENGTH_SHORT).show()
        }

        payButton.setOnClickListener {
            val phone = phoneInput.text.toString().trim()
            val amount = amountInput.text.toString().trim()

            if (phone.isNotEmpty() && amount.isNotEmpty()) {
                getAccessTokenAndTriggerStk(phone, amount)
            } else {
                Toast.makeText(this, "Enter phone and amount", Toast.LENGTH_SHORT).show()
            }
        }

        getAccessToken { token ->
            Log.d("AccessToken", "Startup token: $token")
        }
    }

    private fun getAccessToken(callback: (String?) -> Unit) {
        val credentials = "$consumerKey:$consumerSecret"
        val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        val apiService = ApiClient.retrofit.create(AuthApiService::class.java)
        val call = apiService.getAccessToken(auth)

        call.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.access_token
                    Log.d("AccessToken", "Token: $token")
                    callback(token)
                } else {
                    Log.e("AccessToken", "Failed: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Log.e("AccessToken", "Error: ${t.message}")
                callback(null)
            }
        })
    }

    private fun getAccessTokenAndTriggerStk(phone: String, amount: String) {
        getAccessToken { token ->
            if (token != null) {
                initiateStkPush(token, phone, amount)
            } else {
                Toast.makeText(this, "Failed to get access token", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initiateStkPush(accessToken: String, phoneNumber: String, amount: String) {
        val timestamp = getTimestamp()
        val password = generatePassword(businessShortCode, passkey, timestamp)

        val request = StkPushRequest(
            BusinessShortCode = businessShortCode,
            Password = password,
            Timestamp = timestamp,
            TransactionType = "CustomerPayBillOnline",
            Amount = amount,
            PartyA = phoneNumber,
            PartyB = businessShortCode,
            PhoneNumber = phoneNumber,
            CallBackURL =  "https://mydummy.callback.com/stkpush",

                    AccountReference = "Pesify",
            TransactionDesc = "Payment"
        )

        val api = ApiClient.retrofit.create(StkPushApiService::class.java)
        val call = api.sendStkPush(request)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("STKPush", "Push initiated successfully.")
                } else {
                    Log.e("STKPush", "Failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("STKPush", "Error: ${t.message}")
            }
        })
    }

    private fun getTimestamp(): String {
        val timeFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return timeFormat.format(Date())
    }

    private fun generatePassword(businessShortCode: String, passkey: String, timestamp: String): String {
        val strToEncode = businessShortCode + passkey + timestamp
        return Base64.encodeToString(strToEncode.toByteArray(), Base64.NO_WRAP)
    }
}
