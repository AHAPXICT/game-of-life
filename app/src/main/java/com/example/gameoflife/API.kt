package com.example.gameoflife

import android.util.Log
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpldmZqem51ZWxvZXlld3Z5eXFzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE5MjQ4NTMsImV4cCI6MjA4NzUwMDg1M30.lMumupYe7_LbGPaOw4U9z6ild3wUU9Va3Vn38ZDOO4U"
const val API_URL = "https://jevfjznueloeyewvyyqs.supabase.co/functions/v1"
const val JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpldmZqem51ZWxvZXlld3Z5eXFzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE5MjQ4NTMsImV4cCI6MjA4NzUwMDg1M30.lMumupYe7_LbGPaOw4U9z6ild3wUU9Va3Vn38ZDOO4U"

class API {
    companion object {
        var token: String? = null
        // Function to handle all HTTP methods
        fun callApi(
            requestUrl: String,
            httpMethod: HttpMethod = HttpMethod.GET,
            requestModel: Any? = null
        ): String {

            val response = StringBuilder()

            try {
                val url = URL(API_URL + requestUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = httpMethod.name

                // Headers
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("apikey", API_KEY)

                // 👉 ВСТАВЛЯЕМ JWT СЮДА
                val bearerToken = JWT
                connection.setRequestProperty("Authorization", "Bearer $bearerToken")

                // 👉 BODY ВСТАВЛЯЕТСЯ СЮДА
                if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT) {
                    connection.doOutput = true
                    requestModel?.let {
                        val jsonInput = Gson().toJson(it)
                        OutputStreamWriter(connection.outputStream).use { os ->
                            os.write(jsonInput)
                            os.flush()
                        }
                    }
                }

                val responseCode = connection.responseCode

                val stream = if (responseCode in 200..299)
                    connection.inputStream
                else
                    connection.errorStream

                BufferedReader(InputStreamReader(stream, "utf-8")).use { br ->
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        response.append(line?.trim())
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return e.message.toString()
            }

            return response.toString()
        }
    }
}

enum class HttpMethod {
    GET,
    POST,
    DELETE,
    PUT
}