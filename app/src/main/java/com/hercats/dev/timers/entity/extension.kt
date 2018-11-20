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

fun getThisYearProgress(): Progress {
    val currentDate = Date()
    // believe it or not
    // the below code is work when test
    val thisYear = Date(currentDate.year, 0, 1, 0, 0, 0)
    val nextYear = Date(currentDate.year + 1, 0, 1, 0, 0, 0)
    val thisYearCalendar = Calendar.getInstance()
    return Progress(
        name = "${thisYearCalendar.get(Calendar.YEAR)} progress",
        startTime = thisYear,
        endTime = nextYear
    )
}

fun parseJsonToProgress(progressJsonStr: String): Progress {
    return try {
        val jsonProgress = JSONObject(progressJsonStr)
        Progress(
            name = jsonProgress.getString("name"),
            startTime = Date(jsonProgress.getLong("startTime")),
            endTime = Date(jsonProgress.getLong("endTime"))
        )
    } catch (e: java.lang.Exception) {
        getThisYearProgress()
    }
}
fun getProgress(progress: Progress): Double {
    val totalTime = progress.endTime.time - progress.startTime.time
    val nowTime = Date().time - progress.startTime.time
    val progressValue = nowTime.toDouble() * 100 / totalTime.toDouble()
    return when {
        (progressValue > 100) -> 100.00
        (progressValue < 0) -> 0.00
        else -> progressValue
    }
}
