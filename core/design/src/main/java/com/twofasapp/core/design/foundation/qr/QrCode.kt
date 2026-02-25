package com.twofasapp.core.design.foundation.qr

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun QrCode(
    content: String,
    size: Dp,
    modifier: Modifier = Modifier,
    colors: QrCodeColors = QrCodeDefaults.colors(),
    characterSet: String = "UTF-8",
) {
    val bitmap by rememberQrBitmap(
        content = content,
        size = size,
        backgroundColor = colors.backgroundColor,
        foregroundColor = colors.foregroundColor,
        characterSet = characterSet,
    )
    Crossfade(modifier = modifier, targetState = bitmap) { qrBitmap ->
        if (qrBitmap != null) {
            Image(
                painter = BitmapPainter(qrBitmap.asImageBitmap()),
                contentDescription = null,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(color = colors.backgroundColor),
            )
        }
    }
}

@Composable
private fun rememberQrBitmap(
    content: String,
    size: Dp,
    backgroundColor: Color,
    foregroundColor: Color,
    characterSet: String,
): State<Bitmap?> {
    val density = LocalDensity.current
    val sizePx = remember(size) { with(density) { size.roundToPx() } }
    val backgroundColorInt = remember(backgroundColor) { backgroundColor.toArgb() }
    val foregroundColorInt = remember(foregroundColor) { foregroundColor.toArgb() }

    return produceState(
        null,
        content,
        sizePx,
        backgroundColorInt,
        foregroundColorInt,
        characterSet,
    ) {
        value = withContext(Dispatchers.Default) {
            generateQrBitmap(
                content = content,
                sizePx = sizePx,
                characterSet = characterSet,
                backgroundColor = backgroundColorInt,
                foregroundColor = foregroundColorInt,
            )
        }
    }
}

private fun generateQrBitmap(
    content: String,
    sizePx: Int,
    characterSet: String,
    @ColorInt backgroundColor: Int,
    @ColorInt foregroundColor: Int,
): Bitmap? {
    val qrCodeWriter = QRCodeWriter()

    val encodeHints = mutableMapOf<EncodeHintType, Any?>().apply {
        this[EncodeHintType.MARGIN] = 0
        this[EncodeHintType.CHARACTER_SET] = characterSet
    }

    return try {
        val bitmapMatrix = qrCodeWriter.encode(
            content,
            BarcodeFormat.QR_CODE,
            sizePx,
            sizePx,
            encodeHints,
        )

        val width = bitmapMatrix.width
        val height = bitmapMatrix.height
        val bitmap =
            createBitmap(width, height)

        val pixels = IntArray(width * height) { backgroundColor }
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (bitmapMatrix[x, y]) pixels[index] = foregroundColor
                index++
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        bitmap
    } catch (t: Throwable) {
        Timber.e(t)
        null
    }
}

@Immutable
data class QrCodeColors(
    val backgroundColor: Color,
    val foregroundColor: Color,
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