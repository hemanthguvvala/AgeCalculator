package com.hkgroups.agecalculator.ui.screen.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.hkgroups.agecalculator.ui.theme.SignPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * shareCosmicIdentityText — builds a textual
 * cosmic identity card and fires Android's ACTION_SEND. Works on every
 * device and never produces a "render failed" path.
 *
 * Image-based sharing of the cosmic card is wired via [shareCosmicCardImage]
 * once a bitmap is produced via Compose's experimental capture API.
 */
fun shareCosmicIdentityText(
    context: Context,
    sunSign: String?,
    chineseZodiac: String?,
    birthDate: String?,
    earthAge: String?,
    appName: String = "Zodiac Age"
) {
    val pieces = buildList {
        add("✨ My Cosmic Identity ✨")
        if (sunSign != null) add("Sun sign: $sunSign")
        if (chineseZodiac != null) add("Chinese zodiac: Year of the $chineseZodiac")
        if (birthDate != null) add("Born: $birthDate")
        if (earthAge != null) add("Earth age: $earthAge")
        add("")
        add("Discover yours with $appName.")
    }
    val text = pieces.joinToString(separator = "\n")
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, "Share your cosmic identity")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}

/**
 * renderCosmicShareCard — programmatically draws a 1080x1920 cosmic identity
 * card to an Android Bitmap. Done with android.graphics primitives so it
 * works on every device without depending on experimental Compose capture
 * APIs.
 *
 * Layout:
 *   - Vertical cosmic gradient background
 *   - Sign-tinted radial glow
 *   - Centered sun-sign symbol + name
 *   - Chinese zodiac line
 *   - "Earth age" big number
 *   - Footer: app name + tagline
 */
fun renderCosmicShareCard(
    sunSign: String?,
    sunSignSymbol: String?,
    chineseZodiac: String?,
    earthAge: String?,
    palette: SignPalette
): Bitmap {
    val w = 1080
    val h = 1920
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Background — vertical gradient
    paint.shader = LinearGradient(
        0f, 0f, 0f, h.toFloat(),
        intArrayOf(0xFF06080F.toInt(), 0xFF030410.toInt(), 0xFF000000.toInt()),
        floatArrayOf(0f, 0.5f, 1f),
        Shader.TileMode.CLAMP
    )
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    paint.shader = null

    // Sign-tinted glow at top-center
    paint.shader = RadialGradient(
        w / 2f, h * 0.3f, w * 0.65f,
        intArrayOf(
            (palette.primary.value shr 32).toInt() or 0x66000000.toInt(),
            (palette.secondary.value shr 32).toInt() or 0x22000000.toInt(),
            0x00000000
        ),
        floatArrayOf(0f, 0.55f, 1f),
        Shader.TileMode.CLAMP
    )
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    paint.shader = null

    val centerX = w / 2f

    // Top brand wordmark
    paint.color = 0x99FFFFFF.toInt()
    paint.textSize = 36f
    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    paint.textAlign = Paint.Align.CENTER
    paint.letterSpacing = 0.35f
    canvas.drawText("✨ MY COSMIC IDENTITY ✨", centerX, 200f, paint)
    paint.letterSpacing = 0f

    // Sign symbol (uses unicode glyph since Android Canvas can't draw our
    // Compose ZodiacGlyph; the symbol is the closest equivalent)
    paint.color = palette.primary.value.let {
        (it shr 32).toInt() or 0xFF000000.toInt()
    }
    paint.textSize = 280f
    paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    canvas.drawText(sunSignSymbol ?: "✨", centerX, 600f, paint)

    // Sun sign name
    paint.color = 0xFFFFFFFF.toInt()
    paint.textSize = 110f
    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    canvas.drawText(sunSign ?: "Cosmic Soul", centerX, 760f, paint)

    // Chinese zodiac line
    if (chineseZodiac != null) {
        paint.color = 0xCCFFFFFF.toInt()
        paint.textSize = 44f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("Year of the $chineseZodiac", centerX, 830f, paint)
    }

    // Divider
    paint.color = 0x33FFFFFF.toInt()
    canvas.drawRect(centerX - 80f, 920f, centerX + 80f, 922f, paint)

    // Earth age big number
    if (earthAge != null) {
        paint.color = palette.primary.value.let {
            (it shr 32).toInt() or 0xFF000000.toInt()
        }
        paint.textSize = 72f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.letterSpacing = 0.15f
        canvas.drawText("EARTH AGE", centerX, 1040f, paint)
        paint.letterSpacing = 0f

        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 240f
        canvas.drawText(earthAge, centerX, 1280f, paint)
    }

    // Footer
    paint.color = 0x88FFFFFF.toInt()
    paint.textSize = 38f
    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    canvas.drawText("Discover yours →", centerX, 1700f, paint)

    paint.color = 0xFFFFFFFF.toInt()
    paint.textSize = 56f
    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    paint.letterSpacing = 0.3f
    canvas.drawText("ZODIAC AGE", centerX, 1780f, paint)
    paint.letterSpacing = 0f

    return bmp
}

/**
 * shareCosmicCardImage — saves [bitmap] to the app's cache and fires an
 * image share intent via FileProvider. Caller passes a captured bitmap.
 */
suspend fun shareCosmicCardImage(
    context: Context,
    bitmap: Bitmap
) {
    val file = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "shared")
        if (!dir.exists()) dir.mkdirs()
        val out = File(dir, "cosmic_${System.currentTimeMillis()}.png")
        FileOutputStream(out).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        out
    }
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val chooser = Intent.createChooser(intent, "Share your cosmic card")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}
