package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.MemeStudioState
import com.example.data.OverlayPosition
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object MemeRenderer {

    fun shareMemeCard(context: Context, state: MemeStudioState): Result<Unit> = runCatching {
        val baseBitmap = loadSourceBitmap(context, state.selectedPhotoUri)
        val composed = renderOverlayBitmap(context, baseBitmap, state)

        val memesDir = File(context.cacheDir, "memes").apply { mkdirs() }
        val outFile = File(memesDir, "courier_meme_${System.currentTimeMillis()}.png")
        FileOutputStream(outFile).use { stream ->
            composed.compress(Bitmap.CompressFormat.PNG, 95, stream)
        }
        if (composed !== baseBitmap) {
            composed.recycle()
        }
        baseBitmap.recycle()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "${state.subtitle}: ${state.reason} (${state.amountText})"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, context.getString(R.string.share_meme))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun loadSourceBitmap(context: Context, photoUriString: String?): Bitmap {
        val raw = if (photoUriString != null) {
            val uri = Uri.parse(photoUriString)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val maxDim = max(info.size.width, info.size.height)
                    if (maxDim > 1200) {
                        val scale = 1200f / maxDim
                        decoder.setTargetSize(
                            (info.size.width * scale).toInt(),
                            (info.size.height * scale).toInt()
                        )
                    }
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: BitmapFactory.decodeResource(context.resources, R.drawable.courier_meme_hero)
            }
        } else {
            BitmapFactory.decodeResource(context.resources, R.drawable.courier_meme_hero)
        }

        val targetW = 960
        val scale = targetW.toFloat() / raw.width.coerceAtLeast(1)
        val targetH = (raw.height * scale).toInt().coerceIn(640, 1440)
        val scaled = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(scaled)
        val matrix = Matrix().apply {
            val s = max(targetW.toFloat() / raw.width, targetH.toFloat() / raw.height)
            val dx = (targetW - raw.width * s) / 2f
            val dy = (targetH - raw.height * s) / 2f
            setScale(s, s)
            postTranslate(dx, dy)
        }
        canvas.drawBitmap(raw, matrix, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        if (raw != scaled) raw.recycle()
        return scaled
    }

    private fun renderOverlayBitmap(
        context: Context,
        source: Bitmap,
        state: MemeStudioState
    ): Bitmap {
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val w = result.width.toFloat()
        val h = result.height.toFloat()

        val manrope = ResourcesCompat.getFont(context, R.font.manrope) ?: Typeface.DEFAULT
        val jetbrains = ResourcesCompat.getFont(context, R.font.jetbrains_mono) ?: Typeface.MONOSPACE

        val scale = state.badgeScale.coerceIn(0.75f, 1.35f)
        val pad = w * 0.032f
        val cardW = min(w * 0.56f * scale, w - pad * 2f)
        val cardH = w * 0.175f * scale

        val left = when (state.position) {
            OverlayPosition.TOP_LEFT, OverlayPosition.BOTTOM_LEFT -> pad
            OverlayPosition.TOP_CENTER -> (w - cardW) / 2f
        }
        val top = when (state.position) {
            OverlayPosition.TOP_LEFT, OverlayPosition.TOP_CENTER -> pad
            OverlayPosition.BOTTOM_LEFT -> h - cardH - pad
        }

        val cardRect = RectF(left, top, left + cardW, top + cardH)
        val radius = cardH * 0.22f

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            setShadowLayer(cardH * 0.16f, 0f, cardH * 0.05f, Color.argb(75, 0, 0, 0))
        }
        canvas.drawRoundRect(cardRect, radius, radius, shadowPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.rgb(228, 234, 230)
        }
        canvas.drawRoundRect(cardRect, radius, radius, borderPaint)

        var textStartX = left + cardH * 0.22f

        if (state.showBankBadge) {
            val badgeR = cardH * 0.29f
            val cx = left + cardH * 0.22f + badgeR
            val cy = top + cardH * 0.5f
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(29, 185, 84)
            }
            canvas.drawCircle(cx, cy, badgeR, badgePaint)

            val iconStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = badgeR * 0.14f
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawCircle(cx + badgeR * 0.12f, cy - badgeR * 0.22f, badgeR * 0.30f, iconStroke)
            canvas.drawLine(
                cx - badgeR * 0.45f,
                cy + badgeR * 0.35f,
                cx + badgeR * 0.45f,
                cy + badgeR * 0.22f,
                iconStroke
            )
            textStartX = cx + badgeR + cardH * 0.18f
        }

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = manrope
            textSize = cardH * 0.155f
            color = Color.rgb(140, 148, 144)
        }
        val reasonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(manrope, Typeface.BOLD)
            textSize = cardH * 0.22f
            color = Color.rgb(20, 23, 21)
        }
        val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(jetbrains, Typeface.BOLD)
            textSize = cardH * 0.27f
            color = Color.rgb(33, 160, 56)
        }

        val subtitleText = state.subtitle.ifBlank { "Входящий перевод" }
        val reasonText = state.reason.ifBlank { "На лечение" }
        val amountText = state.amountText.ifBlank { "+ 15₽" }

        canvas.drawText(subtitleText, textStartX, top + cardH * 0.31f, subtitlePaint)
        canvas.drawText(reasonText, textStartX, top + cardH * 0.57f, reasonPaint)
        canvas.drawText(amountText, textStartX, top + cardH * 0.85f, amountPaint)

        return result
    }
}
