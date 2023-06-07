package com.example.youtube.commun

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.youtube.R
import kotlin.math.abs

class TouchMotion(context: Context, attr: AttributeSet) : MotionLayout(context, attr) {

    private val framePlayer by lazy {
        findViewById<View>(R.id.frame_player)
    }

    private val imgBase by lazy {
        findViewById<ImageView>(R.id.video_player)
    }

    private val hidePlayer by lazy {
        findViewById<ImageView>(R.id.hide_player)
    }

    private val playlistPlayer by lazy {
        findViewById<ImageView>(R.id.playlist_player)
    }

    private val sharePlayer by lazy {
        findViewById<ImageView>(R.id.share_player)
    }

    private val morePlayer by lazy {
        findViewById<ImageView>(R.id.more_player)
    }

    private val fullPlayer by lazy {
        findViewById<ImageView>(R.id.full_player)
    }

    private val durationPlayer by lazy {
        findViewById<TextView>(R.id.duration_player)
    }

    private val currentPlayer by lazy {
        findViewById<TextView>(R.id.current_player)
    }

    private val playButton by lazy {
        findViewById<ImageView>(R.id.play_button)
    }

    private val nextButton by lazy {
        findViewById<ImageView>(R.id.next_button)
    }

    private val previousButton by lazy {
        findViewById<ImageView>(R.id.previous_button)
    }

    private val seekBar by lazy {
        findViewById<SeekBar>(R.id.seek_bar)
    }

    private var startX: Float? = null
    private var startY: Float? = null
    private var isPaused: Boolean = false

    private var animatorFadeIn: AnimatorSet? = null
    private var animatorFadeOut: AnimatorSet? = null

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        val isInTarget = touchEventInsideTargetView(imgBase, event!!)
        val isInProgress = (progress > 0.0f && progress < 1.0f)

        return if (isInTarget && isInProgress) {
            super.onInterceptTouchEvent(event)
        } else {
            false
        }
    }

    private fun touchEventInsideTargetView(v: View, ev: MotionEvent) : Boolean{
        if(ev.x > v.left && ev.x < v.right) {
            if (ev.y > v.top && ev.y < v.bottom){
                return true
            }
        }

        return false
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when(ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x
                startY = ev.y
            }
            MotionEvent.ACTION_UP -> {
                val endX = ev.x
                val endY = ev.y

                if (isAClick(startX!!, endX, startY!!, endY)) {
                    if (touchEventInsideTargetView(imgBase, ev)) {
                        if(doClick(imgBase)) {
                            return true
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float) : Boolean {
        val differenceX = abs(startX - endX)
        val differenceY = abs(startY - endY)

        return !(differenceX > 200 || differenceY > 200)
    }
    private fun doClick(view: View) : Boolean {
        var isClickHandler = false

        if (progress < 0.05f) {
            isClickHandler = true

            when(view) {
                imgBase -> {
                    if (isPaused) {

                    } else {
                        animateFade {
                            animatorFadeOut?.startDelay = 1000
                            animatorFadeOut?.start()
                        }
                    }
                }
            }
        }

        return isClickHandler
    }

    private fun animateFade(animateFadeOut: () -> Unit) {
        animatorFadeIn = AnimatorSet()
        animatorFadeOut = AnimatorSet()

        fade(animatorFadeIn!!, arrayOf(
            hidePlayer,
            playlistPlayer,
            sharePlayer,
            morePlayer,
            fullPlayer,
            durationPlayer,
            currentPlayer,
            playButton,
            nextButton,
            previousButton
        ), true)

        animatorFadeIn?.play(
            ObjectAnimator.ofFloat(framePlayer, View.ALPHA, 0f, .5f)
        )

        val seekFadeIn = ValueAnimator.ofInt(0, 255)
            .apply {
                addUpdateListener {
                    seekBar.thumb.mutate().alpha = it.animatedFraction.toInt()
                }
                duration = 400
            }

        animatorFadeIn?.play(seekFadeIn)

        fade(animatorFadeIn!!, arrayOf(
            hidePlayer,
            playlistPlayer,
            sharePlayer,
            morePlayer,
            fullPlayer,
            durationPlayer,
            currentPlayer,
            playButton,
            nextButton,
            previousButton
        ), false)

        animatorFadeOut?.play(
            ObjectAnimator.ofFloat(framePlayer, View.ALPHA, .5f, 0f)
        )

        val seekFadeOut = ValueAnimator.ofInt(255, 0)
            .apply {
                addUpdateListener {
                    seekBar.thumb.mutate().alpha = it.animatedFraction.toInt()
                }
                duration = 400
            }

        animatorFadeOut?.play(seekFadeOut)

        animatorFadeIn?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
            }

            override fun onAnimationEnd(p0: Animator) {
                animateFadeOut.invoke()
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })
    }

    private fun fade(animatorSet: AnimatorSet, views: Array<View>, toZero: Boolean) {
        views.forEach {
            animatorSet.play(
                ObjectAnimator.ofFloat(
                    it, View.ALPHA,
                    if (toZero) 0f else 1f,
                    if (toZero) 1f else 0f
                )
            )
        }
    }

}