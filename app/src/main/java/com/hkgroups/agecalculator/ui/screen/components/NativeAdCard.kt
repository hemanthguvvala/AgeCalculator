package com.hkgroups.agecalculator.ui.screen.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.facebook.ads.AdOptionsView
import com.facebook.ads.MediaView
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdLayout
import com.hkgroups.agecalculator.R
import com.hkgroups.agecalculator.util.LocalAdController

/**
 * NativeAdCard — Facebook native ad rendered as a cosmic glass card so it
 * blends with the dashboard. While the ad loads we show nothing (no skeleton
 * jank); once loaded, the FAN-required `NativeAdLayout` swaps in.
 *
 * The native ad layout is implemented as a traditional Android XML inside
 * AndroidView because FAN's required view types (MediaView, AdIconView,
 * AdOptionsView) live in the View world, not Compose.
 */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val controller = LocalAdController.current ?: return
    val context = LocalContext.current

    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    // Kick off the load on first composition. Same controller instance =
    // same ad — `remember(controller)` keeps it stable across recompositions.
    val pendingAd = remember(controller) {
        controller.createNativeAd(
            context = context,
            onLoaded = { nativeAd = it }
        )
    }

    // Destroy the ad when leaving the composition tree.
    DisposableEffect(pendingAd) {
        onDispose { pendingAd?.destroy() }
    }

    // While loading: render nothing. Saves the layout from collapsing-and-
    // jumping when the ad arrives a few hundred ms later.
    val ad = nativeAd ?: return

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            factory = { ctx ->
                val root = LayoutInflater.from(ctx)
                    .inflate(R.layout.fan_native_ad_layout, null, false) as NativeAdLayout
                val mediaView = root.findViewById<MediaView>(R.id.fan_native_media)
                val iconView = root.findViewById<MediaView>(R.id.fan_native_icon)
                val title = root.findViewById<android.widget.TextView>(R.id.fan_native_title)
                val sponsored = root.findViewById<android.widget.TextView>(R.id.fan_native_sponsored)
                val body = root.findViewById<android.widget.TextView>(R.id.fan_native_body)
                val cta = root.findViewById<android.widget.Button>(R.id.fan_native_cta)
                val choicesContainer = root.findViewById<LinearLayout>(R.id.fan_native_choices)

                title.text = ad.advertiserName
                sponsored.text = "SPONSORED"
                body.text = ad.adBodyText.orEmpty().ifBlank { ad.adSocialContext.orEmpty() }
                cta.text = ad.adCallToAction
                cta.visibility = if (cta.text.isNullOrBlank()) View.GONE else View.VISIBLE

                // FAN's AdChoices icon — required for compliance.
                choicesContainer.removeAllViews()
                choicesContainer.addView(AdOptionsView(ctx, ad, root))

                // Register the views FAN needs to attach impression + click
                // tracking to. Click targets are the title + cta + media.
                val clickable = listOf<View>(title, body, cta)
                ad.registerViewForInteraction(root, mediaView, iconView, clickable)
                root
            }
        )
    }
}
