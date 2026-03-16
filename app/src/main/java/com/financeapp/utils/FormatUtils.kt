package com.financeapp.utils

import java.text.NumberFormat
import java.util.Locale

fun formatAmt(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)
