package com.donbrody.customkeyboard

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * tracksynq-android
 *
 * Created by Bashir on 09-02-2022
 * Copyright © 2021 Quantum Inventions. All rights reserved.
 */

/**
 * This TextWatcher adds [thousandSeparator] automatically
 * From https://stackify.dev/354994-add-comma-as-thousands-separator-for-numbers-in-edittext-for-android-studio
 *
 * @property field
 * @property decimalSeparator
 * @property thousandSeparator
 * @constructor Create empty Custom text watcher
 */
class CustomTextWatcher(
    private val field: EditText,
    private val decimalSeparator: Char,
    private val thousandSeparator: Char
) : TextWatcher {
    private var prevString = ""
    private var curString = ""

    override fun afterTextChanged(p0: Editable?) {
        field.removeTextChangedListener(this)

        try {
            val givenString: String = p0.toString()
            curString = givenString
            val initialCurPos: Int = field.selectionEnd ?: 0

            var isEditing = false
            if (initialCurPos != givenString.length) {
                isEditing = true
            }
            var firstStr = givenString
            var secondStr = ""
            val indexOfDecimalPoint = givenString.indexOf(decimalSeparator)
            if (indexOfDecimalPoint != -1) {
                firstStr = givenString.substring(0, indexOfDecimalPoint)
                secondStr = givenString.substring(indexOfDecimalPoint, givenString.length)
            }
            if (firstStr.contains(thousandSeparator)) {
                firstStr = firstStr.replace(thousandSeparator.toString(), "")
            }
            val longVal: Long = firstStr.toLong()

            // https://docs.oracle.com/javase/tutorial/i18n/format/decimalFormat.html
            val unusualSymbols = DecimalFormatSymbols()
            unusualSymbols.decimalSeparator = decimalSeparator
            unusualSymbols.groupingSeparator = thousandSeparator

            val formatter = DecimalFormat("#,###,###", unusualSymbols)
            formatter.groupingSize = 3
            val formattedString = formatter.format(longVal)

            val resultantStr = formattedString + secondStr
            field.setText(resultantStr)
            //region to calculate the final cursor position
            var finalCurPos = field.text.length
            if (isEditing) {
                finalCurPos = if (
                    curString.length > prevString.length &&
                    firstStr.length != 1 && firstStr.length % 3 == 1 &&
                    initialCurPos != indexOfDecimalPoint
                ) {
                    initialCurPos + 1
                } else {
                    initialCurPos
                }
            }
            //endregion
            field.setSelection(finalCurPos)
            // to place the cursor at the suitable position
            prevString = curString
        } catch (nfe: NumberFormatException) {
            nfe.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        field.addTextChangedListener(this)

    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // no need any callback for this.
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // no need any callback for this.
    }
}
