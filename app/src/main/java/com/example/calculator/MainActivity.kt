package com.example.calculator

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PermissionGroupInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.ActivityMainBinding
import com.example.calculator.fragments.KeyboardCalculatorFragment
import com.example.calculator.fragments.ResultCalculatorFragment
import com.example.calculator.toolFragments.BottomFragment
import com.example.calculator.toolFragments.TopFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        attachFragment(R.id.fragmentContainerResult, ResultCalculatorFragment.newInstance())
        attachFragment(R.id.fragmentContainerKeyboard, KeyboardCalculatorFragment.newInstance())

        subscribeToKeyboardFragment()
    }

    private fun subscribeToKeyboardFragment() {
        supportFragmentManager.setFragmentResultListener(
            KeyboardCalculatorFragment.KEYBOARD_REQUEST_KEY,
            this
        ) { _, bundle ->
            val value = bundle.getString(KeyboardCalculatorFragment.KEYBOARD_RESULT_KEY, "")
            setResultToResultFragment(value)
        }
    }

    private fun setResultToResultFragment(value: String) {
        supportFragmentManager.setFragmentResult(
            ResultCalculatorFragment.RESULT_REQUEST_KEY,
            bundleOf(
                ResultCalculatorFragment.RESULT_RESULT_KEY to value
            )
        )

    }

    private fun attachFragment(fragmentID: Int, fragment: Fragment) =
        supportFragmentManager.beginTransaction()
            .add(fragmentID, fragment)
            .commit()
}

