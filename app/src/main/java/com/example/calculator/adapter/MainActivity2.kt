package com.example.calculator.adapter

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity(), TodoAdapter.OnClickListener
//    OnTextClickListener
{
    private val list = listOf(
        Text("asdasd", 14242),
        Text("qweqwe", 14242),
        Text("asdzx", 14242),
        Text("asd4352zx", 144242),
        Text("asdzx", 14242),
        Text("asdz2345x", 14242),
        Text("asdzx", 14242),
        Text("asd2345435zx", 142442),
        Text("asd2345zx", 14242),
        Text("asdzx23453245"),
        Text("asd235zx", 14242),
        Text("as2345dzx", 124242),
        Text("asdzx", 14242),
        Text("asd2378945zx", 14242),
        Text("as3254dzx"),
        Text("asdzx", 142642),
        Text("as2345dzx", 142472),
        Text("as234756775dzx", 142542),
        Text("as345dzx", 142342),
        Text("asd2345zx", 142442),
    )

    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val text = intent.getStringExtra(KEY)

//        binding.tv.text = text

//        val adapter = TextAdapter(this) {
//            func(it)
//        }
        val adapter = TodoAdapter(this)
        binding.rv.adapter = adapter
    //    adapter.submitList(list)



        binding.ivImage.visibility = View.GONE
        val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            binding.ivImage.setImageURI(it)
        }
        binding.btnChoose.setOnClickListener {
            getImage.launch("image/*")

        }
    }

    companion object {
        const val KEY = "key"
    }

    private fun func(text: String) {

    }

    override fun onClick(id: Int) {
        delete(id)
    }

    fun delete(id: Int) {

    }

//    override fun onClick(text: String) {
//        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
//    }
}