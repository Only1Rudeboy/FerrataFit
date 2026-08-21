package at.rudeboy.ferratafit.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Ein Bild bildschirmfüllend, mit Zoom und Verschieben.
 *
 * Für Topos unverzichtbar: Die Grade und Pfeile sind in der Kachelansicht nicht lesbar,
 * und genau die will man vor dem Einstieg sehen. Zwei Finger zoomen, ein Finger
 * verschiebt; das X oben rechts schließt.
 */
@Composable
fun ZoomImageDialog(path: String, caption: String, onClose: () -> Unit) {
    val bitmap = remember(path) {
        runCatching {
            // Groß genug für Zoom, klein genug für den Speicher
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 2400) sample *= 2
            BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
                ?.asImageBitmap()
        }.getOrNull()
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 6f)
                        // Bei Normalgröße nicht verschieben — sonst „verliert" man das Bild
                        offset = if (scale > 1f) offset + pan else Offset.Zero
                    }
                }
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = caption,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                )
            } else {
                Text(
                    "Bild nicht lesbar",
                    color = Palette.TextLow,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            if (caption.isNotBlank()) {
                Text(
                    caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.TextMid,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)) {
                Icon(Icons.Filled.Close, "Schließen", tint = Color.White)
            }
        }
    }
}
