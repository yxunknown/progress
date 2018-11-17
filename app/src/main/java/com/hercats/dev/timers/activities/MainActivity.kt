package com.hercats.dev.timers.activities

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.hercats.dev.timers.R
import com.hercats.dev.timers.adapter.ProgressAdapter
import com.hercats.dev.timers.entity.Progress
import com.hercats.dev.timers.entity.json
import com.hercats.dev.timers.entity.parseProgress
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.themedRelativeLayout
import org.json.JSONArray
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity(), AnkoLogger {

    // 2018 REQUEST CODE
    private val ADD_PROGRESS_REQUEST_CODE = 8102

    private lateinit var adapter: ProgressAdapter
    private val progresses = mutableListOf<Progress>()
    private val mmkv = MMKV.defaultMMKV()
    private val update = UpdateProgress()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        this.update.execute()
    }
//    override fun onStart() {
//        super.onStart()
//    }

    override fun onPause() {
        super.onPause()
        this.update.cancel(true)
    }

    private fun init() {
        // init the adapter for display progress
        adapter = ProgressAdapter(progresses, applicationContext)
        // bind adapter to list view
        lv_progress.adapter = this.adapter

        // load progress data from mmkv storage or add the default progress
        if (mmkv.containsKey("progress")) {
            val progressJsonArrayStr = mmkv.decodeString("progress", "")
            this.progresses.addAll(parseProgress(progressJsonArrayStr))
            this.adapter.notifyDataSetChanged()
        } else {
            /**
            * application is first running on this device
            * so let's add this year progress for user
            * when you read below comment: (fuck, who will read this code except you)
            * you will find that below comment is damn shit
            * ohh, thanks for your consideration
            * the weather is so cold and i don't have a fuck girlfriend
            * why am I coding here on a weekend time?
            * fuck the dream
            * ok, let's add this year progress for user
            */
            try {
                // frankly, I hate this json library
                val aJsonArray = JSONArray()
                // fuck android, the Instant only supported over api 26
                val currentDate = Date()
                // believe it or not
                // the below code is work when test
                val thisYear = Date(currentDate.year, 0,1, 0, 0, 0)
                val nextYear = Date(currentDate.year + 1, 0, 1, 0, 0, 0)
                val thisYearCalendar = Calendar.getInstance()
                val progress = Progress(
                    name = "${thisYearCalendar.get(Calendar.YEAR)} progress",
                    startTime = thisYear,
                    endTime = nextYear
                )
                aJsonArray.put(progress.json())
                if (mmkv.encode("progress", aJsonArray.toString())) {
                    this.progresses.add(progress)
                    this.adapter.notifyDataSetChanged()
                } else {
                    // store default value to mmkv storage failed
                    // i have no idea on what to do
                    snack("Load progress failed. I'm sorry for what happening.")
                }
            } catch (e: Exception) {
                snack("We encounter an error: ${e.message}.")
            }
        }
    }

    fun addProgress(view: View) {
        val intent = intentFor<AddProgressActivity>()
        this.update.cancel(true)
        startActivity(intent)
        finish()
    }

    inner class UpdateProgress: AsyncTask<Void, Void, Void?>() {
        override fun onProgressUpdate(vararg values: Void?) {
            this@MainActivity.adapter.notifyDataSetChanged()
            tv_current.setText(Date().toString(), true)
        }

        override fun doInBackground(vararg params: Void?): Void? {
            while (true) {
                publishProgress()
                if (isCancelled) {
                    break
                }
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    break
                }
            }
            return null
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ADD_PROGRESS_REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            this.progresses.removeAll(this.progresses)
            this.progresses.addAll(parseProgress(mmkv.decodeString("progress", "")))
        }
    }
}
