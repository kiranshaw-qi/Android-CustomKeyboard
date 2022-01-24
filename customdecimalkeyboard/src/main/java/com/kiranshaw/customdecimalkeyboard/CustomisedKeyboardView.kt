package com.kiranshaw.customdecimalkeyboard

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.InputConnection
import com.donbrody.customkeyboard.components.keyboard.CustomKeyboardView
import com.donbrody.customkeyboard.components.keyboard.layouts.KeyboardLayout
import com.donbrody.customkeyboard.components.keyboard.layouts.NumberDecimalKeyboardLayout
import com.donbrody.customkeyboard.components.keyboard.layouts.NumberKeyboardLayout
import com.donbrody.customkeyboard.components.keyboard.layouts.QwertyKeyboardLayout

/**
 * tracksynq-android
 *
 * Created by Kiran Shaw on 21-01-2022
 * Copyright © 2022 Quantum Inventions. All rights reserved.
 */

class CustomisedKeyboardView(context: Context, attr: AttributeSet) :
    CustomKeyboardView(context, attr) {

    fun setDecimalsSeparator(decSeparator: Char) {
        decimalSeparator = decSeparator
    }

    fun setThousandsSeparator(thousandsSeparator: Char) {
        thousandSeparator = thousandsSeparator
    }

    fun setTextFontSize(txtSize: Float) {
        textSize = txtSize
    }

    fun setBtnGapSize(gapSize: Int) { // in Dp
        this.gapSize = gapSize
    }
}