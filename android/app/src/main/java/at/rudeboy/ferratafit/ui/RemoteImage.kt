package at.rudeboy.ferratafit.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Lädt ein Bild aus dem Netz — einmal. Danach kommt es von der Platte.
 *
 * Ohne Bibliothek, mit Absicht: Coil oder Glide brächten Megabytes und eine eigene
 * Welt an Konfiguration mit, für die paar Dutzend Commons-Fotos reicht das hier.
 * Der Zwischenspeicher liegt im cacheDir, den Android bei Platzmangel selbst leert.
 *
 * Wikimedia verlangt eine aussagekräftige User-Agent-Kennung — ohne sie antwortet der
 * Server mit 403. Deshalb steht hier der Projektname samt Adresse drin.
 */
object ImageCache {

    private const val USER_AGENT =
        "FerrataFit/1.11 (https://github.com/Only1Rudeboy/FerrataFit; private Trainings-App)"

    private fun fileFor(context: Context, url: String): File {
        val dir = File(context.cacheDir, "img").apply { mkdirs() }
        val hash = MessageDigest.getInstance("MD5").digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(dir, "$hash.jpg")
    }

    suspend fun load(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        val file = fileFor(context, url)
        try {
            if (!file.exists() || file.length() == 0L) {
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", USER_AGENT)
                    connectTimeout = 10_000
                    readTimeout = 20_000
                    instanceFollowRedirects = true
                }
                if (conn.responseCode !in 200..299) {
                    conn.disconnect()
                    return@withContext null
                }
                // Erst in eine Nebendatei, dann umbenennen — ein abgebrochener Download
                // hinterlässt sonst eine halbe Datei, die beim nächsten Mal als fertig gilt
                val tmp = File(file.path + ".tmp")
                conn.inputStream.use { input -> tmp.outputStream().use { input.copyTo(it) } }
                conn.disconnect()
                tmp.renameTo(file)
            }
            // Fürs Display reichen 1200 Pixel; größer wäre nur Speicher
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.path, bounds)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 1200) sample *= 2
            BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
        } catch (_: Exception) {
            file.delete()
            null
        }
    }
}

/** Ein Bild aus dem Netz mit Ladeanzeige und ehrlichem Fehlerfall. */
@Composable
fun RemoteImage(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp
) {
    val context = LocalContext.current
    val state by produceState<Pair<Bitmap?, Boolean>>(initialValue = null to false, url) {
        value = ImageCache.load(context, url) to true
    }
    val (bitmap, done) = state

    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(Palette.SurfaceHigh),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(height)
            )
            !done -> CircularProgressIndicator(color = Palette.Sky, strokeWidth = 2.dp)
            else -> Text(
                "Bild nicht geladen — kein Netz?",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.TextLow
            )
        }
    }
}
