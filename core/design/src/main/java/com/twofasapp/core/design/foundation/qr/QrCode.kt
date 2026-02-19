package com.twofasapp.core.design.foundation.qr

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun QrCode(
    content: String,
    modifier: Modifier = Modifier,
    colors: QrCodeColors = QrCodeDefaults.colors(),
    margin: Dp = 0.dp
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val bitmap = rememberQrBitmap(
            content = content,
            size = min(maxHeight, maxWidth),
            backgroundColor = colors.backgroundColor.toArgb(),
            foregroundColor = colors.foregroundColor.toArgb(),
            margin = margin
        )
        Crossfade(bitmap) { qrBitmap ->
            if (qrBitmap != null) {
                Image(
                    painter = remember(qrBitmap) { BitmapPainter(qrBitmap.asImageBitmap()) },
                    contentDescription = null,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colors.backgroundColor)
                )
            }
        }
    }
}

@Composable
private fun rememberQrBitmap(
    content: String,
    margin: Dp,
    size: Dp,
    @ColorInt backgroundColor: Int,
    @ColorInt foregroundColor: Int
): Bitmap? {
    val density = LocalDensity.current
    val sizePx = remember(size) { with(density) { size.roundToPx() } }
    val marginPx = remember(margin) { with(density) { margin.roundToPx() } }

    var bitmap by remember(content) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(content) {
        launch(Dispatchers.IO) {
            val qrCodeWriter = QRCodeWriter()

            val encodeHints = mutableMapOf<EncodeHintType, Any?>().apply {
                this[EncodeHintType.MARGIN] = marginPx
            }

            val bitmapMatrix = try {
                qrCodeWriter.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    sizePx,
                    sizePx,
                    encodeHints,
                )
            } catch (t: Throwable) {
                Timber.e(t)
                null
            }

            val matrixWidth = bitmapMatrix?.width ?: sizePx
            val matrixHeight = bitmapMatrix?.height ?: sizePx

            val newBitmap =
                createBitmap(bitmapMatrix?.width ?: sizePx, bitmapMatrix?.height ?: sizePx)
            newBitmap.eraseColor(backgroundColor)

            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeight) {
                    val shouldColorPixel = bitmapMatrix?.get(x, y) ?: false
                    if (shouldColorPixel) {
                        newBitmap[x, y] = foregroundColor
                    }
                }
            }

            bitmap = newBitmap
        }
    }

    return bitmap
}

@Immutable
data class QrCodeColors(
    val backgroundColor: Color,
    val foregroundColor: Color
)

object QrCodeDefaults {

    @Composable
    fun colors(
        backgroundColor: Color = Color.White,
        foregroundColor: Color = Color.Black,
    ): QrCodeColors {
        return QrCodeColors(
            backgroundColor = backgroundColor,
            foregroundColor = foregroundColor,
        )
    }
}
