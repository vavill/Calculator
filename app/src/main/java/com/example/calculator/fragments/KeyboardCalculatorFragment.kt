package com.example.calculator.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.calculator.CalculatorViewModel
import com.example.calculator.MainActivity2
import com.example.calculator.R
import com.example.calculator.adapter.MainActivity3
import com.example.calculator.databinding.FragmentKeyboardCalculatorBinding
import com.example.calculator.databinding.FragmentResultCalculatorBinding
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalInvalidSymbolException
import com.notkamui.keval.KevalZeroDivisionException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.round

class KeyboardCalculatorFragment : Fragment() {

    lateinit var binding: FragmentKeyboardCalculatorBinding
    lateinit var bindingResult: FragmentResultCalculatorBinding

    companion object {
        @JvmStatic
        lateinit var instance: KeyboardCalculatorFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindingResult = (requireActivity().supportFragmentManager
            .findFragmentById(R.id.fragmentContainerResult) as? ResultCalculatorFragment)!!
            .binding

        binding.actionButton.setOnClickListener {
            CoroutineScope(CoroutineName("myScope")).launch {
                requestPermissions()
            }

            val intent = Intent(requireActivity(), MainActivity3::class.java)
            intent.putExtra(MainActivity2.KEY, bindingResult.tvResult.text.toString())
            startActivity(intent)
        }

        setClickListenersForNumbers()
        setOnClickListenersOperations()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentKeyboardCalculatorBinding.inflate(layoutInflater)
        instance = this
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
        if (ResultCalculatorFragment.instance.isResultFocused())
            bindingResult.tvCalculation.text = ""

        bindingResult.tvCalculation.text = buildString {
            append(bindingResult.tvCalculation.text.toString())
            append(view.text)
        }
    }

    // обработчик нажатий для операций
    private fun setOnClickListenersOperations() {
        val tvC = bindingResult.tvCalculation

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
        bindingResult.tvCalculation.text.isNotEmpty() && bindingResult.tvCalculation.text.last() in '0'..'9'

    private fun clearHistory() {
        bindingResult.tvPrevResult
            .also { it.text = "" }
            .visibility = View.INVISIBLE
        bindingResult.tvPrevResult2
            .also { it.text = "" }
            .visibility = View.INVISIBLE
        bindingResult.tvPrevCalculation
            .also { it.text = "" }
            .visibility = View.INVISIBLE
        bindingResult.tvPrevCalculation2
            .also { it.text = "" }
            .visibility = View.INVISIBLE

    }

    private fun focusResult() = with(bindingResult) {
        if (tvResult.isVisible) {
            tvResult.setTextAppearance(R.style.TextViewCalculationStyle)
            tvCalculation.setTextAppearance(R.style.TextViewResultStyle)
        }
    }

    suspend private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (!hasWriteContactsPermission())
            permissions.add(Manifest.permission.WRITE_CONTACTS)

        if (!hasLocationForegroundPermission())
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (!hasLocationBackgroundPermission() && hasLocationForegroundPermission())
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        if (permissions.isNotEmpty())
            ActivityCompat.requestPermissions(requireActivity(), permissions.toTypedArray(), 0)
    }

    // тест для permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0 && grantResults.isNotEmpty())
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    Log.d("myTag", "${permissions[i]} granted")
            }
    }

    private fun hasLocationForegroundPermission() =
        ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasLocationBackgroundPermission() =
        ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasWriteContactsPermission() =
        ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED


}