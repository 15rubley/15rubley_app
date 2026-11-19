package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.courierDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "courier_preferences"
)

data class CourierPrefs(
    val isDarkTheme: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val soundsEnabled: Boolean = true,
    val garageMusicEnabled: Boolean = true,
    val garageTrackId: String = GarageTrack.GET_LOW_CRUNK.id,
    val garageAutoMix: Boolean = true,
    val showGearboxWidget: Boolean = true,
    val lifetimeTreatmentRub: Int = 15,
    val spentRub: Int = 0,
    val purchasedPadIds: Set<String> = setOf("stock_rubber"),
    val equippedPadId: String = "stock_rubber",
    val brakeHealthPercent: Int = 38,
    val ownedBikeModIds: Set<String> = emptySet(),
    val equippedBikeModIds: Set<String> = emptySet(),
    val accumulatedBreakdownsRaw: String = "",
    val lifetimeDeliveries: Int = 0,
    val isOnShift: Boolean = true,
    val shiftAccumulatedMs: Long = 0L,
    val shiftActiveSinceMs: Long = -1L,
    val shiftFirstStartedMs: Long = -1L
)

class CourierPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SOUNDS_ENABLED = booleanPreferencesKey("sounds_enabled")
        val GARAGE_MUSIC_ENABLED = booleanPreferencesKey("garage_music_enabled")
        val GARAGE_TRACK_ID = stringPreferencesKey("garage_track_id")
        val GARAGE_AUTO_MIX = booleanPreferencesKey("garage_auto_mix")
        val SHOW_GEARBOX_WIDGET = booleanPreferencesKey("show_gearbox_widget")
        val LIFETIME_TREATMENT_RUB = intPreferencesKey("lifetime_treatment_rub")
        val SPENT_RUB = intPreferencesKey("spent_rub")
        val PURCHASED_PAD_IDS = stringSetPreferencesKey("purchased_pad_ids")
        val EQUIPPED_PAD_ID = stringPreferencesKey("equipped_pad_id")
        val BRAKE_HEALTH_PERCENT = intPreferencesKey("brake_health_percent")
        val OWNED_BIKE_MOD_IDS = stringSetPreferencesKey("owned_bike_mod_ids")
        val EQUIPPED_BIKE_MOD_IDS = stringSetPreferencesKey("equipped_bike_mod_ids")
        val ACCUMULATED_BREAKDOWNS_RAW = stringPreferencesKey("accumulated_breakdowns_raw")
        val LIFETIME_DELIVERIES = intPreferencesKey("lifetime_deliveries")
        val IS_ON_SHIFT = booleanPreferencesKey("is_on_shift")
        val SHIFT_ACCUMULATED_MS = longPreferencesKey("shift_accumulated_ms")
        val SHIFT_ACTIVE_SINCE_MS = longPreferencesKey("shift_active_since_ms")
        val SHIFT_FIRST_STARTED_MS = longPreferencesKey("shift_first_started_ms")
    }

    val preferences: Flow<CourierPrefs> = context.courierDataStore.data.map { prefs ->
        CourierPrefs(
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            soundsEnabled = prefs[Keys.SOUNDS_ENABLED] ?: true,
            garageMusicEnabled = prefs[Keys.GARAGE_MUSIC_ENABLED] ?: true,
            garageTrackId = prefs[Keys.GARAGE_TRACK_ID] ?: GarageTrack.GET_LOW_CRUNK.id,
            garageAutoMix = prefs[Keys.GARAGE_AUTO_MIX] ?: true,
            showGearboxWidget = prefs[Keys.SHOW_GEARBOX_WIDGET] ?: true,
            lifetimeTreatmentRub = prefs[Keys.LIFETIME_TREATMENT_RUB] ?: 15,
            spentRub = prefs[Keys.SPENT_RUB] ?: 0,
            purchasedPadIds = prefs[Keys.PURCHASED_PAD_IDS] ?: setOf("stock_rubber"),
            equippedPadId = prefs[Keys.EQUIPPED_PAD_ID] ?: "stock_rubber",
            brakeHealthPercent = prefs[Keys.BRAKE_HEALTH_PERCENT] ?: 38,
            ownedBikeModIds = prefs[Keys.OWNED_BIKE_MOD_IDS] ?: emptySet(),
            equippedBikeModIds = prefs[Keys.EQUIPPED_BIKE_MOD_IDS] ?: emptySet(),
            accumulatedBreakdownsRaw = prefs[Keys.ACCUMULATED_BREAKDOWNS_RAW] ?: "",
            lifetimeDeliveries = prefs[Keys.LIFETIME_DELIVERIES] ?: 0,
            isOnShift = prefs[Keys.IS_ON_SHIFT] ?: true,
            shiftAccumulatedMs = prefs[Keys.SHIFT_ACCUMULATED_MS] ?: 0L,
            shiftActiveSinceMs = prefs[Keys.SHIFT_ACTIVE_SINCE_MS] ?: -1L,
            shiftFirstStartedMs = prefs[Keys.SHIFT_FIRST_STARTED_MS] ?: -1L
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.courierDataStore.edit { it[Keys.IS_DARK_THEME] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.courierDataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setSoundsEnabled(enabled: Boolean) {
        context.courierDataStore.edit { it[Keys.SOUNDS_ENABLED] = enabled }
    }

    suspend fun setShowGearboxWidget(enabled: Boolean) {
        context.courierDataStore.edit { it[Keys.SHOW_GEARBOX_WIDGET] = enabled }
    }

    suspend fun addTreatmentRub(amount: Int) {
        context.courierDataStore.edit { prefs ->
            val current = prefs[Keys.LIFETIME_TREATMENT_RUB] ?: 15
            prefs[Keys.LIFETIME_TREATMENT_RUB] = current + amount
        }
    }

    suspend fun purchaseAndEquipPad(padId: String, priceRub: Int, newHealth: Int) {
        context.courierDataStore.edit { prefs ->
            val currentSpent = prefs[Keys.SPENT_RUB] ?: 0
            val currentOwned = prefs[Keys.PURCHASED_PAD_IDS] ?: setOf("stock_rubber")
            prefs[Keys.SPENT_RUB] = currentSpent + priceRub
            prefs[Keys.PURCHASED_PAD_IDS] = currentOwned + padId
            prefs[Keys.EQUIPPED_PAD_ID] = padId
            prefs[Keys.BRAKE_HEALTH_PERCENT] = newHealth.coerceIn(10, 100)
        }
    }

    suspend fun equipPad(padId: String, newHealth: Int) {
        context.courierDataStore.edit { prefs ->
            prefs[Keys.EQUIPPED_PAD_ID] = padId
            prefs[Keys.BRAKE_HEALTH_PERCENT] = newHealth.coerceIn(10, 100)
        }
    }

    suspend fun setBrakeHealth(percent: Int) {
        context.courierDataStore.edit { it[Keys.BRAKE_HEALTH_PERCENT] = percent.coerceIn(5, 100) }
    }

    suspend fun spendAndRestoreBrakes(costRub: Int, newHealth: Int) {
        context.courierDataStore.edit { prefs ->
            val currentSpent = prefs[Keys.SPENT_RUB] ?: 0
            prefs[Keys.SPENT_RUB] = currentSpent + costRub.coerceAtLeast(0)
            prefs[Keys.BRAKE_HEALTH_PERCENT] = newHealth.coerceIn(5, 100)
        }
    }

    suspend fun setGarageMusicEnabled(enabled: Boolean) {
        context.courierDataStore.edit { it[Keys.GARAGE_MUSIC_ENABLED] = enabled }
    }

    suspend fun setGarageTrackId(trackId: String) {
        context.courierDataStore.edit { it[Keys.GARAGE_TRACK_ID] = trackId }
    }

    suspend fun setGarageAutoMix(enabled: Boolean) {
        context.courierDataStore.edit { it[Keys.GARAGE_AUTO_MIX] = enabled }
    }

    suspend fun purchaseAndEquipBikeMod(modId: String, priceRub: Int) {
        context.courierDataStore.edit { prefs ->
            val currentSpent = prefs[Keys.SPENT_RUB] ?: 0
            val owned = prefs[Keys.OWNED_BIKE_MOD_IDS] ?: emptySet()
            val equipped = prefs[Keys.EQUIPPED_BIKE_MOD_IDS] ?: emptySet()
            prefs[Keys.SPENT_RUB] = currentSpent + priceRub.coerceAtLeast(0)
            prefs[Keys.OWNED_BIKE_MOD_IDS] = owned + modId
            prefs[Keys.EQUIPPED_BIKE_MOD_IDS] = equipped + modId
        }
    }

    suspend fun toggleBikeModEquipped(modId: String) {
        context.courierDataStore.edit { prefs ->
            val equipped = prefs[Keys.EQUIPPED_BIKE_MOD_IDS] ?: emptySet()
            prefs[Keys.EQUIPPED_BIKE_MOD_IDS] = if (equipped.contains(modId)) {
                equipped - modId
            } else {
                equipped + modId
            }
        }
    }

    suspend fun setAccumulatedBreakdownsRaw(raw: String) {
        context.courierDataStore.edit { it[Keys.ACCUMULATED_BREAKDOWNS_RAW] = raw }
    }

    suspend fun incrementDeliveries() {
        context.courierDataStore.edit { prefs ->
            val current = prefs[Keys.LIFETIME_DELIVERIES] ?: 0
            prefs[Keys.LIFETIME_DELIVERIES] = current + 1
        }
    }

    suspend fun saveShiftTimerState(
        isOnShift: Boolean,
        accumulatedMs: Long,
        activeSinceMs: Long,
        firstStartedMs: Long
    ) {
        context.courierDataStore.edit { prefs ->
            prefs[Keys.IS_ON_SHIFT] = isOnShift
            prefs[Keys.SHIFT_ACCUMULATED_MS] = accumulatedMs
            prefs[Keys.SHIFT_ACTIVE_SINCE_MS] = activeSinceMs
            prefs[Keys.SHIFT_FIRST_STARTED_MS] = firstStartedMs
        }
    }
}
