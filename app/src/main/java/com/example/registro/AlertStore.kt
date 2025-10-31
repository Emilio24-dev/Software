package com.example.registro

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object AlertStore {

    private const val PREF_NAME = "sentri_alerts_prefs"
    private const val KEY_ALERTS = "alerts_list"


    fun addAlert(ctx: Context, alert: AlertItem) {
        val list = getAlerts(ctx).toMutableList()
        list.add(alert)
        saveAlerts(ctx, list)
    }


    fun getAlerts(ctx: Context): List<AlertItem> {
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_ALERTS, "[]") ?: "[]"

        val arr = JSONArray(jsonStr)
        val out = ArrayList<AlertItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            out.add(
                AlertItem(
                    message = obj.optString("message", ""),
                    cam = obj.optString("cam", ""),
                    fecha = obj.optString("fecha", ""),
                    horaInicio = obj.optString("horaInicio", ""),
                    horaFin = obj.optString("horaFin", "")
                )
            )
        }
        return out
    }

    private fun saveAlerts(ctx: Context, alerts: List<AlertItem>) {
        val arr = JSONArray()
        for (a in alerts) {
            val o = JSONObject()
            o.put("message", a.message)
            o.put("cam", a.cam)
            o.put("fecha", a.fecha)
            o.put("horaInicio", a.horaInicio)
            o.put("horaFin", a.horaFin)
            arr.put(o)
        }
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ALERTS, arr.toString()).apply()
    }
}
