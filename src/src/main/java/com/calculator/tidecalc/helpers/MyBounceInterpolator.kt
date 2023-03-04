package com.calculator.tidecalc.helpers

import android.view.animation.Interpolator
import kotlin.math.cos
import kotlin.math.pow

class MyBounceInterpolator(var mAmplitude: Double, var mFrequency: Int) : Interpolator {
    override fun getInterpolation(time: Float): Float {
        return (-1 * Math.E.pow(-time / mAmplitude) *
                cos(mFrequency * time) + 1).toFloat()
    }
}