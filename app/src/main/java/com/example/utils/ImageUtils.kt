package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageUtils {
    suspend fun uriToBase64(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return@withContext null

            // Scale down to prevent huge base64 strings in Firestore (max 1MB doc size)
            val maxDim = 800
            val scale = maxDim.toFloat() / Math.max(bitmap.width, bitmap.height)
            val scaledBitmap = if (scale < 1) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            
            // "data:image/jpeg;base64," prefix can be added so coil handles it, but Coil also parses raw base64 sometimes.
            // Coil actually handles base64 if we use a custom fetcher, or if we prefix it if it's an SVG. For jpeg, standard Coil 
            // doesn't natively load raw base64. Wait, we can decode base64 to ByteArray and load in compose?
            // Actually, `BitmapFactory.decodeByteArray` can be used to load it in Compose. 
            // Let's use Base64 and return string.
            val b64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            
            if (scaledBitmap != bitmap) scaledBitmap.recycle()
            bitmap.recycle()
            
            return@withContext b64
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
