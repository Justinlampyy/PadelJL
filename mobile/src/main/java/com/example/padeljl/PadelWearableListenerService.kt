package com.example.padeljl

import android.content.Context
import androidx.core.content.edit
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PadelWearableListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                val map = DataMapItem.fromDataItem(event.dataItem).dataMap
                
                if (path == "/match_finished") {
                    val timestamp = map.getLong("timestamp", 0L)
                    val date = map.getLong("date", System.currentTimeMillis())
                    val finalScore = map.getString("finalScore", "")
                    
                    if (timestamp != 0L) {
                        saveMatchToHistory(timestamp.toString(), date, finalScore)
                    }
                }
            }
        }
    }

    private fun saveMatchToHistory(id: String, date: Long, finalScore: String) {
        val sharedPref = getSharedPreferences("padel_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPref.getString("match_history", null)
        
        val type = object : TypeToken<MutableList<MatchResult>>() {}.type
        val history: MutableList<MatchResult> = if (json != null) {
            try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }

        // Check of de wedstrijd al bestaat op basis van ID
        if (history.none { it.id == id }) {
            val result = MatchResult(
                id = id,
                date = date,
                finalScore = finalScore
            )
            history.add(0, result)
            sharedPref.edit { putString("match_history", gson.toJson(history)) }
        }
    }
}
