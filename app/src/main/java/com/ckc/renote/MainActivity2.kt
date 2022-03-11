package com.ckc.renote

import androidx.appcompat.app.AppCompatActivity
import android.view.ScaleGestureDetector
import android.view.GestureDetector
import android.os.Bundle
import com.ckc.renote.R
import com.ckc.renote.MainActivity2.GestureListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.animation.ScaleAnimation
import android.widget.ScrollView
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View

class MainActivity2 : AppCompatActivity() {
    private var mScale = 1f
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    var gestureDetector: GestureDetector? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        gestureDetector = GestureDetector(this, GestureListener())
        mScaleGestureDetector = ScaleGestureDetector(this, object : SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = 1 - detector.scaleFactor
                val prevScale = mScale
                mScale += scale
                if (mScale > 10f) mScale = 10f
                val scaleAnimation = ScaleAnimation(
                    1f / prevScale,
                    1f / mScale,
                    1f / prevScale,
                    1f / mScale,
                    detector.focusX,
                    detector.focusY
                )
                scaleAnimation.duration = 0
                scaleAnimation.fillAfter = true
                val layout = findViewById<View>(R.id.scrollView) as ScrollView
                layout.startAnimation(scaleAnimation)
                return true
            }
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)
        mScaleGestureDetector!!.onTouchEvent(event)
        gestureDetector!!.onTouchEvent(event)
        return gestureDetector!!.onTouchEvent(event)
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return true
        }
    }
}