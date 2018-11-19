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
import com.hercats.dev.timers.entity.getThisYearProgress
import com.hercats.dev.timers.entity.json
import com.hercats.dev.timers.entity.parseProgress
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import java.util.*

class MainActivity : AppCompatActivity(), AnkoLogger {

    // 2018 REQUEST CODE
    private val ADD_PROGRESS_REQUEST_CODE = 8102

    private lateinit var adapter: ProgressAdapter
    private val progresses = mutableListOf<Progress>()
    private val mmkv = MMKV.defaultMMKV()
    private var update = UpdateProgress()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }
//    override fun onStart() {
//        super.onStart()
//    }

    override fun onResume() {
        super.onResume()
        try {
            // try to start an reserve async task
            this.update.execute()
        } catch (e: Exception) {
            e.printStackTrace()
            // get a new instance
            this.update = UpdateProgress()
            this.update.execute()
        }
    }

    override fun onPause() {
        super.onPause()
        this.update.cancel(true)
    }

    override fun onStop() {
        super.onStop()
        this.update.cancel(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.update.cancel(true)
    }



    override fun onBackPressed() {
        super.onBackPressed()
        this.update.cancel(true)
    }

    private fun init() {
        // init the adapter for display progress
        this.adapter = ProgressAdapter(progresses, applicationContext)
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
                aJsonArray.put(getThisYearProgress().json())
                if (mmkv.encode("progress", aJsonArray.toString())) {
                    this.progresses.add(getThisYearProgress())
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
        startActivityForResult(intent, ADD_PROGRESS_REQUEST_CODE)
    }

    inner class UpdateProgress : AsyncTask<Void, Void, Void?>() {
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
            this.progresses.clear()
            this.progresses.addAll(parseProgress(mmkv.decodeString("progress", "")))
            this.adapter.notifyDataSetChanged()
        }
    }
}
