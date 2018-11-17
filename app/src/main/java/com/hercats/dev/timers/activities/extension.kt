package com.hercats.dev.timers.activities

import android.app.Activity
import android.support.design.widget.Snackbar

fun Activity.snack(msg: String) {
    Snackbar.make(this.window.peekDecorView(), msg, Snackbar.LENGTH_SHORT).show()
}