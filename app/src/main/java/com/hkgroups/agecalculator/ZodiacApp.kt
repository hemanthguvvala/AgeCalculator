package com.hkgroups.agecalculator

import android.app.Activity
import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hkgroups.agecalculator.data.repository.SettingsRepository
import com.hkgroups.agecalculator.util.BillingController
import com.hkgroups.agecalculator.util.ConsentManager
import com.hkgroups.agecalculator.util.FanAdsController
import com.hkgroups.agecalculator.worker.CosmicEventNotificationWorker
import com.hkgroups.agecalculator.worker.HoroscopeNotificationWorker
import com.hkgroups.agecalculator.worker.MoodReminderWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ZodiacAgeApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var settingsRepository: SettingsRepository

    /** Process-scoped controller. Reads the latest `adsDisabled` value
     *  per request — flipping the flag at runtime takes effect immediately. */
    lateinit var adsController: FanAdsController
        private set

    /** Process-scoped Play Billing controller. */
    lateinit var billingController: BillingController
        private set

    /** UMP consent gate. Held by the app so MainActivity can drive the prompt. */
    lateinit var consentManager: ConsentManager
        private set

    // Default-dispatcher scope so background work doesn't run on Main.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var cachedAdsDisabled: Boolean = false

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDailyHoroscope()
        scheduleCosmicEventChecks()
        scheduleMoodReminder()

        // Cache the ads-disabled flag synchronously so ad callbacks (which
        // can fire from any thread) never block on DataStore reads.
        appScope.launch {
            settingsRepository.adsDisabled.collect { cachedAdsDisabled = it }
        }

        consentManager = ConsentManager(applicationContext)

        adsController = FanAdsController(
            applicationContext = applicationContext,
            adsDisabledProvider = { cachedAdsDisabled },
            canShowAdsProvider = { consentManager.canShowAds.value }
        )
        // Note: FAN init is deferred until MainActivity drives the consent
        // prompt — see [requestConsentAndInitAds]. EEA / UK users will see
        // the consent dialog before any ad SDK runs.

        billingController = BillingController(
            context = applicationContext,
            settingsRepository = settingsRepository,
            scope = appScope
        )
        billingController.start()
    }

    /**
     * Drives the UMP consent flow from an Activity context, then initializes
     * FAN ads if consent allows. Idempotent — safe to call from
     * MainActivity.onCreate every launch.
     */
    fun requestConsentAndInitAds(activity: Activity) {
        consentManager.requestConsentIfNeeded(activity) {
            // Whether or not the user consented, we always init FAN — the
            // controller's `canShowAdsProvider` gates whether ads actually
            // load. This way premium users with consent denied see nothing,
            // free users with consent see ads, and the lifecycle is uniform.
            adsController.initialize()
        }
    }

    /** Daily 8pm reminder to log today's mood. */
    private fun scheduleMoodReminder() {
        val request = PeriodicWorkRequestBuilder<MoodReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayUntil(hour = 20), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MoodReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Daily 7am check for imminent cosmic events. */
    private fun scheduleCosmicEventChecks() {
        val request = PeriodicWorkRequestBuilder<CosmicEventNotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayUntil(hour = 7), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CosmicEventNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Daily 8am horoscope notification. */
    private fun scheduleDailyHoroscope() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<HoroscopeNotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayUntil(hour = 8), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyHoroscopeNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    private fun initialDelayUntil(hour: Int): Long {
        val current = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(current)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - current.timeInMillis
    }
}
