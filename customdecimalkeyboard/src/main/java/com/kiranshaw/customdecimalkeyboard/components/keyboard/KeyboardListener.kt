package com.kiranshaw.customdecimalkeyboard.components.keyboard

import com.kiranshaw.customdecimalkeyboard.components.keyboard.controllers.KeyboardController

/**
 * Created by Don.Brody on 7/19/18.
 */
interface KeyboardListener {
    fun characterClicked(c: Char)
    fun specialKeyClicked(key: KeyboardController.SpecialKey)
}