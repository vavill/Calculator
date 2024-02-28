package com.example.calculator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.isVisible
import com.example.calculator.databinding.ActivityMainBinding
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalInvalidSymbolException
import com.notkamui.keval.KevalZeroDivisionException
import kotlin.math.round

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //enableEdgeToEdge()
        setContentView(binding.root)

        binding.actionButton.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            intent.putExtra(MainActivity2.KEY, binding.tvResult.text.toString())

            startActivity(intent)
        }


        binding.tvResult.visibility = View.GONE

        setClickListenersForNumbers()
        setOnClickListenersOperations()

        // отслеживание изменений в tvCalculation
        binding.tvCalculation.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (isResultFocused()) {
                    addToHistory()
                    unfocusResult()
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

                        } catch (_: KevalInvalidExpressionException) {

                        } catch (e: KevalZeroDivisionException) {
                            tvR.text = "= Разделить на ноль нельзя"
                        } catch (_: KevalInvalidSymbolException) {

                        }
                    }
                }
            }
        })
    }

    private fun isResultFocused(): Boolean {
        // какая то дичь
        val density = this.resources.displayMetrics.density
        val tvResultCurrentTextSize = round(binding.tvResult.textSize / density)
        val textSizeTextViewCalculation =
            this.resources.getDimension(R.dimen.textSize_TextViewCalculation) / density

        // сравнение размера шрифта (текущий размер шрифта tvResult == большой черный шрифт)
        return tvResultCurrentTextSize == textSizeTextViewCalculation
    }

    // обработчик нажатий для цифр 0-9
    private fun setClickListenersForNumbers() = with(binding) {
        constraintBottomSide.forEach { item ->
             if ((item is TextView) && (item.text.toString() in "0".."9"))
                    item.setOnClickListener { numbersOnClick(it as TextView) }
        }
    }

    // добавление текста в tvCalculation
    private fun numbersOnClick(view: TextView) {
        if (isResultFocused())
            binding.tvCalculation.text = ""

        binding.tvCalculation.text = buildString {
            append(binding.tvCalculation.text.toString())
            append(view.text)
        }
    }

    // обработчик нажатий для операций
    private fun setOnClickListenersOperations() {
        val tvC = binding.tvCalculation

        // AC button
        binding.btnClear.setOnClickListener {
            if (binding.btnClear.text == "C") {
                tvC.text = ""
                tvC.setHint("0")
            } else
                clearHistory()
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
            if (isLastSymbolNumberAndNotNull())
                tvC.text = buildString {
                    append(tvC.text)
                    append("÷")
                }
        }

        // Multiply button
        binding.btnMultiply.setOnClickListener {
            if (isLastSymbolNumberAndNotNull())
                tvC.text = buildString {
                    append(tvC.text)
                    append("×")
                }
        }

        // Subtract button
        binding.btnSubtract.setOnClickListener {
            if (isLastSymbolNumberAndNotNull())
                tvC.text = buildString {
                    append(tvC.text)
                    append("-")
                }
        }

        // Addition button
        binding.btnAdd.setOnClickListener {
            if (isLastSymbolNumberAndNotNull())
                tvC.text = buildString {
                    append(tvC.text)
                    append("+")
                }
        }

        binding.btnPoint.setOnClickListener {
            if (isLastSymbolNumberAndNotNull())
                tvC.text = buildString {
                    append(tvC.text)
                    append(".")
                }
            else if (tvC.text.isEmpty())
                tvC.text = "0."
        }

        // Equals button
        binding.btnEquals.setOnClickListener {
            focusResult()
        }
    }

    private fun isLastSymbolNumberAndNotNull() =
        binding.tvCalculation.text.isNotEmpty() && binding.tvCalculation.text.last() in '0'..'9'

    private fun addToHistory() {
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

    private fun clearHistory() {
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

    private fun focusResult() {
        if (binding.tvResult.isVisible) {
            binding.tvResult.setTextAppearance(R.style.TextViewCalculationStyle)
            binding.tvCalculation.setTextAppearance(R.style.TextViewResultStyle)

        }
    }

    private fun unfocusResult() {
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
}