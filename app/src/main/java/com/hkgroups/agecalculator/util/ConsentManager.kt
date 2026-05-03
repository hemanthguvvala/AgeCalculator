package com.hkgroups.agecalculator.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Wraps Google's User Messaging Platform (UMP) — the standards-compliant
 * GDPR consent dialog required by Meta + Play before serving ads in
 * regulated regions (EEA / UK / Switzerland).
 *
 * Outside regulated regions, UMP fast-paths to "consent not required" and
 * we proceed to ad init immediately. Inside, the consent form pops on first
 * launch and the user's choice persists across sessions.
 */
class ConsentManager(private val context: Context) {

    companion object {
        private const val TAG = "Consent"
    }

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    private val _canShowAds = MutableStateFlow(consentInformation.canRequestAds())
    /** True when ads are allowed under the user's current consent state. */
    val canShowAds: StateFlow<Boolean> = _canShowAds.asStateFlow()

    /**
     * Request consent info update and surface the consent form if needed.
     * Calls [onResolved] when the consent flow is complete (form dismissed
     * or not needed). Always invokes the callback exactly once.
     */
    fun requestConsentIfNeeded(activity: Activity, onResolved: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    if (error != null) {
                        Log.w(TAG, "Consent form error: ${error.errorCode} ${error.message}")
                    }
                    _canShowAds.value = consentInformation.canRequestAds()
                    onResolved()
                }
            },
            { error ->
                Log.w(TAG, "Consent info update failed: ${error.errorCode} ${error.message}")
                _canShowAds.value = consentInformation.canRequestAds()
                onResolved()
            }
        )
    }

    /** True if a privacy options entry point should be shown in Settings —
     *  required by UMP for users in regulated regions. */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /** Re-presents the consent form on demand (called from Settings → Privacy). */
    fun showPrivacyOptions(activity: Activity, onDismissed: () -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) {
                Log.w(TAG, "Privacy options form error: ${error.errorCode} ${error.message}")
            }
            _canShowAds.value = consentInformation.canRequestAds()
            onDismissed()
        }
    }

    /** Reset consent — debug-only escape hatch for testing the consent flow. */
    fun reset() {
        consentInformation.reset()
        _canShowAds.value = consentInformation.canRequestAds()
    }
}

val LocalConsentManager = compositionLocalOf<ConsentManager?> { null }
