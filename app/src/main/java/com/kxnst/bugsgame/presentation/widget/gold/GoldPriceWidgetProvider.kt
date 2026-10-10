package com.kxnst.bugsgame.presentation.widget.gold

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews

import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.data.gold.CbrGoldPriceNetworkFactory
import com.kxnst.bugsgame.data.gold.CbrGoldPriceRepository
import com.kxnst.bugsgame.domain.gold.GoldPrice
import com.kxnst.bugsgame.domain.gold.GoldPriceTrend

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import java.util.Locale
import kotlin.math.roundToLong

open class GoldPriceWidgetProvider : AppWidgetProvider() {
    protected open val showsDetailedPrices: Boolean = false

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds, forceRefresh = true)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidgets(context, appWidgetManager, intArrayOf(appWidgetId), forceRefresh = false)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, javaClass)
            val widgetIds = manager.getAppWidgetIds(component)

            updateWidgets(context, manager, widgetIds, forceRefresh = true)
        }
    }

    private fun updateWidgets(
        context: Context,
        manager: AppWidgetManager,
        widgetIds: IntArray,
        forceRefresh: Boolean
    ) {
        if (widgetIds.isEmpty()) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = CbrGoldPriceRepository(
                    api = CbrGoldPriceNetworkFactory.createApi(),
                    context = context.applicationContext
                )
                val price = repository.getLatestGoldPrice(forceRefresh)

                widgetIds.forEach { widgetId ->
                    manager.updateAppWidget(widgetId, createRemoteViews(context, price))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun createRemoteViews(
        context: Context,
        price: GoldPrice?
    ): RemoteViews {
        val isDetailed = showsDetailedPrices
        val views = RemoteViews(
            context.packageName,
            if (isDetailed) R.layout.widget_gold_price_detailed else R.layout.widget_gold_price_compact
        )

        setRefreshPendingIntent(context, views, isDetailed)

        if (price == null) {
            if (isDetailed) {
                val unavailablePrices = context.getString(R.string.gold_widget_buy_unavailable) + "\n" +
                    context.getString(R.string.gold_widget_sell_unavailable)
                views.setTextViewText(R.id.tvGoldPricesDetailed, unavailablePrices)
                views.setViewVisibility(R.id.ivGoldTrendDetailed, View.GONE)
            } else {
                views.setTextViewText(R.id.tvGoldPriceCompact, context.getString(R.string.gold_widget_price_unavailable))
                views.setViewVisibility(R.id.ivGoldTrendCompact, View.GONE)
            }

            return views
        }

        if (isDetailed) {
            val prices = context.getString(
                R.string.gold_widget_buy,
                formatPrice(price.buyRublesPerGram)
            ) + "\n" + context.getString(
                R.string.gold_widget_sell,
                formatPrice(price.sellRublesPerGram)
            )
            views.setTextViewText(R.id.tvGoldPricesDetailed, prices)
            applyTrend(
                views,
                R.id.ivGoldTrendDetailed,
                R.drawable.ic_gold_trend_up,
                R.drawable.ic_gold_trend_down,
                price.trend
            )
        } else {
            views.setTextViewText(
                R.id.tvGoldPriceCompact,
                formatPrice(price.buyRublesPerGram)
            )
            applyTrend(
                views,
                R.id.ivGoldTrendCompact,
                R.drawable.ic_gold_trend_up,
                R.drawable.ic_gold_trend_down,
                price.trend
            )
        }

        return views
    }

    private fun applyTrend(
        views: RemoteViews,
        viewId: Int,
        upResourceId: Int,
        downResourceId: Int,
        trend: GoldPriceTrend
    ) {
        when (trend) {
            GoldPriceTrend.UP -> {
                views.setImageViewResource(viewId, upResourceId)
                views.setViewVisibility(viewId, View.VISIBLE)
            }

            GoldPriceTrend.DOWN -> {
                views.setImageViewResource(viewId, downResourceId)
                views.setViewVisibility(viewId, View.VISIBLE)
            }

            GoldPriceTrend.STABLE -> {
                views.setViewVisibility(viewId, View.GONE)
            }
        }
    }

    private fun setRefreshPendingIntent(
        context: Context,
        views: RemoteViews,
        isDetailed: Boolean
    ) {
        val refreshIntent = Intent(context, javaClass).setAction(ACTION_REFRESH)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            if (isDetailed) DETAILED_PENDING_INTENT_REQUEST else COMPACT_PENDING_INTENT_REQUEST,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(
            if (isDetailed) R.id.widgetGoldRootDetailed else R.id.widgetGoldRootCompact,
            pendingIntent
        )
    }

    private fun formatPrice(price: Double): String {
        return String.format(Locale.ROOT, "%d₽", price.roundToLong())
    }

    private companion object {
        const val ACTION_REFRESH = "com.kxnst.bugsgame.action.REFRESH_GOLD_WIDGET"
        const val COMPACT_PENDING_INTENT_REQUEST = 701
        const val DETAILED_PENDING_INTENT_REQUEST = 702
    }
}
