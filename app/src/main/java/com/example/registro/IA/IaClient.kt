package com.example.registro.IA

import android.content.Context
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

object IaClient {

    // ⚠️ CAMBIA ESTA IP POR LA IP DE TU PC SI ES NECESARIO
    private const val BASE_URL = "http://192.168.1.26:8000"
    private const val DETECT_ENDPOINT = "$BASE_URL/detect"
    private val client = OkHttpClient()

    fun checkHealth(onResult: (ok: Boolean, message: String) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/health")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, "Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string() ?: ""
                    if (it.isSuccessful) {
                        onResult(true, body)
                    } else {
                        onResult(false, "HTTP ${it.code}: $body")
                    }
                }
            }
        })
    }

    fun detectFromDrawable(
        context: Context,
        drawableResId: Int,
        onResult: (ok: Boolean, message: String) -> Unit
    ) {
        try {
            val inputStream = context.resources.openRawResource(drawableResId)
            val imageBytes = inputStream.readBytes()
            inputStream.close()

            val mediaType = "image/jpeg".toMediaType()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "prueba.jpg",
                    imageBytes.toRequestBody(mediaType)
                )
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/detect")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onResult(false, "Error de red: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val body = it.body?.string() ?: ""
                        if (it.isSuccessful) {
                            onResult(true, body)
                        } else {
                            onResult(false, "HTTP ${it.code}: $body")
                        }
                    }
                }
            })
        } catch (e: Exception) {
            onResult(false, "Error preparando imagen: ${e.message}")
        }
    }

    /**
     * Enviar un Bitmap a /detect y devolver:
     * - ok: true/false
     * - detections: lista de YoloDetection
     * - imageWidth / imageHeight: tamaño de la imagen que procesó la IA
     * - rawJson: respuesta cruda para logcat o debug
     */
    fun detectFromBitmap(
        bitmap: Bitmap,
        onResult: (
            ok: Boolean,
            detections: List<YoloDetection>,
            imageWidth: Int,
            imageHeight: Int,
            rawJson: String
        ) -> Unit
    ) {
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val imageBytes = stream.toByteArray()
            stream.close()

            val mediaType = "image/jpeg".toMediaType()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "frame.jpg",
                    imageBytes.toRequestBody(mediaType)
                )
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/detect")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onResult(false, emptyList(), 0, 0, "Error de red: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val body = it.body?.string() ?: ""
                        if (!it.isSuccessful) {
                            onResult(false, emptyList(), 0, 0, "HTTP ${it.code}: $body")
                            return
                        }

                        try {
                            val json = JSONObject(body)

                            // Soporta tanto camelCase como snake_case por si acaso
                            val imgW = if (json.has("imageWidth")) {
                                json.optInt("imageWidth", 0)
                            } else {
                                json.optInt("image_width", 0)
                            }

                            val imgH = if (json.has("imageHeight")) {
                                json.optInt("imageHeight", 0)
                            } else {
                                json.optInt("image_height", 0)
                            }

                            val detArray: JSONArray =
                                json.optJSONArray("detections") ?: JSONArray()

                            val list = mutableListOf<YoloDetection>()
                            for (i in 0 until detArray.length()) {
                                val det = detArray.getJSONObject(i)

                                val className = when {
                                    det.has("className") -> det.optString("className", "obj")
                                    det.has("class_name") -> det.optString("class_name", "obj")
                                    else -> "obj"
                                }

                                val conf = det.optDouble("confidence", 0.0)

                                val x1 = det.optDouble("x1", 0.0)
                                val y1 = det.optDouble("y1", 0.0)
                                val x2 = det.optDouble("x2", 0.0)
                                val y2 = det.optDouble("y2", 0.0)

                                list.add(
                                    YoloDetection(
                                        className = className,
                                        confidence = conf.toFloat(),
                                        x1 = x1.toFloat(),
                                        y1 = y1.toFloat(),
                                        x2 = x2.toFloat(),
                                        y2 = y2.toFloat()
                                    )
                                )
                            }

                            onResult(true, list, imgW, imgH, body)
                        } catch (e: Exception) {
                            onResult(
                                false,
                                emptyList(),
                                0,
                                0,
                                "Error parseando JSON: ${e.message}"
                            )
                        }
                    }
                }
            })
        } catch (e: Exception) {
            onResult(false, emptyList(), 0, 0, "Error preparando bitmap: ${e.message}")
        }
    }
}
