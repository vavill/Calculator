package com.example.calculator.fragments

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.core.view.isVisible
import com.example.calculator.ButtonNames
import com.example.calculator.adapter.MainActivity2
import com.example.calculator.R
import com.example.calculator.adapter.MainActivity3
import com.example.calculator.databinding.FragmentKeyboardCalculatorBinding
import com.example.calculator.databinding.FragmentResultCalculatorBinding
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class KeyboardCalculatorFragment : Fragment() {

    private var _binding: FragmentKeyboardCalculatorBinding? = null
    private val binding get() = requireNotNull(_binding)

    companion object {
        fun newInstance() = KeyboardCalculatorFragment()

        const val KEYBOARD_REQUEST_KEY = "KEYBOARD_REQUEST_KEY"
        const val KEYBOARD_RESULT_KEY = "KEYBOARD_RESULT_KEY"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.actionButton.setOnClickListener {
            CoroutineScope(CoroutineName("myScope")).launch {
                requestPermissions()
            }

            val intent = Intent(requireActivity(), MainActivity3::class.java)
            intent.putExtra(MainActivity2.KEY, "")
            startActivity(intent)
        }

        setClickListenersForNumbers()
        setOnClickListenersOperations()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKeyboardCalculatorBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setClickListenersForNumbers() = with(binding) {
        constraintBottomSide.forEach { item ->
            if ((item is TextView) && (item.text.toString() in "0".."9"))
                item.setOnClickListener { numbersOnClick(it as TextView) }
        }
    }

    private fun numbersOnClick(view: TextView) {
        sendResult(view.text.toString())
    }

    private fun sendResult(resultKey: String) {
        requireActivity().supportFragmentManager.setFragmentResult(
            KEYBOARD_REQUEST_KEY, bundleOf(
                KEYBOARD_RESULT_KEY to resultKey
            )
        )
    }

    // обработчик нажатий для операций
    private fun setOnClickListenersOperations() {

        // AC button
        binding.btnClear.setOnClickListener {
            if (binding.btnClear.text == "C") {
                sendResult("CLEAR_C_VALUE")
            } else
                sendResult("CLEAR_AC_VALUE")
        }

        // Backspace button
        binding.btnBackspace.setOnClickListener {
            sendResult("BACKSPACE_VALUE")
        }

        // % button
        binding.btnPercent.setOnClickListener {
            sendResult("PERCENT_VALUE")
        }

        // Divide button
        binding.btnDivide.setOnClickListener {
            sendResult("DIVIDE_VALUE")
        }

        // Multiply button
        binding.btnMultiply.setOnClickListener {
            sendResult("MULTIPLY_VALUE")
        }

        // Subtract button
        binding.btnSubtract.setOnClickListener {
            sendResult("SUBTRACT_VALUE")
        }

        // Addition button
        binding.btnAdd.setOnClickListener {
            sendResult("ADD_VALUE")
        }

        binding.btnPoint.setOnClickListener {
            sendResult("POINT_VALUE")
        }

        // Equals button
        binding.btnEquals.setOnClickListener {
            sendResult("EQUALS_VALUE")
        }
    }


    /////////// testing features below

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