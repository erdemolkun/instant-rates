/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dynoapps.exchange_rates.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

/**
 * An item animator that fades added items in while sliding them from [slideFromEdge]; by default
 * they slide upward from the bottom.
 */
class SlideInItemAnimator(slideFromEdge: Int = Gravity.BOTTOM, layoutDirection: Int = -1) :
    DefaultItemAnimator() {

    private val pendingAdds = ArrayList<RecyclerView.ViewHolder>()
    private val slideFromEdge: Int = Gravity.getAbsoluteGravity(slideFromEdge, layoutDirection)

    init {
        addDuration = 160L
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        val view = holder.itemView
        view.alpha = 0f
        when (slideFromEdge) {
            Gravity.LEFT -> view.translationX = -view.width / 3f
            Gravity.TOP -> view.translationY = -view.height / 3f
            Gravity.RIGHT -> view.translationX = view.width / 3f
            else -> view.translationY = view.height / 3f
        }
        pendingAdds.add(holder)
        return true
    }

    override fun runPendingAnimations() {
        super.runPendingAnimations()
        for (i in pendingAdds.indices.reversed()) {
            val holder = pendingAdds[i]
            holder.itemView.animate()
                .alpha(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(addDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        dispatchAddStarting(holder)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        animation.removeListener(this)
                        dispatchAddFinished(holder)
                        dispatchFinishedWhenDone()
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        clearAnimatedValues(holder.itemView)
                    }
                })
                .setInterpolator(
                    AnimationUtils.loadInterpolator(
                        holder.itemView.context,
                        android.R.interpolator.accelerate_decelerate,
                    )
                )
            pendingAdds.removeAt(i)
        }
    }

    override fun endAnimation(holder: RecyclerView.ViewHolder) {
        holder.itemView.animate().cancel()
        if (pendingAdds.remove(holder)) {
            dispatchAddFinished(holder)
            clearAnimatedValues(holder.itemView)
        }
        super.endAnimation(holder)
    }

    override fun endAnimations() {
        for (i in pendingAdds.indices.reversed()) {
            val holder = pendingAdds[i]
            clearAnimatedValues(holder.itemView)
            dispatchAddFinished(holder)
            pendingAdds.removeAt(i)
        }
        super.endAnimations()
    }

    override fun isRunning(): Boolean = pendingAdds.isNotEmpty() || super.isRunning()

    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    private fun clearAnimatedValues(view: View) {
        view.alpha = 1f
        view.translationX = 0f
        view.translationY = 0f
    }
}
