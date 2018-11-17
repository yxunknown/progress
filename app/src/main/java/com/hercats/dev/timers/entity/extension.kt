package com.hercats.dev.timers.entity

import org.json.JSONArray
import org.json.JSONObject
import java.util.*

fun Progress.json(): JSONObject {
    val json = JSONObject()
    json.put("name", name)
    json.put("startTime", startTime.time)
    json.put("endTime", endTime.time)
    return json
}

fun parseProgress(progressJsonArrayStr: String): List<Progress> {
    return try {
        val array = JSONArray(progressJsonArrayStr)
        val progresses = mutableListOf<Progress>()
        for (index in 0 until array.length()) {
            val progress = Progress(
                name = array.getJSONObject(index).getString("name"),
                startTime = Date(array.getJSONObject(index).getLong("startTime")),
                endTime = Date(array.getJSONObject(index).getLong("endTime"))
            )
            progresses.add(progress)
        }
        progresses
    } catch (e: Exception) {
        e.printStackTrace()
        listOf()
    }
}