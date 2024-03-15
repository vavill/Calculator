package com.example.calculator.fragments

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.example.calculator.R
import com.example.calculator.databinding.FragmentKeyboardCalculatorBinding
import com.example.calculator.databinding.FragmentResultCalculatorBinding
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalInvalidSymbolException
import com.notkamui.keval.KevalZeroDivisionException
import kotlin.math.round

class ResultCalculatorFragment : Fragment() {
    private var _binding: FragmentResultCalculatorBinding? = null
    val binding get() = requireNotNull(_binding)

    companion object {
        fun newInstance() = ResultCalculatorFragment()

        val RESULT_REQUEST_KEY = "RESULT_REQUEST_KEY"
        val RESULT_RESULT_KEY = "RESULT_RESULT_KEY"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultCalculatorBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFromInternalStorage()
        setTextWatcherToCalculationTextView()

        requireActivity().supportFragmentManager.setFragmentResultListener(
            RESULT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val keyValue = bundle.getString(RESULT_RESULT_KEY, "")
            setOnClickListenersOperations(keyValue)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setTextWatcherToCalculationTextView() {
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

                saveToInternalStorage()
            }
        })
    }

    private fun setOnClickListenersOperations(keyValue: String) {
        val tvC = binding.tvCalculation

        when (keyValue) {
            in "0".."9" -> {
                binding.tvCalculation.text = buildString {
                    append(binding.tvCalculation.text.toString())
                    append(keyValue)
                }
            }
            "CLEAR_C_VALUE" -> {
                tvC.text = ""
                tvC.setHint("0")
            }
            "CLEAR_AC_VALUE" -> {
                clearHistory()
            }
            "BACKSPACE_VALUE" -> {
                if (tvC.length() == 1) {
                    tvC.text = ""
                    tvC.setHint("0")
                } else
                    tvC.text = tvC.text.dropLast(1)
            }
            "PERCENT_VALUE" -> {
                try {
                    tvC.text = Keval.eval("(${tvC.text})/100").toString()
                } catch (_: KevalInvalidExpressionException) {
                }
            }
            "DIVIDE_VALUE" -> {
                if (isLastSymbolNumberAndNotNull())
                    tvC.text = buildString {
                        append(tvC.text)
                        append("÷")
                    }
            }
            "MULTIPLY_VALUE" -> {
                if (isLastSymbolNumberAndNotNull())
                    tvC.text = buildString {
                        append(tvC.text)
                        append("×")
                    }
            }
            "SUBTRACT_VALUE" -> {
                if (isLastSymbolNumberAndNotNull())
                    tvC.text = buildString {
                        append(tvC.text)
                        append("-")
                    }
            }
            "ADD_VALUE" -> {
                if (isLastSymbolNumberAndNotNull())
                    tvC.text = buildString {
                        append(tvC.text)
                        append("+")
                    }
            }
            "POINT_VALUE" -> {
                if (isLastSymbolNumberAndNotNull())
                    tvC.text = buildString {
                        append(tvC.text)
                        append(".")
                    }
                else if (tvC.text.isEmpty())
                    tvC.text = "0."
            }
            "EQUALS_VALUE" -> {
                focusResult()

                val message = buildString {
                    append(binding.tvCalculation.text)
                    append(binding.tvResult.text)
                }
                notifyResult(message)
            }
            else -> {}
        }

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

    private fun isLastSymbolNumberAndNotNull() =
        binding.tvCalculation.text.isNotEmpty()
                && binding.tvCalculation.text.last() in '0'..'9'

    private fun focusResult() = with(binding) {
        if (tvResult.isVisible) {
            tvResult.setTextAppearance(com.example.calculator.R.style.TextViewCalculationStyle)
            tvCalculation.setTextAppearance(com.example.calculator.R.style.TextViewResultStyle)
        }
    }

    private fun notifyResult(contentText: String) {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(requireContext(), "0")
            .setSmallIcon(R.drawable.ic_icon)
            .setContentTitle("Result")
            .setContentText(contentText)
            .build()

        val notificationManager = NotificationManagerCompat.from(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
            )

        notificationManager.notify(1, notification)

    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel("0", "myChannel", NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableLights(true)
                lightColor = Color.RED
            }

        val notificationManager =
            requireContext().getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
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

    private fun saveToInternalStorage() {
        val sharedPreferences = requireActivity().getSharedPreferences("Pref", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()

        edit.apply {
            putString("Result", binding.tvResult.text.toString())
            putString("ResultPrev", binding.tvPrevResult.text.toString())
            putString("ResultPrev2", binding.tvPrevResult2.text.toString())
            putString("Calculation", binding.tvCalculation.text.toString())
            putString("CalculationPrev", binding.tvPrevCalculation.text.toString())
            putString("CalculationPrev2", binding.tvPrevCalculation2.text.toString())
            apply()
        }
    }

    private fun loadFromInternalStorage() {
        val sharedPreferences = requireActivity().getSharedPreferences("Pref", Context.MODE_PRIVATE)

        binding.tvResult.text = sharedPreferences.getString("Result", "")
        binding.tvPrevResult.text = sharedPreferences.getString("ResultPrev", "")
        binding.tvPrevResult2.text = sharedPreferences.getString("ResultPrev2", "")
        binding.tvCalculation.text = sharedPreferences.getString("Calculation", "")
        binding.tvPrevCalculation.text = sharedPreferences.getString("CalculationPrev", "")
        binding.tvPrevCalculation2.text = sharedPreferences.getString("CalculationPrev2", "")
    }
}