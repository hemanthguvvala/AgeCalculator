package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.facebook.ads.AdView
import com.hkgroups.agecalculator.util.LocalAdController

/**
 * BannerAdHost — drops a FAN banner into the Compose tree.
 *
 * Returns a zero-height Box when:
 *  - no ad controller is provided (e.g., in previews)
 *  - the controller refuses to create a banner (premium / disabled)
 *
 * The hosted AdView is destroyed on disposal so we don't leak across
 * navigation. One banner per screen — never stacked.
 */
@Composable
fun BannerAdHost(modifier: Modifier = Modifier) {
    val controller = LocalAdController.current ?: return
    val context = LocalContext.current

    // Create the AdView once per composable instance and keep a stable handle
    // so DisposableEffect can destroy() exactly the same instance.
    val adView: AdView? = remember(controller) {
        controller.createBannerAdView(context)
    }

    if (adView == null) return

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { adView }
        )
    }
}
