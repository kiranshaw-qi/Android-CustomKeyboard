package com.donbrody.customkeyboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.kiranshaw.customdecimalkeyboard.components.keyboard.CustomisedKeyboardView
import com.kiranshaw.customdecimalkeyboard.components.keyboard.CustomKeyboardView


/**
 * Created by Don.Brody on 7/18/18.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var keyboard: CustomisedKeyboardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val numberField: EditText = findViewById(R.id.testNumberField)
        val numberDecimalField: EditText = findViewById(R.id.testNumberDecimalField)
        val qwertyField: EditText = findViewById(R.id.testQwertyField)

        keyboard = findViewById(R.id.customKeyboardView)
        keyboard.setTextFontSize(30.0F)
        keyboard.setDecimalsSeparator(',')
        keyboard.setThousandsSeparator('.')
        keyboard.setBackgroundColor(Color.WHITE)
        keyboard.registerEditText(CustomKeyboardView.KeyboardType.NUMBER, numberField)
        keyboard.registerEditText(
            CustomKeyboardView.KeyboardType.NUMBER_DECIMAL,
            numberDecimalField
        )
        // can separately add any TextWatcher
        numberDecimalField.addTextChangedListener(
            CustomTextWatcher(
                numberDecimalField,
                ',',
                '.'
            )
        )
        keyboard.registerEditText(CustomKeyboardView.KeyboardType.QWERTY, qwertyField)

        val switchActivitiesButton: Button = findViewById(R.id.switchActivitiesButton)
        switchActivitiesButton.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    AdvancedFeaturesActivity::class.java
                )
            )
        }
    }

    override fun onBackPressed() {
        if (keyboard.isExpanded) {
            keyboard.translateLayout()
        } else {
            super.onBackPressed()
        }
    }
}