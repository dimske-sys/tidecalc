package com.calculator.tidecalc.widget

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.calculator.tidecalc.R
import com.calculator.tidecalc.activities.MainActivity
import com.calculator.tidecalc.helpers.*
import me.grantland.widget.AutofitHelper

class FloatingCalculator : Service(), View.OnClickListener , Calculator {
    private lateinit var calc: CalculatorImpl

    //Floating variables
    private lateinit var mWindowManager: WindowManager
    private lateinit var mFloatingView: View
    private lateinit var mCollapsedView: View
    private lateinit var mExpandedView: View
    lateinit var paramsForWidget: WindowManager.LayoutParams
    private lateinit var paramsForSoftInput: WindowManager.LayoutParams
    private lateinit var closeBtn: RelativeLayout
    private lateinit var collapseBtn: RelativeLayout
    private lateinit var fullViewBtn: RelativeLayout

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater


        setParams()
        addFloatingWidgetView(inflater)
        implementClickListeners()
        implementTouchListenerToFloatingWidgetView()

    }

    private fun setParams() {
        paramsForSoftInput = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                0,
                PixelFormat.TRANSLUCENT)
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                0,
                PixelFormat.TRANSLUCENT)
        }
        paramsForWidget = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
        }
    }

    @SuppressLint("InflateParams")
    private fun addFloatingWidgetView(inflater: LayoutInflater) {
        mFloatingView = inflater.inflate(R.layout.floating_calculator, null)
        initUserInterface()
        paramsForWidget.gravity = Gravity.TOP or Gravity.START
        paramsForWidget.x = 0
        paramsForWidget.y = 100
        mWindowManager.addView(mFloatingView, paramsForWidget)
        mCollapsedView = mFloatingView.findViewById(R.id.collapse_view)
        mExpandedView = mFloatingView.findViewById(R.id.expanded_container)
    }

    private fun implementTouchListenerToFloatingWidgetView() {
        mFloatingView.findViewById<View>(R.id.root_container)
            .setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f
                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {

                            //remember the initial position.
                            initialX = paramsForWidget.x
                            initialY = paramsForWidget.y

                            //get the touch location
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            val xDiff = (event.rawX - initialTouchX).toInt()
                            val yDiff = (event.rawY - initialTouchY).toInt()

                            //The check for xDiff <10 && yDiff< 10 because sometime elements moves a little while clicking.
                            //So that is click event.
                            if (xDiff < 10 && yDiff < 10) {
                                expandOrCollapse()
                            }
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            //Calculate the X and Y coordinates of the view.
                            paramsForWidget.x = initialX + (event.rawX - initialTouchX).toInt()
                            paramsForWidget.y = initialY + (event.rawY - initialTouchY).toInt()
                            if (mExpandedView.visibility == View.GONE) {
                                //Update the layout with new X & Y coordinate
                                mWindowManager.updateViewLayout(mFloatingView, paramsForWidget)
                            }
                            return true
                        }
                    }
                    return false
                }
            })
    }

    private fun implementClickListeners() {
        closeBtn.setOnClickListener(this)
        collapseBtn.setOnClickListener(this)
        fullViewBtn.setOnClickListener(this)
    }


    private fun initUserInterface() {
        closeBtn = mFloatingView.findViewById(R.id.close_btn)
        collapseBtn = mFloatingView.findViewById(R.id.fullView_btn)
        fullViewBtn = mFloatingView.findViewById(R.id.collapse_btn)

        calc = CalculatorImpl(this, applicationContext)

        mFloatingView.findViewById<Button>(R.id.btn_plus).setOnClickListener { calc.handleOperation(PLUS)}
        mFloatingView.findViewById<Button>(R.id.btn_minus).setOnClickListener { calc.handleOperation(MINUS); }
        mFloatingView.findViewById<Button>(R.id.btn_multiply).setOnClickListener { calc.handleOperation(MULTIPLY); }
        mFloatingView.findViewById<Button>(R.id.btn_divide).setOnClickListener { calc.handleOperation(DIVIDE); }
        mFloatingView.findViewById<Button>(R.id.btn_percent).setOnClickListener { calc.handleOperation(PERCENT); }
        mFloatingView.findViewById<Button>(R.id.btn_power).setOnClickListener { calc.handleOperation(POWER); }
        mFloatingView.findViewById<Button>(R.id.btn_root).setOnClickListener { calc.handleOperation(ROOT); }

        mFloatingView.findViewById<Button>(R.id.btn_minus).setOnLongClickListener {
            calc.turnToNegative()
        }

        mFloatingView.findViewById<Button>(R.id.btn_clear).setOnClickListener { calc.handleClear(); }
        mFloatingView.findViewById<Button>(R.id.btn_clear).setOnLongClickListener { calc.handleReset(); true }

        getButtons().forEach { it ->
            it.setOnClickListener { calc.numpadClicked(it.id); }
        }

        mFloatingView.findViewById<Button>(R.id.btn_equals).setOnClickListener { calc.handleEquals(); }

        AutofitHelper.create(mFloatingView.findViewById(R.id.result))
        AutofitHelper.create(mFloatingView.findViewById(R.id.formula))

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.close_btn -> {
                stopSelf()
            }
            R.id.collapse_btn -> expandOrCollapse()
            R.id.fullView_btn -> {
                val intent = Intent(this@FloatingCalculator, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                stopSelf()
            }
        }
    }

    /*  Detect if the floating view is collapsed or expanded */
    private val isViewCollapsed: Boolean
        get() = mFloatingView.findViewById<View>(R.id.collapse_view).visibility == View.VISIBLE

    /*  on Floating widget click show expanded view  */
    private fun expandOrCollapse() {
        val bouncy: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.bounce)
        var interpolator = MyBounceInterpolator(0.1, 15)
        bouncy.interpolator = interpolator

        if (isViewCollapsed) {
            mWindowManager.removeView(mFloatingView)
            mWindowManager.addView(mFloatingView, paramsForSoftInput)
            mCollapsedView.visibility = View.GONE
            mExpandedView.visibility = View.VISIBLE
            closeBtn.visibility = View.VISIBLE
            collapseBtn.visibility = View.VISIBLE
            fullViewBtn.visibility = View.VISIBLE

            mExpandedView.animation = bouncy
            closeBtn.animation = bouncy
            collapseBtn.animation = bouncy
            fullViewBtn.animation = bouncy
        } else {
            mWindowManager.removeView(mFloatingView)
            mWindowManager.addView(mFloatingView, paramsForWidget)
            mCollapsedView.visibility = View.VISIBLE
            mExpandedView.visibility = View.GONE
            closeBtn.visibility = View.GONE
            collapseBtn.visibility = View.GONE
            fullViewBtn.visibility = View.GONE

            interpolator = MyBounceInterpolator(0.07, 15)
            bouncy.interpolator = interpolator
            mCollapsedView.animation = bouncy

        }
    }


    override fun onDestroy() {
        mWindowManager.removeView(mFloatingView)
        super.onDestroy()
    }


    private fun getButtons() = arrayOf(
        mFloatingView.findViewById<Button>(R.id.btn_decimal),
        mFloatingView.findViewById<Button>(R.id.btn_0),
        mFloatingView.findViewById<Button>(R.id.btn_1),
        mFloatingView.findViewById<Button>(R.id.btn_2),
        mFloatingView.findViewById<Button>(R.id.btn_3),
        mFloatingView.findViewById<Button>(R.id.btn_4),
        mFloatingView.findViewById<Button>(R.id.btn_5),
        mFloatingView.findViewById<Button>(R.id.btn_6),
        mFloatingView.findViewById<Button>(R.id.btn_7),
        mFloatingView.findViewById<Button>(R.id.btn_8),
        mFloatingView.findViewById<Button>(R.id.btn_9))

    override fun showNewResult(value: String, context: Context) {
        mFloatingView.findViewById<TextView>(R.id.result).text = value
    }

    override fun showNewFormula(value: String, context: Context) {
        mFloatingView.findViewById<TextView>(R.id.formula).text = value
    }
}






