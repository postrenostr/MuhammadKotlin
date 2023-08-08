package com.vitorpamplona.amethyst.ui.actions

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException

class SaveQuery {
    val client = OkHttpClient()
    suspend fun saveQuery(param1: String, param2: String) {

        val mediaType = "text/plain".toMediaType()
        val requestBody = "".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api.postre.io/addStrings?param1=$param1&param2=$param2")
            .post(requestBody)
            .build()

        withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
            } catch (e: IOException) {
                // Handle network or IO exception
            }
        }
    }
}