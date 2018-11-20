package com.hercats.dev.timers.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.BaseAdapter
import android.widget.ProgressBar
import android.widget.TextView
import com.hercats.dev.timers.R
import com.hercats.dev.timers.entity.Progress
import com.hercats.dev.timers.entity.getProgress
import com.hercats.dev.timers.entity.json
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import com.robinhood.ticker.TickerView
import com.tencent.mmkv.MMKV
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.*

class ProgressAdapter(private val progresses: MutableList<Progress>,
                      private val context: Context): BaseAdapter() {

    private val inflater = LayoutInflater.from(context)
    private val mmkv = MMKV.defaultMMKV()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: inflater.inflate(R.layout.progress_item, null)
        val name = view.findViewById<TextView>(R.id.tv_name)
        val progress = view.findViewById<ProgressBar>(R.id.pb_progress)
        val progressInfo = view.findViewById<TickerView>(R.id.tv_progress)
        name.text = progresses[position].name
        val progressValue = getProgress(this.progresses[position])
        progress.progress = progressValue.toInt()
        progressInfo.setText("$progressValue%", true)
        val collectBtn = view.findViewById<View>(R.id.btn_collect)
        val deleteBtn = view.findViewById<View>(R.id.btn_delete)
        val swipeMenu = view.findViewById<SwipeMenuLayout>(R.id.swipe_menu)
        collectBtn.setOnClickListener {
            mmkv.encode("widget_progress", progresses[position].json().toString())
            swipeMenu.smoothClose()
        }
        deleteBtn.setOnClickListener {
            // delete from ui
            this.progresses.removeAt(position)
            // update ui
            notifyDataSetChanged()
            // modify storage data
            try {
                val progressArrayStr = mmkv.decodeString("progress", "")
                val progressJsonArray = JSONArray(progressArrayStr)
                val deletedProgress = progressJsonArray.remove(position) as JSONObject
                val oldWidgetProgress = mmkv.decodeString("widget_progress", "")
                if (deletedProgress.toString() == oldWidgetProgress) {
                    // delete the progress that widget used too
                    // so set the widget progress to default progress
                    mmkv.encode("widget_progress", "")
                }
            } catch (e: Exception) {
                context.toast("delete progress failed")
            }
            swipeMenu.smoothClose()
        }
        return view
    }

    override fun getItem(position: Int) = progresses[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = progresses.size
}