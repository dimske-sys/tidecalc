package com.calculator.tidecalc.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.calculator.tidecalc.R
import com.calculator.tidecalc.databinding.ActivityMainBinding
import com.calculator.tidecalc.helpers.*
import com.calculator.tidecalc.widget.FloatingCalculator
import me.grantland.widget.AutofitHelper

class MainActivity : AppCompatActivity(), Calculator {
    private lateinit var calc: CalculatorImpl
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var themeMode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Black status bar
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.BLACK
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE)
        themeMode = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            2
        } else {
            sharedPreferences.getInt("theme", 2)
        }

        calc = CalculatorImpl(this, applicationContext)

        binding.btnPlus.setOnClickListener { calc.handleOperation(PLUS);}
        binding.btnMinus.setOnClickListener { calc.handleOperation(MINUS); }
        binding.btnMultiply.setOnClickListener { calc.handleOperation(MULTIPLY); }
        binding.btnDivide.setOnClickListener { calc.handleOperation(DIVIDE); }
        binding.btnPercent.setOnClickListener { calc.handleOperation(PERCENT); }
        binding.btnPower.setOnClickListener { calc.handleOperation(POWER); }
        binding.btnRoot.setOnClickListener { calc.handleOperation(ROOT); }

        binding.btnMinus.setOnLongClickListener {
            calc.turnToNegative()
        }

        binding.btnClear.setOnClickListener { calc.handleClear(); }
        binding.btnClear.setOnLongClickListener { calc.handleReset(); true }

        getButtons().forEach { it ->
            it.setOnClickListener { calc.numpadClicked(it.id); }
        }

        binding.btnEquals.setOnClickListener { calc.handleEquals(); }

        AutofitHelper.create(binding.result)
        AutofitHelper.create(binding.formula)

        overridePendingTransition(android.R.anim.fade_in, 0)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.theme -> chooseTheme()
            R.id.floatCalc -> {
                if (!Settings.canDrawOverlays(this)) {

                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                    activityResultLauncher.launch(intent)
                } else {
                    initializeFloatView()
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    @SuppressLint("InflateParams")
    private fun launchThemeOptions() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_settings, null)
        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun chooseTheme() {
        val dialogTheme = Dialog(this@MainActivity, R.style.dialogWidth_80)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_settings, findViewById(android.R.id.content), false)
        val themeGroup: RadioGroup = view.findViewById(R.id.themeGroup)
        val ok: Button = view.findViewById(R.id.Ok)
        val cancel: Button = view.findViewById(R.id.Cancel)
        when (themeMode) {
            0 -> view.findViewById<RadioButton>(R.id.Light).isChecked = true
            1 -> view.findViewById<RadioButton>(R.id.Dark).isChecked = true
        }

        ok.setOnClickListener {
            when(themeGroup.checkedRadioButtonId) {
                R.id.Light -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    sharedPreferences.edit().putInt("theme", 0).apply()
                }
                R.id.Dark ->  {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    sharedPreferences.edit().putInt("theme", 1).apply()
                }
            }
            dialogTheme.dismiss()
        }

        cancel.setOnClickListener {
            dialogTheme.dismiss()
        }

        dialogTheme.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogTheme.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogTheme.setContentView(view)
        dialogTheme.show()
    }

    private fun getButtons() = arrayOf(binding.btnDecimal, binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4, binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9)

    override fun showNewResult(value: String, context: Context) {
        binding.result.text = value
    }

    override fun showNewFormula(value: String, context: Context) {
        binding.formula.text = value
    }

    private fun initializeFloatView() {
        val intent = Intent(this@MainActivity, FloatingCalculator::class.java)
        startService(intent)
        finish()
    }
    private var activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(
                applicationContext)
        ) {
            initializeFloatView()
        } else {
            Toast.makeText(applicationContext,
                "Draw over other app permission not available.",
                Toast.LENGTH_SHORT).show()
        }
    }

}






