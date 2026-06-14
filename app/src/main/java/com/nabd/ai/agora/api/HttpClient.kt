package com.nabd.ai.agora.api

import com.nabd.ai.agora.util.DebugLog
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSource
import java.util.concurrent.TimeUnit

object HttpClient {
    private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** The currently active streaming handle, if any. Used to cancel
     *  generation immediately by closing the underlying socket. */
    @Volatile
    var activeStreamHandle: StreamHandle? = null

    class StreamHandle(private val call: Call, private val response: Response) {
        val code: Int = response.code

        /** 
         * The source is drawn directly from the response body. 
         * We ONLY acquire it if the response is successful to avoid 
         * premature consumption of error bodies.
         */
        val source: BufferedSource? = if (response.isSuccessful) response.body?.source() else null

        /** 
         * Error body is ONLY populated if the response is not successful.
         * This prevents 'response.body?.string()' from consuming the stream
         * of a valid SSE connection.
         */
        val errorBody: String? = if (!response.isSuccessful) {
            try {
                // response.body?.string() closes the body automatically
                response.body?.string()
            } catch (e: Exception) {
                DebugLog.e("HttpClient", "Failed to read error body: ${e.message}")
                null
            }
        } else {
            null
        }

        fun close() {
            if (HttpClient.activeStreamHandle === this) {
                HttpClient.activeStreamHandle = null
            }
            try {
                response.close()
            } catch (e: Exception) {
                DebugLog.e("HttpClient", "Error closing response: ${e.message}")
            }
        }

        /** 
         * Reads a single line from the source. 
         * This remains unblocked and lightweight for SSE.
         */
        fun readLine(): String? = source?.readUtf8Line()

        /** Cancel the underlying HTTP call immediately — unblocks [readLine]. */
        fun cancel() {
            call.cancel()
        }
    }

    fun streamPost(url: String, jsonBody: String, headers: Map<String, String> = emptyMap()): StreamHandle {
        val body: RequestBody = jsonBody.toRequestBody(JSON)
        val requestBuilder = Request.Builder().url(url).post(body)
        headers.forEach { (k, v) -> requestBuilder.addHeader(k, v) }
        
        val call = client.newCall(requestBuilder.build())
        // execute() returns the response once headers are received, 
        // allowing the body to be streamed.
        val response = call.execute()
        val handle = StreamHandle(call, response)
        activeStreamHandle = handle
        return handle
    }

    fun post(url: String, jsonBody: String, headers: Map<String, String> = emptyMap()): String? {
        val body: RequestBody = jsonBody.toRequestBody(JSON)
        val requestBuilder = Request.Builder().url(url).post(body)
        headers.forEach { (k, v) -> requestBuilder.addHeader(k, v) }
        
        return try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    val error = response.body?.string()
                    DebugLog.e("HttpClient", "POST $url failed: ${response.code} $error")
                    null
                }
            }
        } catch (e: Exception) {
            DebugLog.e("HttpClient", "POST $url exception: ${e.message}")
            null
        }
    }

    fun fetchModels(url: String, headers: Map<String, String> = emptyMap()): String? {
        val requestBuilder = Request.Builder().url(url).get()
        headers.forEach { (k, v) -> requestBuilder.addHeader(k, v) }
        
        return try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    DebugLog.e("HttpClient", "GET $url failed: ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            DebugLog.e("HttpClient", "GET $url exception: ${e.message}")
            null
        }
    }
}
