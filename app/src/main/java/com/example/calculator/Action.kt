package com.example.calculator

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.calculator.databinding.ActivityMainBinding
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalInvalidSymbolException
import com.notkamui.keval.KevalZeroDivisionException
import kotlin.math.round

fun action(binding: ActivityMainBinding, context: Context) {
    binding.tvResult.visibility = View.GONE

    setClickListenersForNumbers(binding, context)
    setOnClickListenersOperations(binding)

    // отслеживание изменений в tvCalculation
    binding.tvCalculation.addTextChangedListener(object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (isResultFocused(binding, context)) {
                addToHistory(binding)
                unfocusResult(binding)
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            val tvR = binding.tvResult
            when (s.toString()) {
                "0" -> {
                    tvR.text = ""
                    tvR.visibility = View.GONE
                }

                "" -> {
                    tvR.text = "0"
                    tvR.visibility = View.GONE
                    binding.btnClear.text = "AC"
                }

                else -> {
                    binding.btnClear.text = "C"

                    // пытаемся вычислить выражение
                    try {
                        val text = s.toString()
                            .replace("÷", "/")
                            .replace("×", "*")

                        if (text[text.lastIndex] in '0'..'9') {
                            val result = Keval.eval(text)
                            tvR.text = "= ${hasDecimalPart(result)}"
                            tvR.visibility = View.VISIBLE
                        }

                    }
                    catch (_: KevalInvalidExpressionException) {

                    }
                    catch (e: KevalZeroDivisionException) {
                        tvR.text = "= Разделить на ноль нельзя"
                    }
                    catch (_: KevalInvalidSymbolException) {

                    }


                }
            }
        }
    })
}

private fun isResultFocused(binding: ActivityMainBinding, context: Context): Boolean {
    // какая то дичь
    val density = context.resources.displayMetrics.density
    val tvResultCurrentTextSize = round(binding.tvResult.textSize / density)
    val textSizeTextViewCalculation =
        context.resources.getDimension(R.dimen.textSize_TextViewCalculation) / density

    // сравнение размера шрифта (текущий размер шрифта tvResult == большой черный шрифт)
    return tvResultCurrentTextSize == textSizeTextViewCalculation
}

// обработчик нажатий для цифр 0-9
private fun setClickListenersForNumbers(binding: ActivityMainBinding, context: Context) {
    val gridLayout: GridLayout = binding.gridLayout
    for (i in 0 until gridLayout.childCount) {
        val view: View = gridLayout.getChildAt(i)
        if ((view is TextView) && (view.text.toString() in "0".."9")) {
            view.setOnClickListener { numbersOnClick(it as TextView, binding, context) }
        }
    }
}

// добавление текста в tvCalculation
private fun numbersOnClick(view: TextView, binding: ActivityMainBinding, context: Context) {
    if (isResultFocused(binding, context))
        binding.tvCalculation.text = ""

    binding.tvCalculation.text = buildString {
        append(binding.tvCalculation.text.toString())
        append(view.text)
    }
}

// обработчик нажатий для операций
private fun setOnClickListenersOperations(binding: ActivityMainBinding) {
    var tvC = binding.tvCalculation

    // AC button
    binding.btnClear.setOnClickListener {
        if (binding.btnClear.text == "C") {
            tvC.text = ""
            tvC.setHint("0")
        } else
            clearHistory(binding)
    }

    // Backspace button
    binding.btnBackspace.setOnClickListener {
        if (tvC.length() == 1) {
            tvC.text = ""
            tvC.setHint("0")
        } else
            tvC.text = tvC.text.dropLast(1)
    }

    // % button
    binding.btnPercent.setOnClickListener {
        try {
            tvC.text =
                Keval.eval("(${tvC.text})/100").toString()
        } catch (_: KevalInvalidExpressionException) {

        }
    }

    // Divide button
    binding.btnDivide.setOnClickListener {
        if (isLastSymbolNumberAndNotNull(binding))
            tvC.text = buildString {
            append(tvC.text)
            append("÷")
        }
    }

    // Multiply button
    binding.btnMultiply.setOnClickListener {
        if (isLastSymbolNumberAndNotNull(binding))
            tvC.text = buildString {
            append(tvC.text)
            append("×")
        }
    }

    // Subtract button
    binding.btnSubtract.setOnClickListener {
        if (isLastSymbolNumberAndNotNull(binding))
            tvC.text = buildString {
            append(tvC.text)
            append("-")
        }
    }

    // Addition button
    binding.btnAdd.setOnClickListener {
        if (isLastSymbolNumberAndNotNull(binding))
            tvC.text = buildString {
                append(tvC.text)
                append("+")
            }
    }

    binding.btnPoint.setOnClickListener {
        if (isLastSymbolNumberAndNotNull(binding))
            tvC.text = buildString {
                append(tvC.text)
                append(".")
            }
        else if (tvC.text.isEmpty())
            tvC.text = "0."
    }

    // Equals button
    binding.btnEquals.setOnClickListener {
        focusResult(binding)
    }
}

private fun isLastSymbolNumberAndNotNull(binding: ActivityMainBinding)
        = binding.tvCalculation.text.isNotEmpty() && binding.tvCalculation.text.last() in '0'..'9'

private fun addToHistory(binding: ActivityMainBinding) {
    if (binding.tvPrevResult.text.isNotEmpty()) {
        binding.tvPrevResult2.visibility = View.VISIBLE
        binding.tvPrevCalculation2.visibility = View.VISIBLE
    }

    binding.tvPrevResult.visibility = View.VISIBLE
    binding.tvPrevCalculation.visibility = View.VISIBLE

    binding.tvPrevCalculation2.text = binding.tvPrevCalculation.text
    binding.tvPrevResult2.text = binding.tvPrevResult.text

    binding.tvPrevCalculation.text = binding.tvCalculation.text
    binding.tvPrevResult.text = binding.tvResult.text

}

private fun clearHistory(binding: ActivityMainBinding) {
    binding.tvPrevResult
        .also { it.text = "" }
        .visibility = View.INVISIBLE
    binding.tvPrevResult2
        .also { it.text = "" }
        .visibility = View.INVISIBLE
    binding.tvPrevCalculation
        .also { it.text = "" }
        .visibility = View.INVISIBLE
    binding.tvPrevCalculation2
        .also { it.text = "" }
        .visibility = View.INVISIBLE

}

private fun focusResult(binding: ActivityMainBinding) {
    if (binding.tvResult.isVisible){
        binding.tvResult.setTextAppearance(R.style.TextViewCalculationStyle)
        binding.tvCalculation.setTextAppearance(R.style.TextViewResultStyle)
    }
}

private fun unfocusResult(binding: ActivityMainBinding) {
    binding.tvResult.setTextAppearance(R.style.TextViewResultStyle)
    binding.tvCalculation.setTextAppearance(R.style.TextViewCalculationStyle)
}

// убираем дробную часть в tvResult, если *.0
private fun hasDecimalPart(number: Double): String {
    val integerPart = number.toInt()
    return if (number == integerPart.toDouble())
        integerPart.toString()
    else
        number.toString()
}