package com.kiranshaw.customdecimalkeyboard.components.keyboard

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.kiranshaw.customdecimalkeyboard.components.expandableView.ExpandableState
import com.kiranshaw.customdecimalkeyboard.components.expandableView.ExpandableStateListener
import com.kiranshaw.customdecimalkeyboard.components.expandableView.ExpandableView
import com.kiranshaw.customdecimalkeyboard.components.keyboard.controllers.DefaultKeyboardController
import com.kiranshaw.customdecimalkeyboard.components.keyboard.controllers.KeyboardController
import com.kiranshaw.customdecimalkeyboard.components.keyboard.controllers.NumberDecimalKeyboardController
import com.kiranshaw.customdecimalkeyboard.components.keyboard.layouts.KeyboardLayout
import com.kiranshaw.customdecimalkeyboard.components.keyboard.layouts.NumberDecimalKeyboardLayout
import com.kiranshaw.customdecimalkeyboard.components.keyboard.layouts.NumberKeyboardLayout
import com.kiranshaw.customdecimalkeyboard.components.keyboard.layouts.QwertyKeyboardLayout
import com.kiranshaw.customdecimalkeyboard.components.textFields.CustomTextField
import com.kiranshaw.customdecimalkeyboard.components.utilities.ComponentUtils
import java.lang.Exception
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * Created by Don.Brody on 7/18/18.
 */
open class CustomKeyboardView(context: Context, attr: AttributeSet) : ExpandableView(context, attr) {
    private var fieldInFocus: EditText? = null
    private val keyboards = HashMap<EditText, KeyboardLayout?>()
    private val keyboardListener: KeyboardListener
    var decimalSeparator: Char = '.'
    var thousandSeparator: Char = ','
    var textSize: Float = 22.0F
    var gapSize: Int = 8
    var bgColor = Color.WHITE

    init {

        keyboardListener = object: KeyboardListener {
            override fun characterClicked(c: Char) {
                // don't need to do anything here
            }

            override fun specialKeyClicked(key: KeyboardController.SpecialKey) {
                if (key === KeyboardController.SpecialKey.DONE) {
                    translateLayout()
                } else if (key === KeyboardController.SpecialKey.NEXT) {
                    fieldInFocus?.focusSearch(View.FOCUS_DOWN)?.let {
                        it.requestFocus()
                        checkLocationOnScreen()
                        return
                    }
                }
            }
        }

        // register listener with parent (listen for state changes)
        registerListener(object: ExpandableStateListener {
            override fun onStateChange(state: ExpandableState) {
                if (state === ExpandableState.EXPANDED) {
                    checkLocationOnScreen()
                }
            }
        })

        // empty onClickListener prevents user from
        // accidentally clicking views under the keyboard
        setOnClickListener({})
        isSoundEffectsEnabled = false
    }

    fun registerEditText(type: KeyboardType, field: EditText) {
        if (!field.isEnabled) {
            return  // disabled fields do not have input connections
        }

        field.setRawInputType(InputType.TYPE_CLASS_TEXT)
        field.setTextIsSelectable(true)
        field.showSoftInputOnFocus = false
        field.isSoundEffectsEnabled = false
        field.isLongClickable = false

        val inputConnection = field.onCreateInputConnection(EditorInfo())
        keyboards[field] = createKeyboardLayout(type, inputConnection)
        keyboards[field]?.registerListener(keyboardListener)

        field.onFocusChangeListener = OnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (hasFocus) {
                ComponentUtils.hideSystemKeyboard(context, field)

                // if we can find a view below this field, we want to replace the
                // done button with the next button in the attached keyboard
                field.focusSearch(View.FOCUS_DOWN)?.run {
                    if (this is EditText) keyboards[field]?.hasNextFocus = true
                }
                fieldInFocus = field

                renderKeyboard()
                if (!isExpanded) {
                    translateLayout()
                }
            } else if (!hasFocus && isExpanded) {
                for (editText in keyboards.keys) {
                    if (editText.hasFocus()) {
                        return@OnFocusChangeListener
                    }
                }
                translateLayout()
            }
        }

        field.setOnClickListener({
            if (!isExpanded) {
                translateLayout()
            }
        })

        field.addTextChangedListener(object : TextWatcher {

            var prevString = ""
            var curString = ""

            // https://stackify.dev/354994-add-comma-as-thousands-separator-for-numbers-in-edittext-for-android-studio
            override fun afterTextChanged(p0: Editable?) {
                field.removeTextChangedListener(this)

                try {
                    val givenString: String = p0.toString()
                    curString = givenString
                    val initialCurPos: Int = field.selectionEnd

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

        })
    }

    fun autoRegisterEditTexts(rootView: ViewGroup) {
        registerEditTextsRecursive(rootView)
    }

    private fun registerEditTextsRecursive(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                registerEditTextsRecursive(view.getChildAt(i))
            }
        } else {
            if (view is CustomTextField) {
                registerEditText(view.keyboardType, view)
            } else if (view is EditText) {
                when (view.inputType) {
                    InputType.TYPE_CLASS_NUMBER -> {
                        registerEditText(CustomKeyboardView.KeyboardType.NUMBER, view)
                    }
                    InputType.TYPE_NUMBER_FLAG_DECIMAL -> {
                        registerEditText(CustomKeyboardView.KeyboardType.NUMBER_DECIMAL, view)
                    }
                    else -> {
                        registerEditText(CustomKeyboardView.KeyboardType.QWERTY, view)
                    }
                }
            }
        }
    }

    fun unregisterEditText(field: EditText?) {
        keyboards.remove(field)
    }

    fun clearEditTextCache() {
        keyboards.clear()
    }

    private fun renderKeyboard() {
        removeAllViews()
        val keyboard: KeyboardLayout? = keyboards[fieldInFocus]
        keyboard?.let {
            it.orientation = LinearLayout.VERTICAL
            it.createKeyboard(measuredWidth.toFloat())
            addView(keyboard)
        }
    }

    private fun createKeyboardLayout(type: KeyboardType, ic: InputConnection): KeyboardLayout? {
        when(type) {
            KeyboardType.NUMBER -> {
                return NumberKeyboardLayout(context, createKeyboardController(type, ic))
            }
            KeyboardType.NUMBER_DECIMAL -> {
                return NumberDecimalKeyboardLayout(
                    context,
                    createKeyboardController(type, ic),
                    decimalSeparator,
                    textSize,
                    gapSize,
                    bgColor
                )
            }
            KeyboardType.QWERTY -> {
                return QwertyKeyboardLayout(context, createKeyboardController(type, ic))
            }
            else -> return@createKeyboardLayout null // this should never happen
        }
    }

    private fun createKeyboardController(type: KeyboardType, ic: InputConnection): KeyboardController? {
        return when(type) {
            KeyboardType.NUMBER_DECIMAL -> {
                NumberDecimalKeyboardController(ic)
            }
            else -> {
                // not all keyboards require a custom controller
                DefaultKeyboardController(ic)
            }
        }
    }

    override fun configureSelf() {
        renderKeyboard()
        checkLocationOnScreen()
    }

    /**
     * Check if fieldInFocus has a parent that is a ScrollView.
     * Ensure that ScrollView is enabled.
     * Check if the fieldInFocus is below the KeyboardLayout (measured on the screen).
     * If it is, find the deltaY between the top of the KeyboardLayout and the top of the
     * fieldInFocus, add 20dp (for padding), and scroll to the deltaY.
     * This will ensure the keyboard doesn't cover the field (if conditions above are met).
     */
    private fun checkLocationOnScreen() {
        fieldInFocus?.run {
            var fieldParent = this.parent
            while (fieldParent !== null) {
                if (fieldParent is ScrollView) {
                    if (!fieldParent.isSmoothScrollingEnabled) {
                        break
                    }

                    val fieldLocation = IntArray(2)
                    this.getLocationOnScreen(fieldLocation)

                    val keyboardLocation = IntArray(2)
                    this@CustomKeyboardView.getLocationOnScreen(keyboardLocation)

                    val fieldY = fieldLocation[1]
                    val keyboardY = keyboardLocation[1]

                    if (fieldY > keyboardY) {
                        val deltaY = (fieldY - keyboardY)
                        val scrollTo = (fieldParent.scrollY + deltaY + this.measuredHeight + 10.toDp)
                        fieldParent.smoothScrollTo(0, scrollTo)
                    }
                    break
                }
                fieldParent = fieldParent.parent
            }
        }
    }

    enum class KeyboardType {
        NUMBER,
        NUMBER_DECIMAL,
        QWERTY
    }
}
