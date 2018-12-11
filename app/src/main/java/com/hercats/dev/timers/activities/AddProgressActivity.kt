package com.hercats.dev.timers.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
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
        val edtFocusListener = EdtFocusListener()
        edt_start_time.onFocusChangeListener = edtFocusListener
        edt_end_time.onFocusChangeListener = edtFocusListener
    }

    fun close(view: View) {
        finish()
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
        }
    }

    private fun addProgressToMMKV(progressJsonArrayStr: String) {
        if (mmkv.encode("progress", progressJsonArrayStr)) {
            snack("add progress success.")
            // return to pre activity
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            snack("add progress failed.")
        }
    }

    inner class EdtFocusListener: View.OnFocusChangeListener {
        private val dateFormater = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            v as EditText
            val currentDate = if (!v.text.toString().isBlank()) {
                dateFormater.parse(v.text.toString())
            } else {
                Date()
            }
            if (hasFocus) {
                // cancel input method popup
                val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                im.hideSoftInputFromWindow(edt_name.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                val timePicker = TimePickerBuilder(this@AddProgressActivity) {
                    date, _ ->
                    v.setText(dateFormater.format(date))
                }
                    .setDate(Calendar.getInstance().apply { time = currentDate })
                    .setTitleText("chose datetime")
                    .setCancelText("Cancel")
                    .setSubmitText("Ok")
                    .setCancelColor(Color.BLACK)
                    .setSubmitColor(Color.BLACK)
                    .isCyclic(true)
                    .setType(arrayOf(true, true, true, true, true, true).toBooleanArray())
                    .setTextColorCenter(Color.parseColor("#0070FF"))
                    .build()
                timePicker.show()
            }
        }
    }

}
