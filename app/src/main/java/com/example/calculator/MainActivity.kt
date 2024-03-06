package com.example.calculator

import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.ActivityMainBinding
import com.example.calculator.fragments.KeyboardCalculatorFragment
import com.example.calculator.fragments.ResultCalculatorFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        attachFragment(R.id.fragmentContainerResult, ResultCalculatorFragment())
        attachFragment(R.id.fragmentContainerKeyboard, KeyboardCalculatorFragment())
    }

    private fun attachFragment(fragmentID: Int, fragment: Fragment) =
        supportFragmentManager.beginTransaction()
            .replace(fragmentID, fragment)
            .commit()
}

