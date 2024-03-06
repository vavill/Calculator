package com.example.calculator.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.calculator.R
import com.example.calculator.databinding.FragmentKeyboardCalculatorBinding
import com.example.calculator.databinding.FragmentResultCalculatorBinding
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalInvalidSymbolException
import com.notkamui.keval.KevalZeroDivisionException
import kotlin.math.round

class ResultCalculatorFragment : Fragment() {
    lateinit var binding: FragmentResultCalculatorBinding

    companion object {
        @JvmStatic
        lateinit var instance: ResultCalculatorFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentResultCalculatorBinding.inflate(layoutInflater)
        instance = this

        binding.tvResult.visibility = View.GONE
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
                val keyboardCalculatorFragment = requireActivity()
                    .supportFragmentManager.findFragmentById(R.id.fragmentContainerKeyboard) as? KeyboardCalculatorFragment
                val btnClear =
                    keyboardCalculatorFragment?.view?.findViewById<TextView>(R.id.btnClear)

                when (s.toString()) {
                    "0" -> {
                        tvR.text = ""
                        tvR.visibility = View.GONE
                    }

                    "" -> {
                        tvR.text = "0"
                        tvR.visibility = View.GONE
                        btnClear?.text = "AC"
                    }

                    else -> {
                        btnClear?.text = "C"

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

    private fun unfocusResult() {
        binding.tvResult.setTextAppearance(R.style.TextViewResultStyle)
        binding.tvCalculation.setTextAppearance(R.style.TextViewCalculationStyle)
    }

    fun isResultFocused(): Boolean {
        // какая то дичь
        val density = requireActivity().resources.displayMetrics.density
        val tvResultCurrentTextSize = round(binding.tvResult.textSize / density)
        val textSizeTextViewCalculation =
            requireActivity().resources.getDimension(R.dimen.textSize_TextViewCalculation) / density

        // сравнение размера шрифта (текущий размер шрифта tvResult == большой черный шрифт)
        return tvResultCurrentTextSize == textSizeTextViewCalculation
    }

    private fun hasDecimalPart(number: Double): String {
        val integerPart = number.toInt()
        return if (number == integerPart.toDouble())
            integerPart.toString()
        else
            number.toString()
    }
}