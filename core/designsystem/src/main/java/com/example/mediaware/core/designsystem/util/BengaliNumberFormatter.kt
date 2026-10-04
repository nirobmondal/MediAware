package com.example.mediaware.core.designsystem.util

object BengaliNumberFormatter {
    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun format(number: Int): String = format(number.toString())

    fun format(number: Long): String = format(number.toString())

    fun format(text: String): String {
        val sb = StringBuilder()
        for (char in text) {
            if (char in '0'..'9') {
                sb.append(bengaliDigits[char - '0'])
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigits(text: String): String {
        val sb = StringBuilder()
        for (char in text) {
            val index = bengaliDigits.indexOf(char)
            if (index != -1) {
                sb.append(('0' + index))
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }
}

fun Int.toBengaliDigits(): String = BengaliNumberFormatter.format(this)
fun Long.toBengaliDigits(): String = BengaliNumberFormatter.format(this)
fun String.toBengaliDigits(): String = BengaliNumberFormatter.format(this)
fun String.toEnglishDigits(): String = BengaliNumberFormatter.toEnglishDigits(this)
