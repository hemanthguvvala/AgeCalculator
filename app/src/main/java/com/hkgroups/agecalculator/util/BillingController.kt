package com.hkgroups.agecalculator.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.hkgroups.agecalculator.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * BillingController — single owner of Google Play Billing state.
 *
 * Uses a one-time non-consumable purchase ("premium_lifetime") because:
 *  - Lifetime is simpler than subscription (no renewal handling)
 *  - Lower price + lifetime feels generous → better conversion for v1
 *  - Subscription tier can be added later as a second SKU without rewriting
 *
 * Ownership state is mirrored into [SettingsRepository.adsDisabled] so the
 * rest of the app doesn't need to know about Billing at all — they just read
 * the flag.
 */
class BillingController(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "Billing"
        const val PREMIUM_PRODUCT_ID = "premium_lifetime"
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    /** Live ownership flag. Mirrors into SettingsRepository.adsDisabled. */
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    enum class ConnectionState { Disconnected, Connecting, Connected, Failed }

    /** Idempotent — connects to Play Billing service and triggers initial
     *  product+purchase queries. Reconnects automatically on disconnect. */
    fun start() {
        if (billingClient.isReady) return
        connect()
    }

    private fun connect() {
        _connectionState.value = ConnectionState.Connecting
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _connectionState.value = ConnectionState.Connected
                    Log.d(TAG, "Billing connected")
                    queryProductDetails()
                    queryOwnership()
                } else {
                    _connectionState.value = ConnectionState.Failed
                    Log.w(TAG, "Billing connect failed: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _connectionState.value = ConnectionState.Disconnected
                Log.d(TAG, "Billing disconnected — will retry on next call")
            }
        })
    }

    /** Fetch the price / title of the premium SKU from Play. */
    private fun queryProductDetails() {
        scope.launch {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PREMIUM_PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                )
                .build()
            val result = billingClient.queryProductDetails(params)
            val details = result.productDetailsList?.firstOrNull()
            if (details != null) {
                _productDetails.value = details
                Log.d(TAG, "Premium product loaded: ${details.oneTimePurchaseOfferDetails?.formattedPrice}")
            } else {
                Log.w(TAG, "Product details unavailable: ${result.billingResult.debugMessage}")
            }
        }
    }

    /** Check if the user already owns the premium SKU. */
    fun queryOwnership() {
        scope.launch {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            val result = billingClient.queryPurchasesAsync(params)
            val owned = result.purchasesList.any { p ->
                p.products.contains(PREMIUM_PRODUCT_ID) &&
                    p.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            updatePremium(owned)

            // Acknowledge any unacknowledged owned purchases (Play requires
            // acknowledgement within 3 days or the purchase auto-refunds).
            result.purchasesList
                .filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged
                }
                .forEach { acknowledge(it) }
        }
    }

    /** Launch the purchase UI for the premium SKU. */
    fun launchPurchase(activity: Activity) {
        val details = _productDetails.value ?: run {
            Log.w(TAG, "Cannot launch purchase: product details not loaded")
            return
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            )
            .build()
        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "launchBillingFlow failed: ${result.debugMessage}")
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) {
            Log.d(TAG, "Purchase update: ${result.responseCode} ${result.debugMessage}")
            return
        }
        purchases.forEach { purchase ->
            if (purchase.products.contains(PREMIUM_PRODUCT_ID) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            ) {
                updatePremium(true)
                if (!purchase.isAcknowledged) acknowledge(purchase)
            }
        }
    }

    private fun acknowledge(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        scope.launch {
            val result = billingClient.acknowledgePurchase(params)
            Log.d(TAG, "Acknowledged: ${result.responseCode}")
        }
    }

    private fun updatePremium(isOwned: Boolean) {
        if (_isPremium.value == isOwned) return
        _isPremium.value = isOwned
        scope.launch {
            settingsRepository.setAdsDisabled(isOwned)
        }
    }
}

/** Composition-local hook so any composable can launch the purchase flow. */
val LocalBillingController = compositionLocalOf<BillingController?> { null }
