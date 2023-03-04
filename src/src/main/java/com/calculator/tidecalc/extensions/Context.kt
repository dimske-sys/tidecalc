package com.calculator.tidecalc.extensions

import android.content.Context
import android.widget.Toast


fun Context.toast(message: CharSequence) {
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}
