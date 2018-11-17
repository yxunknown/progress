package com.hercats.dev.timers.activities

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.hercats.dev.timers.R
import com.hercats.dev.timers.entity.Progress
import com.hercats.dev.timers.entity.json
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_add_progress.*
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

class AddProgressActivity : AppCompatActivity() {
    private val mmkv = MMKV.defaultMMKV()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_progress)
    }

    fun close(view: View) {
        onBackPressed()
    }

    fun addProgress(view: View) {
        val name = edt_name.text.toString()
        val startTime = edt_start_time.text.toString()
        val endTime = edt_end_time.text.toString()
        fun validate(): Boolean {
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.CHINA)
            if (name.isBlank()) {
                snack("name cant not be blank!")
                return false
            }
            if (startTime.isBlank()) {
                snack("start time can not be blank")
                return false
            }
            if (endTime.isBlank()) {
                snack("end time can not be blank")
            }

            val sd = try {
                dateFormat.parse(startTime)
            } catch (e: ParseException) {
                snack("start time is invalid")
                null
            }
            val ed = try {
                dateFormat.parse(endTime)
            } catch (e: ParseException) {
                snack("end time is invalid")
                null
            }
            return ed != null && sd != null && sd.time < ed.time
        }
        if (validate()) {
            try {
                val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.CHINA)
                val progress = Progress(
                    name = name,
                    startTime = dateFormatter.parse(startTime),
                    endTime = dateFormatter.parse(endTime)
                )
                val progressJsonArrayString = mmkv.decodeString("progress", "")
                if (progressJsonArrayString.isBlank()) {
                    val progressJsonArray = JSONArray()
                    progressJsonArray.put(progress.json())
                    addProgressToMMKV(progressJsonArray.toString())
                } else {
                    val progressJsonArray = JSONArray(progressJsonArrayString)
                    progressJsonArray.put(progress.json())
                    addProgressToMMKV(progressJsonArray.toString())
                }
            } catch (e: Exception) {
                snack("add new progress with error: ${e.message}.")
            }
        } else {
            snack("your input is invalid!")
        }
    }

    private fun addProgressToMMKV(progressJsonArrayStr: String) {
        if (mmkv.encode("progress", progressJsonArrayStr)) {
            snack("add progress success.")
            // return to pre activity
            startActivity(intentFor<MainActivity>())
            finish()
        } else {
            snack("add progress failed.")
        }
    }

}
