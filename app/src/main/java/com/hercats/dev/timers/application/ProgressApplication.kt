package com.hercats.dev.timers.application

import android.app.Application
import com.tencent.mmkv.MMKV
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

class ProgressApplication: Application(), AnkoLogger {
    override fun onCreate() {
        super.onCreate()
        //init mmkv
        val rootDir = MMKV.initialize(applicationContext)
        debug(rootDir)
    }
}