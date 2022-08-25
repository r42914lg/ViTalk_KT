package com.r42914lg.arkados.vitalk.ui

import android.animation.Animator

import android.animation.AnimatorListenerAdapter
import android.view.View

object AnimatorHelper {
    fun animateView(view: View?, toVisibility: Int, toAlpha: Float, duration: Int) {
        if (view == null)
            return

        val show = toVisibility == View.VISIBLE
        if (show) {
            view.alpha = 0f
        }

        view.visibility = View.VISIBLE

        view.animate()
            .setDuration(duration.toLong())
            .alpha(if (show) toAlpha else 0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = toVisibility
                }
            })
    }
}