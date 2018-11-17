package com.hercats.dev.timers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ProgressBar
import android.widget.TextView
import com.hercats.dev.timers.R
import com.hercats.dev.timers.entity.Progress
import com.robinhood.ticker.TickerView
import java.util.*

class ProgressAdapter(private val progresses: List<Progress>,
                      private val context: Context): BaseAdapter() {

    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: inflater.inflate(R.layout.progress_item, null)
        val name = view.findViewById<TextView>(R.id.tv_name)
        val progress = view.findViewById<ProgressBar>(R.id.pb_progress)
        val progressInfo = view.findViewById<TickerView>(R.id.tv_progress)
        name.text = progresses[position].name
        progress.progress = getProgress(position).toInt()
        progressInfo.setText("${getProgress(position)}%", true)
        return view
    }

    override fun getItem(position: Int) = progresses[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = progresses.size

    private fun getProgress(position: Int): Double {
        val p = progresses[position]
        val totalTime = p.endTime.time - p.startTime.time
        val nowTime = Date().time - p.startTime.time
        return nowTime.toDouble() * 100 / totalTime.toDouble()
    }
}