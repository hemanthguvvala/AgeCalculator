package com.hkgroups.agecalculator.ui.screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * CosmicPullToRefresh — wraps [PullToRefreshBox] with our orbiting-planet
 * indicator so the refresh affordance stays on-brand.
 *
 * The default Material indicator is replaced by [CosmicLoading], which
 * scales+rotates as the pull progresses and spins continuously while the
 * refresh is in flight.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmicPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        state = state,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        indicator = {
            // Custom indicator drawn at the top — fades + scales with pull
            // distance so it grows in as the user drags.
            val pullProgress = state.distanceFraction.coerceIn(0f, 1.5f)
            val scale = (0.4f + pullProgress * 0.6f).coerceAtMost(1f)
            val alpha = (pullProgress * 1.2f).coerceAtMost(1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRefreshing || pullProgress > 0f) {
                    CosmicLoading(
                        modifier = Modifier
                            .graphicsLayer {
                                this.scaleX = scale
                                this.scaleY = scale
                                this.alpha = alpha
                            }
                    )
                }
            }
        }
    ) {
        content()
    }
}
